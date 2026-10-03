package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.TestServerMock;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the tab list, the name tags and the priority of groups.
 *
 * @author Christian34
 */
class DisplayManagerTest extends PluginTestBase {

    private static String plain(Component component) {
        return component == null ? null : PlainTextComponentSerializer.plainText().serialize(component);
    }

    private Scoreboard scoreboard() {
        return server.getScoreboardManager().getMainScoreboard();
    }

    private Team team(Player player) {
        return scoreboard().getTeam(DisplayManager.TEAM_PREFIX + player.getName());
    }

    private Group group(String name) {
        return plugin.getGroupHandler().getGroup(name);
    }

    private void setConfig(String key, Object value) {
        plugin.getConfigData().save(key, value);
        plugin.getDisplayManager().start();
    }

    @BeforeEach
    void simpleDefaultGroup() {
        group("default").setPrefix("<red>[Guest] ");
        group("default").setSuffix(" <gray>*");
    }

    @Nested
    class TabList {

        @Test
        void showsPrefixAndNameWithoutSuffix() {
            // the suffix usually ends with the separator of the chat (":")
            PlayerMock player = addPlayer("Steve");
            assertEquals("[Guest] Steve", plain(player.playerListName()));
        }

        @Test
        void layoutCanBeChanged() {
            setConfig(ConfigData.Keys.DISPLAY_TAB_LIST_LAYOUT, "{prefix}{name}{suffix} <gray>(%ep_user_group%)");
            PlayerMock player = addPlayer("Steve");
            assertEquals("[Guest] Steve * (default)", plain(player.playerListName()));
        }

        @Test
        void updatesWhenGroupChanges() {
            PlayerMock player = addPlayer("Steve");
            group("Admin").setPrefix("[Admin] ");
            user(player).setGroup(group("Admin"), true);
            assertTrue(plain(player.playerListName()).startsWith("[Admin] Steve"), plain(player.playerListName()));
        }

        @Test
        void updatesWhenGroupIsEdited() {
            PlayerMock player = addPlayer("Steve");
            group("default").setPrefix("[New] ");
            assertTrue(plain(player.playerListName()).startsWith("[New] Steve"));
        }

        @Test
        void updatesWhenTagChanges() {
            PlayerMock player = addPlayer("Steve");
            group("default").setPrefix("%ep_tag_prefix% ");
            user(player).setSubgroup(plugin.getGroupHandler().getSubgroup("Vip"));
            assertTrue(plain(player.playerListName()).startsWith("VIP Steve"), plain(player.playerListName()));
        }

        @Test
        void refreshesPlaceholdersPeriodically() {
            PlayerMock player = addPlayer("Steve");
            plugin.getGroupHandler().getGroup("default").setPrefix("[%test_rank%] ");
            com.christian34.easyprefix.extensions.TestHooks.setPlaceholders(plugin, (p, text) -> text.replace("%test_rank%", "Gold"));
            // update-interval: 5 seconds
            server.getScheduler().performTicks(5 * 20);
            assertTrue(plain(player.playerListName()).startsWith("[Gold] Steve"), plain(player.playerListName()));
        }

        @Test
        void canBeDisabled() {
            PlayerMock player = addPlayer("Steve");
            setConfig(ConfigData.Keys.DISPLAY_TAB_LIST, false);
            assertFalse(plain(player.playerListName()).contains("[Guest]"));
        }

        @Test
        void disabledTabListIsLeftToOtherPlugins() {
            setConfig(ConfigData.Keys.DISPLAY_TAB_LIST, false);
            PlayerMock player = addPlayer("Steve");
            player.playerListName(Component.text("set by another plugin"));
            player.setPlayerListOrder(42);
            plugin.getDisplayManager().updateAll();
            assertEquals(Component.text("set by another plugin"), player.playerListName());
            assertEquals(42, player.getPlayerListOrder());
        }

        @Test
        void isSortedByGroupPriority() {
            PlayerMock admin = addPlayer("Anna", "EasyPrefix.group.Admin");
            PlayerMock guest = addPlayer("Steve");
            assertEquals(DisplayManager.order(90, 0), admin.getPlayerListOrder());
            assertEquals(DisplayManager.order(0, 0), guest.getPlayerListOrder());
        }

        @Test
        void playersOfAGroupAreSortedByNameWithNumbersByValue() {
            PlayerMock bot19 = addPlayer("EpBot19");
            PlayerMock bot2 = addPlayer("EpBot2");
            PlayerMock anna = addPlayer("anna");
            assertEquals(DisplayManager.order(0, 0), anna.getPlayerListOrder());
            assertTrue(anna.getPlayerListOrder() > bot2.getPlayerListOrder());
            assertTrue(bot2.getPlayerListOrder() > bot19.getPlayerListOrder());
        }

