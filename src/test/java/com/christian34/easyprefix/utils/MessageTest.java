package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.PluginTestBase;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Test
    void everyTextOfMessagesYmlIsUsed() {
        Set<String> paths = Arrays.stream(Message.values()).map(Message::getPath).collect(Collectors.toSet());
        for (String key : bundled().getKeys(true)) {
            if (bundled().isConfigurationSection(key)) continue;
            assertTrue(paths.contains(key), "messages.yml contains '" + key + "', which is no Message");
        }
    }

    @Test
    void bundledTextsAreMiniMessage() {
        YamlConfiguration bundled = bundled();
        for (String key : bundled.getKeys(true)) {
            List<String> texts = bundled.isList(key) ? bundled.getStringList(key) : List.of(String.valueOf(bundled.get(key)));
            for (String text : texts) {
                assertEquals(text, Message.setColors(text), key + " contains legacy color codes");
                assertFalse(text.contains("§"), key + " contains legacy color codes");
            }
        }
    }

    @Test
    void rendersMiniMessage() {
        assertEquals("§cHello §7World", Message.colorize("<red>Hello <gray>World"));
        assertEquals("§7a §9§lb§7 c", Message.colorize("<gray>a <blue><bold>b</bold></blue> c"));
        assertEquals("§x§f§f§8§8§0§0Hex", Message.colorize("<#ff8800>Hex"));
        assertEquals("§8| §r%line%", Message.colorize("<dark_gray>| <reset>%line%"));
        assertEquals(" ", Message.colorize(" "));
    }

    @Test
    void keepsLegacyColorsOfOlderFiles() {
        assertEquals("§cHello §lWorld", Message.colorize("&cHello &lWorld"));
        assertEquals("§cHello", Message.colorize("§cHello"));
        // a single & is no color code
        assertEquals("§9Join & Quit", Message.colorize("<blue>Join & Quit"));
    }

    @Test
    void adminTextsHaveThePrefixOfThePlugin() {
        assertTrue(Message.CMD_RELOADED.admin().startsWith(Message.PREFIX), Message.CMD_RELOADED.admin());
        // %prefix% is the one of the plugin, not prefix_alt
        String saved = Message.INPUT_SAVED.admin();
        assertTrue(saved.startsWith(Message.PREFIX), saved);
        assertFalse(saved.contains(Message.PREFIX_ALT.getText()), saved);
        assertTrue(Message.SETUP_GROUP_CREATED.admin("name", "Vip").contains("'Vip'"));
    }

    @Test
    void listsExpandValuesWithSeveralLines() {
        List<String> lore = Message.SETUP_BTN_BLACKLIST.getList("words", "one\ntwo");
        int index = lore.indexOf("one");
        assertTrue(index > 0, lore.toString());
        assertEquals("two", lore.get(index + 1));
        assertFalse(String.join("\n", lore).contains("%words%"));

        // a line that only consists of a placeholder without value is left out, spacers stay
        List<String> empty = Message.SETUP_BTN_BLACKLIST.getList("words", "");
        assertEquals(lore.size() - 2, empty.size());
        assertTrue(empty.contains(" "));
    }

    private YamlConfiguration bundled() {
        return YamlConfiguration.loadConfiguration(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("messages.yml")), StandardCharsets.UTF_8));
    }

}
