package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.groups.Group;
import net.milkbowl.vault2.chat.ChatUnlocked;
import net.milkbowl.vault2.chat.InfoKey;
import net.milkbowl.vault2.helper.context.Context;
import net.milkbowl.vault2.helper.subject.Subject;
import net.milkbowl.vault2.minecraft.BukkitHelper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * VaultUnlocked (the Vault fork) is also named Vault - with its api on the classpath, EasyPrefix registers a provider
 * for the new chat api next to the legacy one.
 *
 * @author Christian34
 */
class VaultUnlockedTest extends PluginTestBase {

    @Override
    protected void beforePluginLoad() {
        MockBukkit.createMockPlugin("Vault");
    }

    private ChatUnlocked chat() {
        ChatUnlocked chat = server.getServicesManager().load(ChatUnlocked.class);
        assertNotNull(chat, "no chat provider registered");
        return chat;
    }

    private static Subject subject(PlayerMock player) {
        return BukkitHelper.fromPlayer(player);
    }

    @Test
    void registersChatProvider() {
        assertEquals("EasyPrefix", chat().getName());
        assertTrue(chat().isEnabled());
        assertTrue(chat().hasGroupSupport());
        assertTrue(plugin.getExpansionManager().isUsingVaultUnlocked());
        // the legacy api keeps working
        assertNotNull(server.getServicesManager().load(net.milkbowl.vault.chat.Chat.class));
    }

    @Nested
    class Players {

        @Test
        void returnsPrefixAndSuffixWithLegacyColors() {
            PlayerMock player = addPlayer("Steve");
            assertEquals("§7", chat().getPrefixOrNull(Context.GLOBAL, subject(player)));
            assertEquals("§f:", chat().getSuffixOrNull(Context.GLOBAL, subject(player)));
        }

        @Test
        void resolvesPlaceholders() {
            PlayerMock player = addPlayer("Steve", "EasyPrefix.group.Vip");
            user(player).setSubgroup(plugin.getGroupHandler().getSubgroup("Vip"));
            assertEquals(Optional.of("§6VIP §7| §e"), chat().getPrefix(Context.GLOBAL, subject(player)));
        }

        @Test
        void setsCustomPrefixAndSuffix() {
            PlayerMock player = addPlayer("Steve", "EasyPrefix.custom.prefix", "EasyPrefix.custom.suffix");
            assertTrue(chat().setPrefix(Context.GLOBAL, subject(player), "&b[Pro] "));
            assertTrue(chat().setSuffix(Context.GLOBAL, subject(player), " &7>"));
            assertTrue(user(player).hasCustomPrefix());
            assertEquals("§b[Pro] ", chat().getPrefixOrNull(Context.GLOBAL, subject(player)));
            assertEquals(" §7>", chat().getSuffixOrNull(Context.GLOBAL, subject(player)));
        }

        @Test
        void worksForOfflinePlayers() {
            PlayerMock player = addPlayer("Steve");
            player.disconnect();
            Subject offline = BukkitHelper.fromPlayer(server.getOfflinePlayer(player.getUniqueId()));
            assertEquals("§7", chat().getPrefixOrNull(Context.GLOBAL, offline));
        }

        @Test
        void unknownPlayerHasNoPrefix() {
            Subject unknown = Subject.player(java.util.UUID.randomUUID(), "Nobody");
            assertTrue(chat().getPrefix(Context.GLOBAL, unknown).isEmpty());
            assertFalse(chat().setPrefix(Context.GLOBAL, unknown, "&c"));
        }

        @Test
        void asyncReturnsTheSame() {
            PlayerMock player = addPlayer("Steve");
            assertEquals(Optional.of("§7"), chat().getPrefixAsync(Context.GLOBAL, subject(player)).join());
        }
    }

    @Nested
    class Groups {

        @Test
        void returnsGroupPrefix() {
            Group admin = plugin.getGroupHandler().getGroup("Admin");
            admin.setPrefix("<dark_red>Admin ");
            assertEquals("§4Admin ", chat().getPrefixOrNull(Context.GLOBAL, Subject.group("admin")));
            assertEquals("§f:", chat().getSuffixOrNull(Context.GLOBAL, Subject.group("Admin")));
        }

        @Test
        void unknownGroupIsEmpty() {
            assertTrue(chat().getPrefix(Context.GLOBAL, Subject.group("Nothing")).isEmpty());
            assertFalse(chat().setPrefix(Context.GLOBAL, Subject.group("Nothing"), "&c"));
        }

        @Test
        void setsGroupPrefixAndSuffix() {
            assertTrue(chat().setPrefix(Context.GLOBAL, Subject.group("Admin"), "&c[A] "));
            assertTrue(chat().setSuffix(Context.GLOBAL, Subject.group("Admin"), "&7:"));
            assertEquals("&c[A] ", plugin.getGroupHandler().getGroup("Admin").getPrefix());
            assertEquals("§c[A] ", chat().getPrefixOrNull(Context.GLOBAL, Subject.group("Admin")));
            assertEquals("§7:", chat().getSuffixOrNull(Context.GLOBAL, Subject.group("Admin")));
        }

        @Test
        void copiesTheRawPrefix() {
            chat().setPrefix(Context.GLOBAL, Subject.group("Admin"), "<red>[A] ");
            assertTrue(chat().copyPrefix(Context.GLOBAL, Subject.group("Admin"), Subject.group("default")));
            assertEquals("<red>[A] ", plugin.getGroupHandler().getGroup("default").getPrefix());
        }
    }

    @Test
    void infoNodesAreEmpty() {
        PlayerMock player = addPlayer("Steve");
        assertTrue(chat().get(Context.GLOBAL, subject(player), InfoKey.intKey("homes")).isEmpty());
        assertEquals(7, chat().getOrDefault(Context.GLOBAL, subject(player), InfoKey.intKey("homes"), 7));
        assertFalse(chat().set(Context.GLOBAL, subject(player), InfoKey.boolKey("vip"), true));
    }

}
