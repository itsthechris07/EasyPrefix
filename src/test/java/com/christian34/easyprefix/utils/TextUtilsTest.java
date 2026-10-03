package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.PluginTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the conversion between MiniMessage and legacy colors.
 *
 * @author Christian34
 */
class TextUtilsTest extends PluginTestBase {

    @Test
    void replacesLegacyColorsWithTags() {
        assertEquals("<red>Hello <white>world", TextUtils.escapeLegacyColors("&cHello §fworld"));
    }

    @Test
    void replacesLegacyFormattings() {
        assertEquals("<red><bold>Admin<reset> ", TextUtils.escapeLegacyColors("&c&lAdmin&r "));
    }

    @Test
    void keepsTextWithoutColors() {
        assertEquals("Hello world", TextUtils.escapeLegacyColors("Hello world"));
        assertNull(TextUtils.escapeLegacyColors(null));
    }

    @Test
    void colorizesTags() {
        assertEquals("§cHello", TextUtils.colorize("<red>Hello"));
    }

    @Test
    void serializesHexColors() {
        // the format Bukkit reads (lore, item names, sendMessage) and other plugins expect
        assertEquals("§x§1§2§3§4§5§6Hi", TextUtils.colorize("<#123456>Hi"));
    }

}
