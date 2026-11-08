package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.files.ConfigData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * MySQL is enabled, but not reachable: the plugin runs on the local storage instead.
 *
 * @author Christian34
 */
class SqlFallbackTest extends PluginTestBase {

    @Override
    protected void beforePluginLoad() {
        try {
            YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration(resource("/plugin.yml"));
            File folder = server.getPluginManager().createTemporaryDirectory("EasyPrefix-" + pluginYml.getString("version"));
            YamlConfiguration config = YamlConfiguration.loadConfiguration(resource("/config.yml"));
            config.set("config.sql.enabled", true);
            // nothing listens on port 1
            config.set("config.sql.host", "127.0.0.1");
            config.set("config.sql.port", 1);
            config.save(new File(folder, "config.yml"));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private InputStreamReader resource(String name) {
        return new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(name)), StandardCharsets.UTF_8);
    }

    @Test
    void runsOnTheLocalStorage() {
        assertTrue(plugin.isEnabled());
        assertTrue(plugin.isSqlFallback());
        assertEquals(StorageType.LOCAL, plugin.getStorageType());
        assertNull(plugin.getSqlDatabase());
        assertTrue(plugin.getGroupHandler().isGroup("default"));
    }

    @Test
    void savesUsersLocally() {
        PlayerMock player = addPlayer("Steve", "EasyPrefix.color.red");
        assertNull(execute(player, "color set red"));
        plugin.reload();
        assertTrue(plugin.isSqlFallback());
        assertEquals("red", user(player).getColor().getName());
    }

    @Test
    void reloadKeepsTheFallback() {
        plugin.reload();
        assertTrue(plugin.isEnabled());
        assertTrue(plugin.isSqlFallback());
        assertEquals(StorageType.LOCAL, plugin.getStorageType());
    }

    @Test
    void disablesWithoutFallback() {
        plugin.getConfigData().save(ConfigData.Keys.SQL_FALLBACK, false);
        plugin.reload();
        assertFalse(plugin.isEnabled());
    }

}
