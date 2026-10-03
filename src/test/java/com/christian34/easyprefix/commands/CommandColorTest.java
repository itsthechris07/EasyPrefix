package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Decoration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.event.inventory.InventoryType;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the /color command.
 *
 * @author Christian34
 */
class CommandColorTest extends PluginTestBase {

    @Test
    void opensColorMenu() {
        PlayerMock player = addPlayer("Steve");
        execute(player, "color");
        assertEquals(InventoryType.CHEST, player.getOpenInventory().getTopInventory().getType());
    }

    @Test
    void setsAllowedColor() {
        PlayerMock player = addPlayer("Steve", "easyprefix.color.red");
        execute(player, "color set red");
        assertEquals("red", user(player).getColor().getName());
        assertContains(messages(player), "Red");
    }

    @Test
    void rejectsColorWithoutPermission() {
        PlayerMock player = addPlayer("Steve");
        Color before = user(player).getColor();
        execute(player, "color set red");
        assertEquals(before, user(player).getColor());
        assertContains(messages(player), "Couldn't find color red");
    }

    @Test
    void rejectsUnknownColor() {
        PlayerMock player = addPlayer("Steve");
        execute(player, "color set pink");
        assertContains(messages(player), "Couldn't find color pink");
    }

    @Test
    void adminSetsColorOfOtherPlayer() {
        PlayerMock admin = addAdmin("Admin");
        PlayerMock target = addPlayer("Steve");
        execute(admin, "color Steve set red");
        assertEquals("red", user(target).getColor().getName());
        assertContains(messages(admin), "Steve");
    }

    @Test
    void rainbowColorsEveryLetter() {
        PlayerMock admin = addAdmin("Admin");
        PlayerMock target = addPlayer("Steve");
        execute(admin, "color Steve set rainbow");
        Component message = ChatListener.formatMessage(user(target), "Hello");
        Set<TextColor> colors = new HashSet<>();
        message.iterator(ComponentIteratorType.DEPTH_FIRST).forEachRemaining(c -> {
            if (c instanceof TextComponent text && !text.content().isEmpty() && c.color() != null) colors.add(c.color());
        });
        assertTrue(colors.size() > 1, "rainbow should use several colors, got " + colors);
    }

    @Test
    void setsAllowedFormatting() {
        PlayerMock player = addPlayer("Steve", "easyprefix.color.bold");
        execute(player, "color format bold");
        assertEquals("bold", user(player).getDecoration().getName());
        assertContains(messages(player), "Your formatting has been set to Bold");

        execute(player, "color format none");
        assertNull(user(player).getDecoration());
        assertContains(messages(player), "Your formatting has been removed");
    }

    @Test
    void rejectsFormattingWithoutPermission() {
        PlayerMock player = addPlayer("Steve");
        assertNotNull(execute(player, "color format bold"));
        assertNull(user(player).getDecoration());
        assertContains(messages(player), "Couldn't find formatting bold");
    }

    @Test
    void adminSetsFormattingOfOtherPlayer() {
        PlayerMock admin = addAdmin("Admin");
        PlayerMock target = addPlayer("Steve");
        execute(admin, "color Steve format underlined");
        assertEquals("underlined", user(target).getDecoration().getName());
        assertContains(messages(admin), "The formatting for Steve has been set to Underlined");

        execute(admin, "color Steve format none");
        assertNull(user(target).getDecoration());
    }

    @Test
    void playersCannotSetFormattingOfOthers() {
        PlayerMock player = addPlayer("Alex", "easyprefix.color.bold");
        PlayerMock target = addPlayer("Steve");
        execute(player, "color Steve format bold");
        assertNull(user(target).getDecoration());
        assertContains(messages(player), "You do not have permission");
    }

    @Test
    void resetsOwnColor() {
        PlayerMock player = addPlayer("Steve");
        user(player).setColor(Color.of("red"));
        user(player).setDecoration(Decoration.of("bold"));

        execute(player, "color reset");
        assertEquals(user(player).getGroup().getColor(), user(player).getColor());
        assertNull(user(player).getDecoration());
        assertContains(messages(player), "reset to the default of your group");
    }

    @Test
    void adminResetsColorOfOtherPlayer() {
        PlayerMock admin = addAdmin("Admin");
        PlayerMock target = addPlayer("Steve");
        user(target).setColor(Color.of("red"));

        execute(admin, "color Steve reset");
        assertEquals(user(target).getGroup().getColor(), user(target).getColor());
        assertContains(messages(admin), "The color for Steve has been reset");
    }

    @Test
    void playersCannotResetOthers() {
        PlayerMock player = addPlayer("Alex");
        PlayerMock target = addPlayer("Steve");
        user(target).setColor(Color.of("red"));

        execute(player, "color Steve reset");
        assertEquals("red", user(target).getColor().getName());
        assertContains(messages(player), "You do not have permission");
    }

    @Test
    void showsOwnPreview() {
        PlayerMock player = addPlayer("Steve");
        execute(player, "color show");
        List<String> messages = messages(player);
        assertContains(messages, "This is how chat messages of Steve look");
        assertContains(messages, "Hello, this is what my messages look like!");
    }

    @Test
    void consoleShowsPreviewOfPlayer() {
        addPlayer("Steve");
        execute(server.getConsoleSender(), "color Steve show");
        assertContains(messages(server.getConsoleSender()), "This is how chat messages of Steve look");
    }

    @Test
    void unknownPlayerGivesError() {
        PlayerMock admin = addAdmin("Admin");
        assertNotNull(execute(admin, "color Nobody show"));
        assertContains(messages(admin), "Nobody");
    }

    @Test
    void consoleCannotUsePlayerCommands() {
        execute(server.getConsoleSender(), "color show");
        assertContains(messages(server.getConsoleSender()), "You cannot use this command in the console");
    }

}
