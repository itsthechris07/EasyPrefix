package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Decoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * PlaceholderAPI itself can't run on MockBukkit (it needs the real plugin class loader), so the expansion is called
 * directly like PlaceholderAPI would, and placeholders of other plugins are resolved by a fake resolver.
 *
 * @author Christian34
 */
class PlaceholderApiTest extends PluginTestBase {

    private ExpansionManager expansions() {
        return plugin.getExpansionManager();
    }

    @Nested
    class Expansion {
        private CustomPlaceholder expansion;

        @BeforeEach
        void createExpansion() {
            this.expansion = new CustomPlaceholder(expansions());
        }

        @Test
        void isRegisteredAsEp() {
            assertEquals("ep", expansion.getIdentifier());
            assertTrue(expansion.persist(), "must survive /papi reload");
            assertEquals(plugin.getPluginMeta().getVersion(), expansion.getVersion());
        }

        @Test
        void returnsGroupAndLayout() {
            PlayerMock player = addPlayer("Steve");
            assertEquals("default", expansion.onRequest(player, "user_group"));
            assertEquals("§7", expansion.onRequest(player, "user_prefix"));
            assertEquals("§f:", expansion.onRequest(player, "user_suffix"));
            assertEquals(expansion.onRequest(player, "user_prefix"), expansion.onRequest(player, "prefix"));
        }

        @Test
        void hoverPlaceholdersAreInternal() {
            PlayerMock player = addPlayer("Steve");
            for (String name : new String[]{"user_playtime", "user_first_join", "user_world", "user_real_name", "user_display_name"}) {
                assertNull(expansion.onRequest(player, name), "%ep_" + name + "% must not be available in PlaceholderAPI");
            }
        }

        @Test
        void returnsLegacyColorsInsteadOfTags() {
            PlayerMock player = addPlayer("Steve");
            plugin.getGroupHandler().getGroup("default").setPrefix("<red>[Guest] ");
            String prefix = expansion.onRequest(player, "user_prefix");
            assertEquals("§c[Guest] ", prefix);
        }

        @Test
        void resolvesPlaceholdersInPrefix() {
            PlayerMock player = addPlayer("Steve", "EasyPrefix.group.Vip");
            user(player).setSubgroup(plugin.getGroupHandler().getSubgroup("Vip"));
            // group Vip: '%ep_tag_prefix% &7| &e' with tag Vip: '&6VIP'
            assertEquals("§6VIP §7| §e", expansion.onRequest(player, "user_prefix"));
        }

        @Test
        void returnsTag() {
            PlayerMock player = addPlayer("Steve");
            assertEquals("", expansion.onRequest(player, "user_tag"));
            assertEquals("", expansion.onRequest(player, "tag_prefix"));

            user(player).setSubgroup(plugin.getGroupHandler().getSubgroup("Vip"));
            assertEquals("Vip", expansion.onRequest(player, "user_tag"));
            assertEquals("§6VIP", expansion.onRequest(player, "tag_prefix"));
            assertEquals(expansion.onRequest(player, "tag_prefix"), expansion.onRequest(player, "user_subgroup_prefix"));
        }

        @Test
        void returnsColorAndFormatting() {
            PlayerMock player = addPlayer("Steve");
            User user = user(player);
            user.setColor(Color.of("red"));
            user.setDecoration(Decoration.of("bold"));
            assertEquals("<red>", expansion.onRequest(player, "user_chatcolor"));
            assertEquals("red", expansion.onRequest(player, "user_color"));
            assertEquals("bold", expansion.onRequest(player, "user_formatting"));
        }

        @Test
        void ignoresCase() {
            PlayerMock player = addPlayer("Steve");
            assertEquals("default", expansion.onRequest(player, "USER_GROUP"));
        }

        @Test
        void unknownPlaceholderIsNotReplaced() {
            assertNull(expansion.onRequest(addPlayer("Steve"), "nonsense"));
        }

        @Test
        void worksWithoutPlayer() {
            assertEquals("", expansion.onRequest(null, "user_group"));
        }

        @Test
        void worksForOfflinePlayers() {
            PlayerMock player = addPlayer("Steve");
            user(player).setSubgroup(plugin.getGroupHandler().getSubgroup("Vip"));
            player.disconnect();
            assertEquals("Vip", expansion.onRequest(server.getOfflinePlayer(player.getUniqueId()), "user_tag"));
        }

        @Test
        void unknownOfflinePlayerIsEmpty() {
            assertEquals("", expansion.onRequest(server.getOfflinePlayer("Nobody"), "user_group"));
        }
    }

    @Nested
    class OtherPlugins {

        @BeforeEach
        void fakePlaceholderApi() {
            expansions().setPlaceholderResolver((player, text) -> text
                    .replace("%test_rank%", "Gold")
                    .replace("%test_name%", String.valueOf(player.getName())));
        }

        @Test
        void placeholderApiIsUsed() {
            assertTrue(expansions().isUsingPapi());
        }

        @Test
        void resolvesPlaceholdersInPrefix() {
            PlayerMock player = addPlayer("Steve");
            plugin.getGroupHandler().getGroup("default").setPrefix("[%test_rank%] ");
            String line = PlainTextComponentSerializer.plainText().serialize(
                    ChatListener.formatLine(user(player), ChatListener.formatMessage(user(player), "hi")));
            assertTrue(line.startsWith("[Gold] Steve"), line);
        }

        @Test
        void resolvesPlaceholdersInJoinMessages() {
            User user = user(addPlayer("Steve"));
            assertEquals("Steve joined as Gold", plugin.setPlaceholders(user, "%test_name% joined as %test_rank%"));
        }

        @Test
        void ownPlaceholdersAreResolvedFirst() {
            User user = user(addPlayer("Steve"));
            assertEquals("default Gold", plugin.setPlaceholders(user, "%ep_user_group% %test_rank%"));
        }

        @Test
        void brokenResolverKeepsText() {
            expansions().setPlaceholderResolver((player, text) -> {
                throw new IllegalStateException("broken expansion");
            });
            User user = user(addPlayer("Steve"));
            assertEquals("hello %test_rank%", plugin.setPlaceholders(user, "hello %test_rank%"));
        }
    }

    @Nested
    class WithoutPlaceholderApi {

        @Test
        void isNotUsedWhenNotInstalled() {
            assertFalse(expansions().isUsingPapi());
        }

        @Test
        void ownPlaceholdersStillWork() {
            PlayerMock player = addPlayer("Steve");
            user(player).setSubgroup(plugin.getGroupHandler().getSubgroup("Vip"));
            // the tag prefix of Vip is "&6VIP"
            assertEquals("default &6VIP Steve", plugin.setPlaceholders(user(player), "%ep_user_group% %ep_tag_prefix% %player%"));
        }
    }

}
