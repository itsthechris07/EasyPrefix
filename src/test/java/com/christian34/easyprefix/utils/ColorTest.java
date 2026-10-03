package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.PluginTestBase;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the chat colors and formattings from config.yml.
 *
 * @author Christian34
 */
class ColorTest extends PluginTestBase {

    @Test
    void findsColorsIgnoringCase() {
        Color red = Color.of("RED");
        assertNotNull(red);
        assertEquals("red", red.getName());
        assertEquals(NamedTextColor.RED.value(), red.getTextColor().value());
        assertEquals("c", red.getColorCode());
        assertEquals("<red>", red.getTagName());
    }

    @Test
    void unknownColorIsNull() {
        assertNull(Color.of("pink"));
        assertNull(Color.of(null));
    }

    @Test
    void colorsNeedPermissionUnlessDefault() {
        Color red = Color.of("red");
        assertNotNull(red);
        assertNotNull(red.getPermission());
        assertEquals("easyprefix.color.red", red.getPermission().getName());
    }

    @Test
    void rainbowIsSpecial() {
        Color rainbow = Color.of("rainbow");
        assertNotNull(rainbow);
        assertEquals("x", rainbow.getColorCode());
        assertEquals("Rainbow", rainbow.getDisplayName());
    }

    @Test
    void displayNameIsColored() {
        Color red = Color.of("red");
        assertNotNull(red);
        assertEquals("§cRed", red.getDisplayName());
    }

    @Test
    void findsColorsStoredByOlderVersions() {
        Color red = Color.of("red");
        assertSame(red, Color.of("<red>"));
        assertSame(red, Color.of("&c"));
        assertSame(red, Color.of("§c"));
        assertSame(red, Color.of("c"));
        assertNull(Color.of("&z"));
    }

    @Test
    void findsDecorationsStoredByOlderVersions() {
        Decoration bold = Decoration.of("bold");
        assertSame(bold, Decoration.of("&l"));
        assertSame(bold, Decoration.of("<bold>"));
        assertSame(Decoration.of("underlined"), Decoration.of("underline"));
        assertNull(Decoration.of(null));
    }

    @Test
    void findsDecorations() {
        Decoration bold = Decoration.of("bold");
        assertNotNull(bold);
        assertEquals(TextDecoration.BOLD, bold.getTextDecoration());
        assertNull(Decoration.of("sparkling"));
    }

    @Test
    void decorationsNeedPermissionUnlessDefault() {
        // like colors: 'default: true' in config.yml allows a decoration for everyone
        Decoration bold = Decoration.of("bold");
        assertNotNull(bold);
        assertNotNull(bold.getPermission(), "bold is not a default decoration and must require a permission");
    }

}
