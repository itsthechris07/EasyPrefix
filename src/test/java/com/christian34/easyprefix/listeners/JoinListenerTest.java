package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.PluginTestBase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 *
 * @author Christian34
 */
class JoinListenerTest extends PluginTestBase {

    private void executeLocal(String sql) {
        try (Connection connection = plugin.getLocalDatabase().getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * the user is loaded on the login thread, the join (main thread) does not query the database again
     */
    @Test
    void loadsUsersWhileLoggingIn() {
        plugin.getGroupHandler().createGroup("Admin");
        UUID uuid = UUID.randomUUID();
        executeLocal("INSERT INTO `users` (`uuid`, `username`, `group`, `force_group`) VALUES ('" + uuid + "', 'Steve', 'Admin', 1)");

        // runs after EasyPrefix (same priority, registered later): a change the join must not see anymore
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onPreLogin(AsyncPlayerPreLoginEvent e) {
                executeLocal("UPDATE `users` SET `group` = NULL WHERE `uuid` = '" + uuid + "'");
            }
        }, MockBukkit.createMockPlugin());

        PlayerMock player = new PlayerMock(server, "Steve", uuid);
        server.addPlayer(player);
        assertEquals("Admin", user(player).getGroup().getName());

        // later logins read the database again
        plugin.getUsers().clear();
        assertEquals("default", user(player).getGroup().getName());
    }

    /**
     * the server does not know the name of a new player while they are logging in, it is taken from the login
     */
    @Test
    void storesTheNameOfNewPlayers() throws SQLException {
        PlayerMock player = addPlayer("Steve");
        assertEquals("default", user(player).getGroup().getName());
        assertEquals("Steve", plugin.getLocalDatabase().query("SELECT `username` FROM `users` WHERE `uuid` = '"
                + player.getUniqueId() + "'", result -> result.next() ? result.getString(1) : null));
    }

}
