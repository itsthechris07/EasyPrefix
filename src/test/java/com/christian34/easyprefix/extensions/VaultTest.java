package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.groups.Group;
import net.milkbowl.vault.chat.Chat;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Vault is only an api - a plugin named Vault is enough for EasyPrefix to register its chat provider.
 *
 * @author Christian34
 */
class VaultTest extends PluginTestBase {

    @Override
    protected void beforePluginLoad() {
        MockBukkit.createMockPlugin("Vault");
    }

    private Chat chat() {
        Chat chat = server.getServicesManager().load(Chat.class);
        assertNotNull(chat, "no chat provider registered");
        return chat;
    }

    @Test
    void registersChatProvider() {
        assertEquals("EasyPrefix", chat().getName());
        assertTrue(chat().isEnabled());
    }

    @Nested
    class Players {

        @Test
        void returnsPrefixAndSuffixWithLegacyColors() {
            PlayerMock player = addPlayer("Steve");
            assertEquals("§7", chat().getPlayerPrefix(player));
            assertEquals("§f:", chat().getPlayerSuffix(player));
        }

        @Test
        void resolvesPlaceholders() {
            PlayerMock player = addPlayer("Steve", "EasyPrefix.group.Vip");
            user(player).setSubgroup(plugin.getGroupHandler().getSubgroup("Vip"));
            assertEquals("§6VIP §7| §e", chat().getPlayerPrefix(player));
        }

        @Test
        void setsCustomPrefix() {
            PlayerMock player = addPlayer("Steve", "EasyPrefix.custom.prefix");
            chat().setPlayerPrefix(player, "&b[Pro] ");
            assertTrue(user(player).hasCustomPrefix());
            assertEquals("§b[Pro] ", chat().getPlayerPrefix(player));
        }

        @Test
        void setsCustomSuffix() {
            PlayerMock player = addPlayer("Steve", "EasyPrefix.custom.suffix");
            chat().setPlayerSuffix(player, " &7>");
            assertEquals(" §7>", chat().getPlayerSuffix(player));
        }

        @Test
        @SuppressWarnings("deprecation")
        void worksWithNames() {
            addPlayer("Steve");
            assertEquals("§7", chat().getPlayerPrefix("world", "Steve"));
            assertEquals("", chat().getPlayerPrefix("world", "Nobody"));
        }

        @Test
        void worksForOfflinePlayers() {
            PlayerMock player = addPlayer("Steve");
            player.disconnect();
            assertEquals("§7", chat().getPlayerPrefix("world", server.getOfflinePlayer(player.getUniqueId())));
        }
    }

    @Nested
    class Groups {

        @Test
        void returnsGroupPrefix() {
            Group admin = plugin.getGroupHandler().getGroup("Admin");
            admin.setPrefix("<dark_red>Admin ");
            assertEquals("§4Admin ", chat().getGroupPrefix("world", "admin"));
            assertEquals("§f:", chat().getGroupSuffix("world", "Admin"));
        }

        @Test
        void unknownGroupIsEmpty() {
            assertEquals("", chat().getGroupPrefix("world", "Nothing"));
        }

        @Test
        void setsGroupPrefixAndSuffix() {
            chat().setGroupPrefix("world", "Admin", "&c[A] ");
            chat().setGroupSuffix("world", "Admin", "&7:");
            assertEquals("&c[A] ", plugin.getGroupHandler().getGroup("Admin").getPrefix());
            assertEquals("§c[A] ", chat().getGroupPrefix("world", "Admin"));
            assertEquals("§7:", chat().getGroupSuffix("world", "Admin"));
        }

        @Test
        void providesGroupsWithoutPermissionPlugin() {
            PlayerMock player = addPlayer("Steve", "EasyPrefix.group.Admin");
            assertEquals("Admin", chat().getPrimaryGroup(player));
            assertArrayEquals(new String[]{"Admin"}, chat().getPlayerGroups(player));
            assertTrue(chat().playerInGroup(player, "admin"));
            assertFalse(chat().playerInGroup(player, "default"));
            assertTrue(Arrays.asList(chat().getGroups()).containsAll(java.util.List.of("default", "Admin")));
        }
    }

    @Test
    void infoNodesReturnDefaults() {
        PlayerMock player = addPlayer("Steve");
        assertEquals(7, chat().getPlayerInfoInteger(player, "homes", 7));
        assertEquals(1.5, chat().getPlayerInfoDouble(player, "multiplier", 1.5));
        assertTrue(chat().getPlayerInfoBoolean(player, "vip", true));
        assertEquals("none", chat().getPlayerInfoString(player, "title", "none"));
        assertEquals(3, chat().getGroupInfoInteger("world", "Admin", "homes", 3));
    }

}
