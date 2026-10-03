package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.TaskManager;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Keeps several servers with the same MySQL database in sync: every change is announced as a row in the messages
 * table (what has changed, e.g. a user), the other servers poll for new rows every second and reload only that.
 * Old messages are removed after a few minutes.
 *
 * @author Christian34
 */
public class SQLSynchronizer {
    static final long POLL_TICKS = 20;
    /**
     * changes that are made at once (e.g. several columns of a group) are sent as one message
     */
    static final long SEND_DELAY_TICKS = 5;
    private static final int MESSAGE_MINUTES = 10;
    /**
     * a server that could not poll for this long may have missed messages that are removed already
     */
    private static final long MISSED_MESSAGES_MILLIS = MESSAGE_MINUTES * 60_000L / 2;
    /**
     * the ids of messages are counted up when the insert starts, not when it is committed: a message with a lower id
     * can show up after a higher one, so the messages of the last seconds are read again
     */
    private static final int LATE_MESSAGE_SECONDS = 5;
    private static final int CLEANUP_POLLS = 60;

    private final EasyPrefix instance;
    private final SQLDatabase database;
    private final String serverId = UUID.randomUUID().toString();
    private final Set<Message> pending = new LinkedHashSet<>();
    private final Set<Long> handled = Collections.newSetFromMap(new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Boolean> eldest) {
            return size() > 10_000;
        }
    });
    private long lastId;
    private long lastPoll = System.currentTimeMillis();
    private int polls;
    private ScheduledTask sendTask;
    private final ScheduledTask pollTask;

    public enum Type {
        /**
         * a user, all users if there is no target
         */
        USER,
        /**
         * groups or tags, the users are reloaded as well
         */
        GROUPS,
        /**
         * the settings of config.yml in {@link SharedConfig}
         */
        CONFIG
    }

    record Message(Type type, @Nullable String target) {
    }

    public SQLSynchronizer(EasyPrefix instance, SQLDatabase database) {
        this.instance = instance;
        this.database = database;
        try {
            // older messages were made before the start, which loads everything anyway
            this.lastId = database.query("SELECT COALESCE(MAX(`id`), 0) FROM `%p%messages`", result -> result.next() ? result.getLong(1) : 0);
        } catch (SQLException ex) {
            Debug.catchException(ex);
        }
        this.pollTask = TaskManager.asyncTimer(this::poll, POLL_TICKS, POLL_TICKS);
    }

    /**
     * tells the other servers what has changed
     *
     * @param target the uuid of a user
     */
    public void announce(Type type, @Nullable String target) {
        synchronized (pending) {
            pending.add(new Message(type, target));
            if (sendTask != null) return;
            this.sendTask = TaskManager.asyncLater(this::send, SEND_DELAY_TICKS);
        }
    }

    private void send() {
        List<Message> messages;
        synchronized (pending) {
            this.sendTask = null;
            if (pending.isEmpty()) return;
            messages = new ArrayList<>(pending);
            pending.clear();
        }
        String sql = "INSERT INTO `" + database.getTablePrefix() + "messages` (`server`, `type`, `target`) VALUES (?, ?, ?)";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Message message : messages) {
                statement.setString(1, serverId);
                statement.setString(2, message.type().name());
                statement.setString(3, message.target());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            Debug.catchException(ex);
        }
    }

    private synchronized void poll() {
        boolean missed = System.currentTimeMillis() - lastPoll > MISSED_MESSAGES_MILLIS;
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT `id`, `type`, `target` FROM `" + database.getTablePrefix() + "messages` WHERE (`id` > ? OR `created_at` > CURRENT_TIMESTAMP - INTERVAL "
                + LATE_MESSAGE_SECONDS + " SECOND) AND `server` <> ? ORDER BY `id`";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, lastId);
            statement.setString(2, serverId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    long id = result.getLong("id");
                    this.lastId = Math.max(lastId, id);
                    if (!handled.add(id)) continue;
                    try {
                        messages.add(new Message(Type.valueOf(result.getString("type")), result.getString("target")));
                    } catch (IllegalArgumentException ignored) {
                        // a message of a newer version
                    }
                }
            }
            if (++polls % CLEANUP_POLLS == 0) {
                try (Statement cleanup = connection.createStatement()) {
                    cleanup.executeUpdate("DELETE FROM `" + database.getTablePrefix() + "messages` WHERE `created_at` < CURRENT_TIMESTAMP - INTERVAL " + MESSAGE_MINUTES + " MINUTE");
                }
            }
        } catch (SQLException ex) {
            // e.g. the database is not reachable for a moment - the next poll tries again
            Debug.catchException(ex);
            return;
        }
        this.lastPoll = System.currentTimeMillis();
        if (missed) {
            Debug.log("Couldn't check for changes of other servers for a while, reloading everything...");
            messages.add(new Message(Type.CONFIG, null));
            messages.add(new Message(Type.GROUPS, null));
        }
        if (!messages.isEmpty()) apply(messages);
    }

    private void apply(List<Message> messages) {
        boolean config = messages.stream().anyMatch(message -> message.type() == Type.CONFIG);
        boolean groups = messages.stream().anyMatch(message -> message.type() == Type.GROUPS);
        boolean allUsers = messages.stream().anyMatch(message -> message.type() == Type.USER && message.target() == null);
        try {
            if (config && SharedConfig.download(instance)) instance.loadFormats();
            if (groups) instance.getGroupHandler().load();
            // users reference their groups and colors
            if (config || groups || allUsers) {
                instance.reloadUsers();
            } else {
                messages.stream().map(Message::target).distinct().forEach(this::reloadUser);
            }
            instance.getDisplayManager().updateAll();
        } catch (Exception ex) {
            Debug.handleException(ex);
        }
    }

    private void reloadUser(String uuid) {
        try {
            instance.reloadUser(UUID.fromString(uuid));
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * stops checking for changes of other servers and sends the last changes, e.g. before the database is closed
     */
    public void stop() {
        pollTask.cancel();
        synchronized (pending) {
            if (sendTask != null) sendTask.cancel();
        }
        send();
    }

}
