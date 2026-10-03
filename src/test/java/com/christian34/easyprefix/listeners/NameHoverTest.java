package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.Subgroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the hover text of names in the chat and its own placeholders.
 *
 * @author Christian34
 */
class NameHoverTest extends PluginTestBase {

    private List<String> lines(PlayerMock player) {
        Component hover = NameHover.create(user(player));
        assertNotNull(hover);
        return List.of(PlainTextComponentSerializer.plainText().serialize(hover).split("\n"));
    }

    @Test
    void showsRankPlaytimeAndFirstJoin() {
        PlayerMock player = addPlayer("Steve");
        player.setStatistic(Statistic.PLAY_ONE_MINUTE, 20 * 60 * 90);
        List<String> lines = lines(player);
        assertEquals("Steve", lines.getFirst());
        assertTrue(lines.contains("Rank: default"), lines.toString());
        assertTrue(lines.contains("Playtime: 1h 30m"), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.startsWith("Member since: ")), lines.toString());
        assertEquals("» Click to write a message", lines.getLast());
    }

    @Test
    void leavesOutLinesWithoutValue() {
        PlayerMock player = addPlayer("Steve");
        List<String> lines = lines(player);
        assertTrue(lines.stream().noneMatch(line -> line.startsWith("Tag:")), "player has no tag: " + lines);
        assertTrue(lines.stream().noneMatch(line -> line.startsWith("Name:")), "name equals display name: " + lines);
    }

    @Test
    void showsTagAndRealNameIfSet() {
        PlayerMock player = addPlayer("Steve");
        Subgroup tag = plugin.getGroupHandler().getSubgroups().iterator().next();
        user(player).setSubgroup(tag);
        player.displayName(Component.text("Stevie"));
        List<String> lines = lines(player);
        assertEquals("Stevie", lines.getFirst());
        assertTrue(lines.contains("Name: Steve"), lines.toString());
        assertTrue(lines.contains("Tag: " + tag.getName()), lines.toString());
    }

    @Test
    void boldNameDoesNotContinueInNextLine() {
        PlayerMock player = addPlayer("Steve");
        Component hover = NameHover.create(user(player));
        assertNotNull(hover);
        Component rank = hover.children().stream()
                .filter(child -> PlainTextComponentSerializer.plainText().serialize(child).contains("Rank"))
                .findFirst().orElseThrow();
        assertNotEquals(TextDecoration.State.TRUE, rank.decoration(TextDecoration.BOLD));
    }

    @Test
    void colorPlaceholdersDoNotHideLines() {
        assertEquals("§cAdmin", NameHover.replace("%ep_user_group_color%Admin", Map.of("ep_user_group_color", "§c")));
        assertNull(NameHover.replace("Tag: %ep_user_tag%", Map.of("ep_user_tag", "")));
        assertNull(NameHover.replace("Prefix: %ep_user_group_prefix%", Map.of("ep_user_group_prefix", "<gray>")));
    }

    @Test
    void groupCanHaveOwnHover() {
        PlayerMock player = addPlayer("Steve");
        plugin.getGroupHandler().getGroup("default").setHover(List.of("&6Guest %ep_user_name%", "", "&7Tag: %ep_user_tag%"));
        assertEquals(List.of("Guest Steve"), lines(player));
    }

    @Test
    void ownHoverIsSavedAndLoadedAgain() {
        plugin.getGroupHandler().getGroup("default").setHover(List.of("a", "", "b"));
        plugin.reload();
        assertEquals(List.of("a", "", "b"), plugin.getGroupHandler().getGroup("default").getOwnHover());
        plugin.getGroupHandler().getGroup("default").setHover(null);
        plugin.reload();
        assertNull(plugin.getGroupHandler().getGroup("default").getOwnHover());
    }

    @Test
    void groupsWithoutOwnHoverUseTheOneOfTheDefaultGroup() {
        Group admin = plugin.getGroupHandler().getGroup("Admin");
        admin.setHover(null);
        plugin.getGroupHandler().getGroup("default").setHover(List.of("from default"));
        assertEquals(List.of("from default"), admin.getHover());
        admin.setHover(List.of("own"));
        assertEquals(List.of("own"), admin.getHover());
    }

    @Test
    void builtInHoverIfNotEvenTheDefaultGroupHasOne() {
        plugin.getGroupHandler().getGroup("default").setHover(null);
        assertEquals(Group.DEFAULT_HOVER, plugin.getGroupHandler().getGroup("Admin").getHover());
        assertEquals(Group.DEFAULT_HOVER, plugin.getGroupHandler().getGroup("default").getHover());
    }

    @Test
    void hoverOfOlderMessagesYmlMovesToDefaultGroup() throws Exception {
        plugin.getGroupHandler().getGroup("default").setHover(null);
        File messages = new File(plugin.getDataFolder(), "messages.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(messages);
        yaml.set("chat_name_hover", List.of("old hover"));
        yaml.save(messages);

        plugin.reload();
        assertEquals(List.of("old hover"), plugin.getGroupHandler().getGroup("default").getOwnHover());
        assertFalse(YamlConfiguration.loadConfiguration(messages).contains("chat_name_hover"));
    }

    @Test
    void formatsPlaytime() {
        assertEquals("1m", NameHover.playtime(0));
        assertEquals("5m", NameHover.playtime(5 * 60));
        assertEquals("12h 30m", NameHover.playtime(12 * 3600 + 30 * 60));
        assertEquals("3d 4h", NameHover.playtime(3 * 86400 + 4 * 3600 + 59));
    }

}
