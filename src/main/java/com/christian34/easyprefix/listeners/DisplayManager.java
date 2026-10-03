package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.TaskManager;
import com.christian34.easyprefix.utils.TextUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyFormat;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EasyPrefix 2026.
 * <p>
 * Shows prefixes and suffixes in the tab list and above the heads of players (name tags). Only uses the api: {@link
 * Player#playerListName(Component)}, {@link Player#setPlayerListOrder(int)} and scoreboard teams (not on Folia, which
 * does not support scoreboards).
 *
 * @author Christian34
 */
public class DisplayManager implements Listener {
    /**
     * teams of the main scoreboard are saved with the world, so they are recognized by this prefix and removed again
     */
    static final String TEAM_PREFIX = "ep_";
    private static final Pattern LEGACY_COLOR = Pattern.compile("§(#[0-9a-fA-F]{6}|[0-9a-fA-F])");
    private final EasyPrefix instance;
    private ScheduledTask refreshTask;
    private boolean running = false;
    /**
     * the features as they were enabled on start - disabled features are not touched at all, so other plugins can
     * manage the tab list or name tags
     */
    private boolean tabListActive, nameTagsActive;
    /**
     * worlds of 'display.excluded-worlds', compiled on (re)start
     */
    private List<Pattern> excludedWorlds = List.of();
    /**
     * the tab list name and order EasyPrefix has set per player - in excluded worlds and on stop only these are reset,
     * names set by other plugins are left alone
     */
    private final Map<UUID, Shown> shown = new ConcurrentHashMap<>();

    private record Shown(Component name, int order) {
    }

    public DisplayManager(EasyPrefix instance) {
        this.instance = instance;
    }

    private ConfigData config() {
        return instance.getConfigData();
    }

    private boolean tabList() {
        return config().getBoolean(ConfigData.Keys.DISPLAY_TAB_LIST);
    }

    private boolean nameTags() {
        return config().getBoolean(ConfigData.Keys.DISPLAY_NAME_TAGS);
    }

    /**
     * (re)starts the displays with the current config - also used after the config has been changed
     */
    public void start() {
        stop();
        // Folia does not support scoreboards
        if (!TaskManager.isFolia()) {
            // teams left over from a crash
            removeTeams();
        } else if (nameTags()) {
            Debug.warn("Name tags ('display.name-tags' in config.yml) are not supported on Folia and stay disabled");
        }
        this.excludedWorlds = config().getList(ConfigData.Keys.DISPLAY_EXCLUDED_WORLDS).stream()
                .map(DisplayManager::worldPattern).toList();
        this.tabListActive = tabList();
        this.nameTagsActive = nameTags() && !TaskManager.isFolia();
        if (!tabListActive && !nameTagsActive) return;
        this.running = true;
        long interval = config().getInt(ConfigData.Keys.DISPLAY_UPDATE_INTERVAL) * 20L;
        if (interval > 0) {
            this.refreshTask = TaskManager.globalTimer(this::updateAll, interval, interval);
        }
        updateAll();
    }

