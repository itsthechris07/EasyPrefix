package com.christian34.easyprefix;

import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.sql.database.StorageType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests that the plugin starts with all files, colors, groups and commands.
 *
 * @author Christian34
 */
class PluginLoadTest extends PluginTestBase {

    @Test
    void pluginIsEnabled() {
        assertTrue(plugin.isEnabled());
        assertSame(plugin, EasyPrefix.getInstance());
        assertEquals(StorageType.LOCAL, plugin.getStorageType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"config.yml", "messages.yml", "groups.yml"})
    void createsFilesInDataFolder(String file) {
        assertTrue(new File(plugin.getDataFolder(), file).isFile(), file + " has not been created");
    }

    @Test
    void missingConfigKeysAreAdded() throws Exception {
        // a config of an older version without display.excluded-worlds
        File file = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration old = YamlConfiguration.loadConfiguration(file);
        old.set("config." + ConfigData.Keys.DISPLAY_EXCLUDED_WORLDS, null);
        old.save(file);
        plugin.getConfigData().update();
        YamlConfiguration updated = YamlConfiguration.loadConfiguration(file);
        assertTrue(updated.isList("config." + ConfigData.Keys.DISPLAY_EXCLUDED_WORLDS));
        assertTrue(updated.getStringList("config." + ConfigData.Keys.DISPLAY_EXCLUDED_WORLDS).isEmpty());
    }

    @Test
    void loadsAllColors() {
        ConfigurationSection colors = plugin.getConfigData().getSection("chat.colors");
        assertNotNull(colors);
        // colors that fail to load are skipped silently, so compare with the config
        assertEquals(colors.getKeys(false).size(), plugin.getColors().size());
    }

    @Test
    void loadsAllDecorations() {
        ConfigurationSection decorations = plugin.getConfigData().getSection("chat.decorations");
        assertNotNull(decorations);
        assertEquals(decorations.getKeys(false).size(), plugin.getDecorations().size());
    }

    @Test
    void loadsGroups() {
        Group defaultGroup = plugin.getGroupHandler().getGroup("default");
        assertEquals("default", defaultGroup.getName());
        assertNotNull(defaultGroup.getColor(), "the default group needs a color");
        assertTrue(plugin.getGroupHandler().isGroup("Admin"));
        assertNotNull(plugin.getGroupHandler().getSubgroup("Vip"));
    }

    @Test
    void commandsCanBeRegistered() {
        assertNotNull(plugin.getCommandManager());
        assertFalse(commands.commands().isEmpty());
    }

    @Test
    void noHooksWithoutOtherPlugins() {
        assertFalse(plugin.getExpansionManager().isUsingPapi());
        assertNull(server.getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class), "vault chat provider without vault");
        assertNull(server.getServicesManager().getRegistration(net.milkbowl.vault2.chat.ChatUnlocked.class), "vaultunlocked chat provider without vault");
        assertFalse(plugin.getExpansionManager().isUsingVaultUnlocked());
    }

    @Test
    void reloadKeepsPluginWorking() {
        plugin.reload();
        assertTrue(plugin.isEnabled());
        assertTrue(plugin.getGroupHandler().isGroup("default"));
    }

    @Test
    void reloadKeepsListenersOfLibraries() {
        // opening a gui registers the listener of InventoryGui under EasyPrefix (cloud does the same on a real server)
        new com.christian34.easyprefix.utils.UserInterface(user(addAdmin("Admin"))).openPageSetup();
        int listeners = org.bukkit.event.HandlerList.getRegisteredListeners(plugin).size();
        plugin.reload();
        assertEquals(listeners, org.bukkit.event.HandlerList.getRegisteredListeners(plugin).size());
    }

}