        @Test
        void naturalOrder() {
            List<String> names = new ArrayList<>(List.of("EpBot10", "epbot9", "EpBot1", "Anna", "EpBot", "EpBot01a"));
            names.sort(DisplayManager.NATURAL_ORDER);
            assertEquals(List.of("Anna", "EpBot", "EpBot1", "EpBot01a", "epbot9", "EpBot10"), names);
        }

        @Test
        void sortingCanBeDisabled() {
            PlayerMock admin = addPlayer("Anna", "EasyPrefix.group.Admin");
            setConfig(ConfigData.Keys.DISPLAY_SORT_TAB_LIST, false);
            assertEquals(0, admin.getPlayerListOrder());
        }
    }

    @Nested
    class ExcludedWorlds {

        /**
         * remembers the raw tab list name (the mock returns the player name for null) and counts the writes
         */
        private static class RecordingPlayer extends PlayerMock {
            private Component rawListName;
            private int writes;

            RecordingPlayer(org.mockbukkit.mockbukkit.ServerMock server, String name) {
                super(server, name);
            }

            @Override
            public void playerListName(Component name) {
                super.playerListName(name);
                this.rawListName = name;
                this.writes++;
            }

            @Override
            public void setPlayerListOrder(int order) {
                super.setPlayerListOrder(order);
                this.writes++;
            }
        }

        private World minigame;

        @BeforeEach
        void excludeMinigameWorlds() {
            this.minigame = server.addSimpleWorld("ctb_abc");
            setConfig(ConfigData.Keys.DISPLAY_EXCLUDED_WORLDS, List.of("CTB_*"));
        }

        private RecordingPlayer join(String name, World world, String... permissions) {
            RecordingPlayer player = new RecordingPlayer(server, name);
            player.setLocation(world.getSpawnLocation());
            for (String permission : permissions) {
                player.addAttachment(plugin, permission, true);
            }
            server.addPlayer(player);
            return player;
        }

        private World world() {
            return server.getWorld("world");
        }

        @Test
        void wildcardIgnoresCase() {
            DisplayManager display = plugin.getDisplayManager();
            assertTrue(display.isExcluded(minigame));
            assertTrue(display.isExcluded(server.addSimpleWorld("CTB_abc_lobby")));
            assertFalse(display.isExcluded(world()));
            assertFalse(display.isExcluded(server.addSimpleWorld("my_ctb_abc")));
            assertTrue(DisplayManager.worldPattern("a*c*").matcher("AxxC").matches());
            assertFalse(DisplayManager.worldPattern("a.c").matcher("abc").matches());
        }

        @Test
        void tabListIsNotSetInExcludedWorld() {
            RecordingPlayer player = join("Steve", minigame, "EasyPrefix.group.Admin");
            assertNull(player.rawListName);
            assertEquals(0, player.getPlayerListOrder());
            assertEquals(0, player.writes);
        }

        @Test
        void otherWorldsShowTheName() {
            RecordingPlayer player = join("Steve", world());
            assertEquals("[Guest] Steve", plain(player.rawListName));
        }

        @Test
        void changingWorldsRemovesAndRestoresTheName() {
            RecordingPlayer player = join("Steve", world(), "EasyPrefix.group.Admin");
            assertEquals(DisplayManager.order(90, 0), player.getPlayerListOrder());

            player.teleport(minigame.getSpawnLocation());
            assertNull(player.rawListName);
            assertEquals(0, player.getPlayerListOrder());

            player.teleport(world().getSpawnLocation());
            assertTrue(plain(player.rawListName).endsWith("Steve"), plain(player.rawListName));
            assertNotEquals("Steve", plain(player.rawListName));
            assertEquals(DisplayManager.order(90, 0), player.getPlayerListOrder());
        }

        @Test
        void nothingIsWrittenAfterTheReset() {
            RecordingPlayer player = join("Steve", world());
            player.teleport(minigame.getSpawnLocation());
            int writes = player.writes;
            plugin.getDisplayManager().updateAll();
            server.getScheduler().performTicks(3 * 5 * 20);
            plugin.getDisplayManager().update(user(player));
            assertEquals(writes, player.writes);
        }

        @Test
        void namesOfOtherPluginsAreKept() {
            RecordingPlayer player = join("Steve", world());
            player.teleport(minigame.getSpawnLocation());
            player.playerListName(Component.text("Steve", NamedTextColor.BLUE));
            player.setPlayerListOrder(7);
            plugin.getDisplayManager().updateAll();
            server.getScheduler().performTicks(5 * 20);
            assertEquals(Component.text("Steve", NamedTextColor.BLUE), player.rawListName);
            assertEquals(7, player.getPlayerListOrder());
        }

        @Test
        void nameSetByOtherPluginBeforeWorldChangeIsKept() {
            RecordingPlayer player = join("Steve", world());
            // e.g. the minigame assigns the team color right before teleporting the player
            player.playerListName(Component.text("Steve", NamedTextColor.RED));
            player.teleport(minigame.getSpawnLocation());
            assertEquals(Component.text("Steve", NamedTextColor.RED), player.rawListName);
        }

