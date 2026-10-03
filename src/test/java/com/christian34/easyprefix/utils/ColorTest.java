package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.HashSet;
import java.util.Set;

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
    void rainbowIsAnEffect() {
        Color rainbow = Color.of("rainbow");
        assertNotNull(rainbow);
        assertTrue(rainbow.isEffect());
        assertNull(rainbow.getColorCode(), "&x is the start of legacy hex colors");
        assertTrue(rainbow.getDisplayName().endsWith("w"));
        assertNotEquals("Rainbow", rainbow.getDisplayName(), "the name is shown in rainbow colors");
    }

    @Test
    void rainbowWithoutTagFromOlderConfigs() {
        plugin.getConfigData().save("chat.colors.rainbow.tag", null);
        plugin.loadFormats();
        Color rainbow = Color.of("rainbow");
        assertNotNull(rainbow);
        assertTrue(rainbow.isEffect());
    }

    @Test
    void gradientsAreEffects() {
        Color sunset = Color.of("sunset");
        assertNotNull(sunset);
        assertTrue(sunset.isEffect());
        assertEquals(0xff5555, sunset.getTextColor().value(), "the first color of the gradient for icons");
        assertEquals("easyprefix.color.sunset", sunset.getPermission().getName());

        Component text = TextUtils.miniMessage().deserialize("<sunset>Hello</sunset> world");
        assertTrue(colors(text).size() > 2, "every letter has its own color");
        assertEquals("Hello world", PlainTextComponentSerializer.plainText().serialize(text));
    }

    @Test
    void invalidEffectIsSkipped() {
        plugin.getConfigData().save("chat.colors.broken.tag", "<nonsense:1>");
        plugin.getConfigData().save("chat.colors.broken.display-name", "Broken");
        plugin.loadFormats();
        assertNull(Color.of("broken"));
        assertNotNull(Color.of("sunset"));
    }

    @Test
    void shadowsAreEffectDecorations() {
        Decoration glow = Decoration.of("glow");
        assertNotNull(glow);
        assertTrue(glow.isEffect());
        assertNull(glow.getTextDecoration());

        Component text = TextUtils.miniMessage().deserialize("<glow>Hi");
        assertNotNull(firstWithText(text).shadowColor());
        assertEquals(ShadowColor.none(), firstWithText(TextUtils.miniMessage().deserialize("<no_shadow>Hi")).shadowColor());
    }

    @Test
    void effectsColorTheChat() {
        PlayerMock player = addPlayer("Steve");
        User user = user(player);
        user.setColor(Color.of("ocean"));
        user.setDecoration(Decoration.of("neon"));

        Component message = ChatListener.formatMessage(user, "Hello there");
        assertTrue(colors(message).size() > 2);
        assertNotNull(firstWithText(message).shadowColor());
        assertEquals("Hello there", PlainTextComponentSerializer.plainText().serialize(message));
    }

    private static Set<TextColor> colors(Component component) {
        Set<TextColor> colors = new HashSet<>();
        component.iterator(ComponentIteratorType.DEPTH_FIRST).forEachRemaining(c -> {
            if (c.color() != null) colors.add(c.color());
        });
        return colors;
    }

    /**
     * @return the style of the first letter, with the styles of its parents
     */
    private static Style firstWithText(Component component) {
        Style style = component.style();
        Component current = component;
        while (!(current instanceof TextComponent text && !text.content().isEmpty()) && !current.children().isEmpty()) {
            current = current.children().getFirst();
            style = style.merge(current.style());
        }
        return style;
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
