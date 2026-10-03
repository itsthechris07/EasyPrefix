package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Decoration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests loading, creating, changing and deleting groups and tags.
 *
 * @author Christian34
 */
class GroupHandlerTest extends PluginTestBase {

    private GroupHandler groups() {
        return plugin.getGroupHandler();
    }

    private YamlConfiguration groupsFile() {
        return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "groups.yml"));
    }

    @Test
    void convertsLegacyColorsOfGroups() {
        Group group = groups().getGroup("default");
        assertEquals("<gray>", group.getPrefix());
        assertEquals("<white>:", group.getSuffix());
    }

    @Test
    void defaultGroupsHaveValidColorsAndFormattings() {
        // a wrong value in the bundled groups.yml only shows up as a warning on the server
        for (Group group : groups().getGroups()) {
            assertNotNull(group.getColor(), "color of " + group.getName());
            String formatting = groupsFile().getString("groups." + group.getName() + ".chat-formatting");
            if (formatting != null) assertNotNull(group.getDecoration(), "formatting of " + group.getName());
        }
        assertEquals("rainbow", groups().getGroup("Vip").getColor().getName());
    }

    @Test
    void rainbowOfOlderVersionsBecomesAColor() {
        YamlConfiguration yaml = groupsFile();
        yaml.set("groups.Vip.chat-color", null);
        yaml.set("groups.Vip.chat-formatting", "%r");
        assertDoesNotThrow(() -> yaml.save(new File(plugin.getDataFolder(), "groups.yml")));
        plugin.reload();

        Group vip = plugin.getGroupHandler().getGroup("Vip");
        assertEquals("rainbow", vip.getColor().getName());
        assertNull(vip.getDecoration());
        assertNull(groupsFile().getString("groups.Vip.chat-formatting"), "old value is removed from the file");
    }

    @Test
    void groupColorIsTheFirstColorOfThePrefix() {
        Group group = groups().getGroup("default");
        group.setPrefix("<red>[A] <gray>");
        assertEquals("§c", group.getGroupColor());
        group.setPrefix("<#123456>Hex");
        assertEquals(2, group.getGroupColor().length(), "hex colors use the closest named color");
        group.setPrefix("");
        assertEquals("§5", group.getGroupColor());
    }

    @Test
    void newGroupsHaveNoInvalidDefaults() {
        assertTrue(groups().createGroup("Tester"));
        Group group = groups().getGroup("Tester");
        assertEquals("gray", group.getColor().getName());
        assertNull(group.getDecoration());
    }

    @Test
    void reloadDoesNotLoseGroups() {
        int amount = groups().getGroups().size();
        plugin.reload();
        assertEquals(amount, plugin.getGroupHandler().getGroups().size());
    }

    @Test
    void unknownGroupFallsBackToDefault() {
        assertEquals("default", groups().getGroup("doesnotexist").getName());
        assertFalse(groups().isGroup("doesnotexist"));
    }

    @Test
    void findsGroupsIgnoringCase() {
        assertTrue(groups().isGroup("admin"));
        assertEquals("Admin", groups().getGroup("ADMIN").getName());
    }

    @Test
    void createsGroup() {
        assertTrue(groups().createGroup("Tester"));
        assertTrue(groups().isGroup("Tester"));
        assertTrue(groupsFile().isConfigurationSection("groups.Tester"));
    }

    @Test
    void doesNotCreateGroupTwice() {
        assertTrue(groups().createGroup("Tester"));
        assertFalse(groups().createGroup("Tester"));
        assertFalse(groups().createGroup("admin"));
    }

    @Test
    void deletesGroup() {
        groups().createGroup("Tester");
        groups().getGroup("Tester").delete();
        assertFalse(groups().isGroup("Tester"));
        assertFalse(groupsFile().isSet("groups.Tester"));
    }

    @Test
    void createsSubgroup() {
        assertTrue(groups().createSubgroup("Legend"));
        assertNotNull(groups().getSubgroup("legend"));
        assertTrue(groupsFile().isConfigurationSection("subgroups.Legend"));
    }

    @Test
    void savesGroupChanges() {
        Group group = groups().getGroup("Admin");
        group.setPrefix("§4[A] ");
        group.setSuffix("&f>");
        group.setColor(Color.of("red"));
        group.setDecoration(Decoration.of("italic"));
        group.setJoinMessage("hi %player%");

        YamlConfiguration file = groupsFile();
        assertEquals("&4[A] ", file.getString("groups.Admin.prefix"));
        assertEquals("&f>", file.getString("groups.Admin.suffix"));
        assertEquals("red", file.getString("groups.Admin.chat-color"));
        assertEquals("italic", file.getString("groups.Admin.chat-formatting"));
        assertEquals("hi %player%", file.getString("groups.Admin.join-msg"));
    }

    @Test
    void changesSurviveReload() {
        groups().getGroup("Admin").setColor(Color.of("aqua"));
        plugin.reload();
        assertEquals("aqua", plugin.getGroupHandler().getGroup("Admin").getColor().getName());
    }

    @Test
    void groupsWithoutJoinMessageUseDefault() {
        Group admin = groups().getGroup("Admin");
        assertEquals(groups().getGroup("default").getJoinMessage(), admin.getJoinMessage());
    }

}