        @Test
        void disablingThePluginKeepsNamesOfOtherPlugins() {
            RecordingPlayer player = join("Steve", minigame);
            player.playerListName(Component.text("Steve", NamedTextColor.BLUE));
            server.getPluginManager().disablePlugin(plugin);
            assertEquals(Component.text("Steve", NamedTextColor.BLUE), player.rawListName);
        }

        @Test
        void nameTagTeamIsRemovedAndRestored() {
            RecordingPlayer player = join("Steve", world());
            assertNotNull(team(player));
            player.teleport(minigame.getSpawnLocation());
            assertNull(team(player));
            player.teleport(world().getSpawnLocation());
            assertNotNull(team(player));
            assertTrue(team(player).hasEntry("Steve"));
        }
    }

    @Nested
    class NameTags {

        @Test
        void showPrefixWithoutSuffix() {
            PlayerMock player = addPlayer("Steve");
            Team team = team(player);
            assertNotNull(team);
            assertTrue(team.hasEntry("Steve"));
            assertEquals("[Guest] ", plain(team.prefix()));
            assertEquals("", plain(team.suffix()));
        }

        @Test
        void layoutCanBeChanged() {
            setConfig(ConfigData.Keys.DISPLAY_NAME_TAG_PREFIX, "");
            setConfig(ConfigData.Keys.DISPLAY_NAME_TAG_SUFFIX, "{suffix}");
            PlayerMock player = addPlayer("Steve");
            assertEquals("", plain(team(player).prefix()));
            assertEquals(" *", plain(team(player).suffix()));
        }

        @Test
        void nameHasColorOfPrefixEnd() {
            PlayerMock player = addPlayer("Steve");
            assertEquals(NamedTextColor.RED, team(player).color());
        }

        @Test
        void hexColorsUseClosestNamedColor() {
            group("default").setPrefix("<#5aff5a>[Guest] ");
            PlayerMock player = addPlayer("Steve");
            assertEquals(NamedTextColor.GREEN, team(player).color());
        }

        @Test
        void teamIsRemovedOnQuit() {
            PlayerMock player = addPlayer("Steve");
            player.disconnect();
            assertNull(team(player));
        }

        @Test
        void canBeDisabled() {
            PlayerMock player = addPlayer("Steve");
            setConfig(ConfigData.Keys.DISPLAY_NAME_TAGS, false);
            assertNull(team(player));
        }

        @Test
        void teamsAreRemovedWhenPluginIsDisabled() {
            PlayerMock player = addPlayer("Steve");
            server.getPluginManager().disablePlugin(plugin);
            assertNull(team(player));
            assertEquals(Component.text("Steve"), player.playerListName());
        }
    }

    @Nested
    class Priority {

        @Test
        void highestPriorityWins() {
            // Admin 90, Moderator 70, Builder 50 - independent of the order of the set
            PlayerMock player = addPlayer("Steve", "EasyPrefix.group.Builder", "EasyPrefix.group.Admin", "EasyPrefix.group.Moderator");
            assertEquals("Admin", user(player).getGroup().getName());
        }

        @Test
        void groupsWithoutPriorityAreAboveDefault() {
            assertTrue(plugin.getGroupHandler().createGroup("Tester"));
            assertEquals(1, group("Tester").getPriority());
            plugin.reload();
            assertEquals(1, plugin.getGroupHandler().getGroup("Tester").getPriority());
            assertEquals(0, plugin.getGroupHandler().getGroup("default").getPriority());
        }

        @Test
        void changingPriorityIsSaved() {
            group("Builder").setPriority(95);
            plugin.reload();
            assertEquals(95, plugin.getGroupHandler().getGroup("Builder").getPriority());
            PlayerMock player = addPlayer("Steve", "EasyPrefix.group.Builder", "EasyPrefix.group.Admin");
            assertEquals("Builder", user(player).getGroup().getName());
        }

        @Test
        void commandSetsPriority() {
            PlayerMock admin = addAdmin("Anna");
            assertNull(execute(admin, "ep group Builder setpriority 42"));
            assertEquals(42, group("Builder").getPriority());
        }
    }

    @Test
    void leftoverTeamsAreRemovedOnStart() {
        MockBukkit.unmock();
        this.server = MockBukkit.mock(new TestServerMock());
        // e.g. after a crash, teams of the main scoreboard are saved with the world
        server.getScoreboardManager().getMainScoreboard().registerNewTeam(DisplayManager.TEAM_PREFIX + "Old");
        this.plugin = MockBukkit.load(com.christian34.easyprefix.EasyPrefix.class);
        assertNull(scoreboard().getTeam(DisplayManager.TEAM_PREFIX + "Old"));
    }

}