    /**
     * removes everything EasyPrefix has shown, e.g. when the plugin is disabled
     */
    public void stop() {
        if (refreshTask != null) {
            refreshTask.cancel();
            this.refreshTask = null;
        }
        if (!running) return;
        this.running = false;
        if (tabListActive) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!shown.containsKey(player.getUniqueId())) continue;
                Runnable reset = () -> resetTabList(player);
                // on Folia other regions own the players - a disabled plugin can't schedule tasks anymore
                if (Bukkit.isOwnedByCurrentRegion(player)) {
                    reset.run();
                } else if (instance.isEnabled()) {
                    TaskManager.run(player, reset);
                } else {
                    shown.remove(player.getUniqueId());
                }
            }
        }
        if (nameTagsActive) removeTeams();
        this.tabListActive = false;
        this.nameTagsActive = false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        update(instance.getUser(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        shown.remove(event.getPlayer().getUniqueId());
        if (nameTagsActive) removeTeam(event.getPlayer());
    }

    /**
     * the tab list is only updated periodically (or not at all with update-interval: 0), so excluded worlds are
     * applied right away
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        update(instance.getUser(event.getPlayer()));
    }

    public void updateAll() {
        if (!running) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            update(instance.getUser(player));
        }
    }

    public void update(@NotNull User user) {
        if (!running) return;
        Player player = user.getPlayer();
        if (player == null || !player.isOnline()) return;
        // commands and the database synchronizer run async, the scoreboard and tab list may only be changed by the
        // thread that owns the player (the main thread on Paper, its region on Folia)
        if (!Bukkit.isOwnedByCurrentRegion(player)) {
            TaskManager.run(player, () -> update(user));
            return;
        }

        if (isExcluded(player.getWorld())) {
            // e.g. a minigame shows its own team colors - only what EasyPrefix has set is removed (once)
            if (tabListActive) resetTabList(player);
            if (nameTagsActive) removeTeam(player);
            return;
        }
        if (tabListActive) {
            Component name = ChatListener.formatLayout(user, layout(ConfigData.Keys.DISPLAY_TAB_LIST_LAYOUT, "{prefix}{name}"));
            // setting the name sends a packet to every player, so only on changes
            if (!name.equals(player.playerListName())) player.playerListName(name);
            int order = config().getBoolean(ConfigData.Keys.DISPLAY_SORT_TAB_LIST) ? user.getGroup().getPriority() : 0;
            if (player.getPlayerListOrder() != order) player.setPlayerListOrder(order);
            shown.put(player.getUniqueId(), new Shown(name, order));
        }
        if (nameTagsActive) updateTeam(user);
    }

    /**
     * @return whether the world is listed in 'display.excluded-worlds'
     */
    public boolean isExcluded(@NotNull World world) {
        String name = world.getName();
        for (Pattern pattern : excludedWorlds) {
            if (pattern.matcher(name).matches()) return true;
        }
        return false;
    }

    /**
     * @param world a world name, * matches any number of characters
     */
    static Pattern worldPattern(String world) {
        StringBuilder regex = new StringBuilder();
        for (String part : world.split("\\*", -1)) {
            if (!regex.isEmpty()) regex.append(".*");
            regex.append(Pattern.quote(part));
        }
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    /**
     * removes the tab list name and order EasyPrefix has set - values another plugin has set since then are kept
     */
    private void resetTabList(Player player) {
        Shown previous = shown.remove(player.getUniqueId());
        if (previous == null) return;
        if (previous.name().equals(player.playerListName())) player.playerListName(null);
        if (previous.order() == player.getPlayerListOrder()) player.setPlayerListOrder(0);
    }

    /**
     * the team would keep the name tag and conflict with teams of other plugins on the main scoreboard
     */
    private static void removeTeam(Player player) {
        Team team = scoreboard().getTeam(teamName(player));
        if (team != null) team.unregister();
    }

    /**
     * @return the layout of config.yml - an empty value is valid (e.g. no suffix above the head)
     */
    private String layout(String key, String defaultLayout) {
        return Objects.requireNonNullElse(config().getString(key), defaultLayout);
    }

    private void updateTeam(User user) {
        Player player = user.getPlayer();
        Scoreboard scoreboard = scoreboard();
        Team team = scoreboard.getTeam(teamName(player));
        if (team == null) team = scoreboard.registerNewTeam(teamName(player));
        if (!team.hasEntry(player.getName())) team.addEntry(player.getName());

        Component prefix = ChatListener.formatLayout(user, layout(ConfigData.Keys.DISPLAY_NAME_TAG_PREFIX, "{prefix}"));
        Component suffix = ChatListener.formatLayout(user, layout(ConfigData.Keys.DISPLAY_NAME_TAG_SUFFIX, ""));
        if (!prefix.equals(team.prefix())) team.prefix(prefix);
        if (!suffix.equals(team.suffix())) team.suffix(suffix);

        // the name itself is colored by the team color, not by the end of the prefix
        NamedTextColor color = Objects.requireNonNullElse(lastColor(prefix), NamedTextColor.WHITE);
        if (!team.hasColor() || !color.equals(team.color())) team.color(color);
    }

    /**
     * @return the color at the end of the text (it would color the name), null if there is none
     */
    @Nullable
    static NamedTextColor lastColor(Component text) {
        String legacy = TextUtils.colorizeOpenEnd(TextUtils.miniMessage().serialize(text));
        Matcher matcher = LEGACY_COLOR.matcher(legacy);
        String last = null;
        while (matcher.find()) last = matcher.group(1);
        if (last == null) return null;
        if (last.startsWith("#")) {
            TextColor hex = TextColor.fromHexString(last);
            return hex == null ? null : NamedTextColor.nearestTo(hex);
        }
        LegacyFormat format = LegacyComponentSerializer.parseChar(last.charAt(0));
        return format == null || format.color() == null ? null : NamedTextColor.nearestTo(format.color());
    }

    private static Scoreboard scoreboard() {
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    private static String teamName(Player player) {
        return TEAM_PREFIX + player.getName();
    }

    private static void removeTeams() {
        for (Team team : new ArrayList<>(scoreboard().getTeams())) {
            if (team.getName().startsWith(TEAM_PREFIX)) team.unregister();
        }
    }

}
