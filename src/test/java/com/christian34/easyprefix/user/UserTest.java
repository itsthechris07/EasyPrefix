package com.christian34.easyprefix.user;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Decoration;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests loading and saving users with their group, colors, prefix and tag.
 *
 * @author Christian34
 */
class UserTest extends PluginTestBase {

    /**
     * loads the user from the storage again, like after a rejoin
     */
    private User reload(PlayerMock player) {
        plugin.unloadUser(player);
        return plugin.getUser(player);
    }

    @Test
    void newPlayerGetsDefaultGroup() {
        User user = user(addPlayer("Steve"));
        assertEquals("default", user.getGroup().getName());
        assertNull(user.getSubgroup());
        assertNull(user.getDecoration());
    }

    @Test
    void playerGetsGroupByPermission() {
        PlayerMock player = addPlayer("Steve", "EasyPrefix.group.Admin");
        assertEquals("Admin", user(player).getGroup().getName());
    }

    @Test
    void colorFallsBackToGroupColor() {
        User user = user(addPlayer("Steve"));
        assertEquals(user.getGroup().getColor(), user.getColor());
    }

    @Test
    void savesColorAndDecoration() {
        PlayerMock player = addPlayer("Steve");
        User user = user(player);
        user.setColor(Color.of("red"));
        user.setDecoration(Decoration.of("bold"));

        User loaded = reload(player);
        assertEquals("red", loaded.getColor().getName());
        assertNotNull(loaded.getDecoration());
        assertEquals("bold", loaded.getDecoration().getName());
    }

    @Test
    void resetColorUsesGroupColorAgain() {
        PlayerMock player = addPlayer("Steve");
        User user = user(player);
        user.setColor(Color.of("red"));
        user.setColor(null);
        user.setDecoration(null);

        User loaded = reload(player);
        assertEquals(loaded.getGroup().getColor(), loaded.getColor());
        assertNull(loaded.getDecoration());
    }

    @Test
    void onlyAllowedColorsAreAvailable() {
        PlayerMock player = addPlayer("Steve");
        assertTrue(user(player).getColors().stream().noneMatch(color -> color.getName().equals("red")));

        PlayerMock other = addPlayer("Alex", "easyprefix.color.red");
        assertTrue(user(other).getColors().stream().anyMatch(color -> color.getName().equals("red")));
    }

    @Test
    void customPrefixNeedsPermission() {
        PlayerMock player = addPlayer("Steve");
        User user = user(player);
        String groupPrefix = user.getPrefix();
        user.setPrefix("&6[Pro] ");
        assertEquals(groupPrefix, user.getPrefix(), "custom prefix without permission");

        player.addAttachment(plugin, "EasyPrefix.custom.prefix", true);
        User loaded = reload(player);
        assertTrue(loaded.hasCustomPrefix());
        assertEquals("<gold>[Pro] ", loaded.getPrefix());
    }

    @Test
    void settingGroupResetsCustomisation() {
        PlayerMock player = addPlayer("Steve");
        User user = user(player);
        user.setColor(Color.of("red"));
        user.setGroup(plugin.getGroupHandler().getGroup("Admin"), true);

        User loaded = reload(player);
        assertEquals("Admin", loaded.getGroup().getName(), "forced group without permission");
        assertEquals(loaded.getGroup().getColor(), loaded.getColor());
    }

    @Test
    void savesSubgroup() {
        PlayerMock player = addPlayer("Steve");
        user(player).setSubgroup(plugin.getGroupHandler().getSubgroup("Vip"));
        assertEquals("Vip", reload(player).getSubgroup().getName());

        reload(player).setSubgroup(null);
        assertNull(reload(player).getSubgroup());
    }

    @Test
    void availableSubgroupsNeedPermission() {
        PlayerMock player = addPlayer("Steve");
        assertTrue(user(player).getAvailableSubgroups().isEmpty());
        player.addAttachment(plugin, "EasyPrefix.tag.Vip", true);
        assertEquals(1, user(player).getAvailableSubgroups().size());
    }

}
