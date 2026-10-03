package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.user.User;
import org.bukkit.event.inventory.InventoryType;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the /easyprefix command.
 *
 * @author Christian34
 */
class CommandEasyPrefixTest extends PluginTestBase {

    @Test
    void mainCommandShowsVersion() {
        PlayerMock player = addPlayer("Steve");
        execute(player, "ep");
        assertContains(messages(player), plugin.getPluginMeta().getVersion());
    }

    @Test
    void aliasWorks() {
        PlayerMock player = addPlayer("Steve");
        execute(player, "easyprefix");
        assertContains(messages(player), "EasyPrefix");
    }

    @Test
    void helpListsCommands() {
        PlayerMock admin = addAdmin("Admin");
        assertNull(execute(admin, "ep help"));
        assertFalse(messages(admin).isEmpty());
    }

    @Test
    void showsGroupInfo() {
        PlayerMock admin = addAdmin("Admin");
        assertNull(execute(admin, "ep group default info"));
        List<String> messages = messages(admin);
        assertContains(messages, "default");
        assertContains(messages, "Prefix");
        assertContains(messages, "Chat color");
    }

    @Test
    void groupNamesIgnoreCase() {
        PlayerMock admin = addAdmin("Admin");
        assertNull(execute(admin, "ep group ADMIN info"));
        assertContains(messages(admin), "Admin");
    }

    @Test
    void unknownGroupGivesError() {
        PlayerMock admin = addAdmin("Admin");
        assertNotNull(execute(admin, "ep group nothing info"));
        assertContains(messages(admin), "Group was not found");
    }

    @Test
    void incompleteCommandShowsSyntax() {
        PlayerMock admin = addAdmin("Admin");
        assertNotNull(execute(admin, "ep group default"));
        assertContains(messages(admin), "Invalid syntax");
    }

    @Test
    void adminCommandsNeedPermission() {
        PlayerMock player = addPlayer("Steve");
        assertNotNull(execute(player, "ep group default info"));
        assertContains(messages(player), "You do not have permission");
    }

    @Test
    void setsGroupPrefixWithQuotes() {
        PlayerMock admin = addAdmin("Admin");
        execute(admin, "ep group Admin setprefix \"&4[A] \"");
        assertEquals("&4[A] ", plugin.getGroupHandler().getGroup("Admin").getPrefix());
    }

    @Test
    void setsGroupSuffix() {
        PlayerMock admin = addAdmin("Admin");
        execute(admin, "ep group Admin setsuffix >>");
        assertEquals(">>", plugin.getGroupHandler().getGroup("Admin").getSuffix());
    }

    @Test
    void showsUserInfo() {
        PlayerMock admin = addAdmin("Admin");
        addPlayer("Steve");
        assertNull(execute(admin, "ep user Steve info"));
        List<String> messages = messages(admin);
        assertContains(messages, "Steve");
        assertContains(messages, "Group: default");
    }

    @Test
    void setsGroupOfUser() {
        PlayerMock admin = addAdmin("Admin");
        PlayerMock target = addPlayer("Steve");
        execute(admin, "ep user Steve setgroup Admin");
        assertEquals("Admin", user(target).getGroup().getName());
    }

    @Test
    void setsTagOfUser() {
        PlayerMock admin = addAdmin("Admin");
        PlayerMock target = addPlayer("Steve");
        execute(admin, "ep user Steve settag Vip");
        assertEquals("Vip", user(target).getSubgroup().getName());
        assertEquals(1, messages(admin).stream().filter(message -> message.contains("User has been updated")).count(),
                "confirmation must only be sent once");
    }

    @Test
    void setsCustomPrefixOfUser() {
        PlayerMock admin = addAdmin("Admin");
        PlayerMock target = addPlayer("Steve");
        target.addAttachment(plugin, "EasyPrefix.custom.prefix", true);
        execute(admin, "ep user Steve setprefix \"&6[Pro] \"");
        User user = user(target);
        assertTrue(user.hasCustomPrefix());
    }

    @Test
    void settingsOpenGui() {
        PlayerMock player = addPlayer("Steve");
        player.addAttachment(plugin, "EasyPrefix.settings", true);
        execute(player, "ep settings");
        assertEquals(InventoryType.CHEST, player.getOpenInventory().getTopInventory().getType());
    }

    @Test
    void setupOpensGuiForAdmins() {
        PlayerMock admin = addAdmin("Admin");
        execute(admin, "ep setup");
        assertEquals(InventoryType.CHEST, admin.getOpenInventory().getTopInventory().getType());
    }

    @Test
    void setupIsForbiddenForPlayers() {
        PlayerMock player = addPlayer("Steve");
        execute(player, "ep setup");
        var top = player.getOpenInventory().getTopInventory();
        assertTrue(top == null || top.getType() != InventoryType.CHEST);
        assertContains(messages(player), "You do not have permission");
    }

    @Test
    void reloads() {
        PlayerMock admin = addAdmin("Admin");
        assertNull(execute(admin, "ep reload"));
        assertContains(messages(admin), "reloaded");
        assertTrue(plugin.isEnabled());
    }

}
