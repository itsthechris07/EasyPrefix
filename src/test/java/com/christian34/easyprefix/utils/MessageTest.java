package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.PluginTestBase;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the texts of messages.yml.
 *
 * @author Christian34
 */
class MessageTest extends PluginTestBase {

    @Test
    void everyMessageExistsInMessagesYml() {
        YamlConfiguration bundled = YamlConfiguration.loadConfiguration(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("messages.yml")), StandardCharsets.UTF_8));
        for (Message message : Message.values()) {
            assertTrue(bundled.isSet(message.getPath()), "messages.yml misses '" + message.getPath() + "' (Message." + message.name() + ")");
        }
    }

    @Test
    void textMessagesAreNotEmpty() {
        for (Message message : Message.values()) {
            if (message.getList().isEmpty()) {
                assertFalse(message.getText().isEmpty(), "Message." + message.name() + " is empty");
            }
        }
    }

    @Test
    void replacesPlaceholders() {
        String text = Message.COLOR_RESET_PLAYER.get("player", "Steve");
        assertTrue(text.contains("Steve"));
        assertFalse(text.contains("%player%"));
        assertFalse(text.contains("%prefix%"), "%prefix% has not been replaced");
        assertTrue(text.startsWith(Message.PREFIX_ALT.getText()));
    }

    @Test
    void translatesColorCodes() {
        assertFalse(Message.CHAT_NO_PERMS.getText().contains("&c"));
        assertTrue(Message.CHAT_NO_PERMS.getText().contains("§c"));
    }

    @Test
    void replacesNewlines() {
        assertTrue(Message.CHAT_TAGS_HEADER.getText().contains("\n"));
    }

}
