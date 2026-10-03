package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.PluginTestBase;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the /tags command.
 *
 * @author Christian34
 */
class CommandTagsTest extends PluginTestBase {

    @Test
    void adminSetsTag() {
        PlayerMock admin = addAdmin("Admin");
        PlayerMock target = addPlayer("Steve");
        assertNull(execute(admin, "tags set Steve Vip"));
        assertEquals("Vip", user(target).getSubgroup().getName());
        assertContains(messages(admin), "Steve");
    }

    @Test
    void adminClearsTag() {
        PlayerMock admin = addAdmin("Admin");
        PlayerMock target = addPlayer("Steve");
        user(target).setSubgroup(plugin.getGroupHandler().getSubgroup("Vip"));
        assertNull(execute(admin, "tags clear Steve"));
        assertNull(user(target).getSubgroup());
    }

    @Test
    void playerSelectsAllowedTag() {
        PlayerMock player = addPlayer("Steve");
        player.addAttachment(plugin, "EasyPrefix.tags.switch", true);
        player.addAttachment(plugin, "EasyPrefix.tag.vip", true);
        assertNull(execute(player, "tags select Vip"));
        assertEquals("Vip", user(player).getSubgroup().getName());
    }

    @Test
    void playerCannotSelectForbiddenTag() {
        PlayerMock player = addPlayer("Steve");
        player.addAttachment(plugin, "EasyPrefix.tags.switch", true);
        execute(player, "tags select Vip");
        assertNull(user(player).getSubgroup());
        assertContains(messages(player), "You do not have permission");
    }

    @Test
    void unknownTagGivesError() {
        PlayerMock admin = addAdmin("Admin");
        addPlayer("Steve");
        assertNotNull(execute(admin, "tags set Steve Nothing"));
        assertFalse(messages(admin).isEmpty());
    }

    @Test
    void listsTags() {
        PlayerMock player = addPlayer("Steve");
        player.addAttachment(plugin, "EasyPrefix.tag.Vip", true);
        assertNull(execute(player, "tags list"));
        assertContains(messages(player), "(1)");
    }

}
