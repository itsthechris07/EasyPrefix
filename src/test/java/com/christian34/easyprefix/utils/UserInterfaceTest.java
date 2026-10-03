package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.user.CustomLayout;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.textinput.UserInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.simulate.entity.PlayerSimulation;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the inventory menus by clicking through them. Slots are counted from the top left (0) - the first and the
 * last row of every gui are the frame of {@link GuiCreator}.
 *
 * @author Christian34
 */
class UserInterfaceTest extends PluginTestBase {

    private void click(PlayerMock player, int slot) {
        click(player, ClickType.LEFT, slot);
    }

    private void click(PlayerMock player, ClickType type, int slot) {
        new PlayerSimulation(player).simulateInventoryClick(player.getOpenInventory(), type, slot);
        server.getScheduler().performOneTick();
    }

    private static Material itemAt(PlayerMock player, int slot) {
        ItemStack item = player.getOpenInventory().getTopInventory().getItem(slot);
        return item == null ? Material.AIR : item.getType();
    }

    private static void assertNoRawTags(PlayerMock player) {
        for (ItemStack item : player.getOpenInventory().getTopInventory().getContents()) {
            if (item == null || !item.hasItemMeta() || item.getItemMeta().displayName() == null) continue;
            String name = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
            assertFalse(name.contains("<"), "item name shows a tag: " + name);
        }
    }

    private static boolean hasGuiOpen(PlayerMock player) {
        Inventory top = player.getOpenInventory().getTopInventory();
        return top != null && top.getType() == InventoryType.CHEST;
    }

    @Nested
    class UserSettings {
        // pattern: "    i    ", a centered row of the usable buttons out of a c t l m, "   v r   " + footer "<  pwn  q"
        // (without permissions: "  a c m  ")
        static final int INFO = 13, PREFIXES = 20, COLORS = 22, MENTIONS = 24, PREVIEW = 30, RESET = 32, PLUGIN_SETTINGS = 44;

        private PlayerMock open(PlayerMock player) {
            new UserInterface(user(player)).openUserSettings();
            return player;
        }

        @Test
        void showsOverview() {
            PlayerMock player = open(addPlayer("Steve"));
            assertEquals(Material.PLAYER_HEAD, itemAt(player, INFO));
            assertEquals(Material.NAME_TAG, itemAt(player, PREFIXES));
            assertEquals(Material.SPYGLASS, itemAt(player, PREVIEW));
            assertEquals(Material.BARRIER, itemAt(player, RESET));
        }

        private String infoLore(PlayerMock player) {
            List<Component> lore = player.getOpenInventory().getTopInventory().getItem(INFO).lore();
            assertNotNull(lore);
            return lore.stream().map(line -> PlainTextComponentSerializer.plainText().serialize(line)).collect(Collectors.joining("\n"));
        }

        @Test
        void infoShowsPrefixOfGroup() {
            PlayerMock player = addPlayer("Steve");
            user(player).getGroup().setPrefix("<red>Member ");
            open(player);
            assertTrue(infoLore(player).contains("Prefix: «Member »"), infoLore(player));
        }

        @Test
        void infoShowsCustomPrefix() {
            PlayerMock player = addPlayer("Steve", "easyprefix.custom.prefix");
            user(player).setPrefix("&cCustom");
            open(player);
            assertTrue(infoLore(player).contains("Prefix: «Custom»"), infoLore(player));
        }

        @Test
        void centersTheUsableButtons() {
            PlayerMock player = open(addPlayer("Steve", "easyprefix.custom.gui"));
            // no tags: prefixes, colors, layout and mentions in the middle of the row
            assertEquals(Material.NAME_TAG, itemAt(player, 19));
            assertEquals(Material.ANVIL, itemAt(player, 23));
            assertEquals(Material.BELL, itemAt(player, 25));
            assertEquals(Material.GRAY_STAINED_GLASS_PANE, itemAt(player, 20));
        }

        @Test
        void togglesMentions() {
            PlayerMock player = open(addPlayer("Steve"));
            assertEquals(Material.BELL, itemAt(player, MENTIONS));
            click(player, MENTIONS);
            assertFalse(user(player).isMentionable());
            click(player, MENTIONS);
            assertTrue(user(player).isMentionable());
        }

        @Test
        void noMentionsButtonWhenDisabledGlobally() {
            plugin.getConfigData().save(ConfigData.Keys.MENTIONS, false);
            PlayerMock player = open(addPlayer("Steve"));
            // only prefixes and colors are left
            assertEquals(Material.NAME_TAG, itemAt(player, 21));
            assertNotEquals(Material.BELL, itemAt(player, 25));
            assertNotEquals(Material.BELL, itemAt(player, 24));
        }

        @Test
        void pluginSettingsOnlyForAdmins() {
            assertNotEquals(Material.COMPARATOR, itemAt(open(addPlayer("Steve")), PLUGIN_SETTINGS));
            assertEquals(Material.COMPARATOR, itemAt(open(addAdmin("Admin")), PLUGIN_SETTINGS));
        }

        @Test
        void previewClosesGuiAndShowsMessage() {
            PlayerMock player = open(addPlayer("Steve"));
            click(player, PREVIEW);
            assertFalse(hasGuiOpen(player));
            assertContains(messages(player), "Hello, this is what my messages look like!");
        }

        @Test
        void resetsColor() {
            PlayerMock player = addPlayer("Steve");
            user(player).setColor(Color.of("red"));
            open(player);
            click(player, RESET);
            assertEquals(user(player).getGroup().getColor(), user(player).getColor());
        }

        @Test
        void resetsCustomLayout() {
            PlayerMock player = addPlayer("Steve", "easyprefix.custom.prefix", "easyprefix.custom.suffix");
            user(player).setPrefix("&cCustom ");
            user(player).setSuffix("&7>");
            open(player);
            click(player, RESET);
            assertFalse(user(player).hasCustomPrefix());
            assertFalse(user(player).hasCustomSuffix());
            assertEquals(user(player).getGroup().getPrefix(), user(player).getPrefix());
        }

        @Test
        void colorMenuShowsPermittedColors() {
            PlayerMock player = addPlayer("Steve", "easyprefix.color.red");
            new UserInterface(user(player)).openPageUserColors();
            // first slot of the color rows, default icon is wool
            assertEquals(Material.RED_WOOL, itemAt(player, 9));
            assertEquals(Material.AIR, itemAt(player, 10), "only red is permitted");
        }

        @Test
        void colorButtonOpensColorPageWithBackButton() {
            PlayerMock player = open(addPlayer("Steve"));
            click(player, COLORS);
            assertNotEquals(Material.PLAYER_HEAD, itemAt(player, INFO));
            // back button of the color page (4 rows + frame)
            click(player, 36);
            assertEquals(Material.PLAYER_HEAD, itemAt(player, INFO));
        }
    }

    @Nested
    class Setup {
        // setup: "xxaxbxcxx"; settings: " a b c d ", " e f g h ", " n o p q ", " i j m k " + footer
        static final int GROUPS = 11, SETTINGS = 13;
        static final int HANDLE_CHAT = 10, COLOR_ICONS = 12, COOLDOWN = 23, TAB_LIST = 28, NAME_TAGS = 32, MENTIONS = 34, BACK = 45;

        private PlayerMock openSettings() {
            PlayerMock admin = addAdmin("Admin");
            new UserInterface(user(admin)).openPageSetup();
            click(admin, SETTINGS);
            return admin;
        }

        @Test
        void isForbiddenForPlayers() {
            PlayerMock player = addPlayer("Steve");
            new UserInterface(user(player)).openPageSetup();
            assertFalse(hasGuiOpen(player));
            assertContains(messages(player), "You do not have permission");
        }

        @Test
        void togglesChatHandling() {
            PlayerMock admin = openSettings();
            assertEquals(Material.OAK_SIGN, itemAt(admin, HANDLE_CHAT));
            click(admin, HANDLE_CHAT);
            assertFalse(plugin.getConfigData().getBoolean(ConfigData.Keys.HANDLE_CHAT));
            click(admin, HANDLE_CHAT);
            assertTrue(plugin.getConfigData().getBoolean(ConfigData.Keys.HANDLE_CHAT));
        }

        @Test
        void togglesMentionsGlobally() {
            PlayerMock admin = openSettings();
            assertEquals(Material.BELL, itemAt(admin, MENTIONS));
            click(admin, MENTIONS);
            assertFalse(plugin.getConfigData().getBoolean(ConfigData.Keys.MENTIONS));
            click(admin, MENTIONS);
            assertTrue(plugin.getConfigData().getBoolean(ConfigData.Keys.MENTIONS));
        }

        @Test
        void changesCooldown() {
            PlayerMock admin = openSettings();
            double start = plugin.getConfigData().getDouble(ConfigData.Keys.CUSTOM_LAYOUT_COOLDOWN);
            click(admin, ClickType.LEFT, COOLDOWN);
            assertEquals(start + 0.5, plugin.getConfigData().getDouble(ConfigData.Keys.CUSTOM_LAYOUT_COOLDOWN), 0.001);
            click(admin, ClickType.SHIFT_LEFT, COOLDOWN);
            assertEquals(start + 5.5, plugin.getConfigData().getDouble(ConfigData.Keys.CUSTOM_LAYOUT_COOLDOWN), 0.001);
            for (int i = 0; i < 5; i++) click(admin, ClickType.SHIFT_RIGHT, COOLDOWN);
            assertEquals(0, plugin.getConfigData().getDouble(ConfigData.Keys.CUSTOM_LAYOUT_COOLDOWN), 0.001, "cooldown must not be negative");
        }

        @Test
        void selectsColorIcon() {
            PlayerMock admin = openSettings();
            click(admin, COLOR_ICONS);
            // icons: "  aaaaaa " -> slots 11-16 in the order of ColorIcon
            click(admin, 11 + ColorIcon.LEATHER.ordinal());
            assertEquals(ColorIcon.LEATHER, ColorIcon.fromConfig());
            // preview row shows the selected icon
            assertEquals(Material.LEATHER_HELMET, itemAt(admin, 27));
        }

        @Test
        void backButtonReturnsToSetup() {
            PlayerMock admin = openSettings();
            click(admin, BACK);
            assertEquals(Material.CHEST, itemAt(admin, GROUPS));
        }

        @Test
        void shiftClickResetsHoverOfGroup() {
            PlayerMock admin = addAdmin("Admin");
            var group = plugin.getGroupHandler().getGroup("default");
            group.setHover(java.util.List.of("own"));
            new UserInterface(user(admin)).openGroupProfile(group);
            // pattern "xabcxdehf" starts at slot 9
            assertEquals(Material.OAK_SIGN, itemAt(admin, 16));
            click(admin, ClickType.SHIFT_LEFT, 16);
            assertNull(group.getOwnHover());
        }

        @Test
        void formattingsAreShownWithoutTags() {
            PlayerMock admin = addAdmin("Admin");
            new UserInterface(user(admin)).openPageColorGroup(plugin.getGroupHandler().getGroup("default"));
            assertNoRawTags(admin);
            new UserInterface(user(admin)).openPageUserColors();
            assertNoRawTags(admin);
        }

        @Test
        void groupListHasBackButton() {
            PlayerMock admin = addAdmin("Admin");
            new UserInterface(user(admin)).openPageSetup();
            click(admin, GROUPS);
            // group list: 2 rows + frame
            assertEquals(Material.SPECTRAL_ARROW, itemAt(admin, 27));
            click(admin, 27);
            assertEquals(Material.CHEST, itemAt(admin, GROUPS));
        }

        @Test
        void revokedPermissionBlocksChanges() {
            PlayerMock admin = addPlayer("Admin");
            PermissionAttachment attachment = admin.addAttachment(plugin, "EasyPrefix.admin", true);
            new UserInterface(user(admin)).openPageSetup();
            click(admin, SETTINGS);

            admin.removeAttachment(attachment);
            click(admin, HANDLE_CHAT);
            assertTrue(plugin.getConfigData().getBoolean(ConfigData.Keys.HANDLE_CHAT), "setting changed without permission");
            assertFalse(hasGuiOpen(admin));
        }

        @Test
        void groupListShowsRenderedValues() {
            PlayerMock admin = addAdmin("Admin");
            var group = plugin.getGroupHandler().getGroup("default");
            group.setPrefix("%ep_tag_prefix% <gray>| <yellow>");
            group.setColor(Color.of("rainbow"));
            new UserInterface(user(admin)).openPageSetup();
            click(admin, GROUPS);
            String lore = java.util.Arrays.stream(admin.getOpenInventory().getTopInventory().getContents())
                    .filter(item -> item != null && item.lore() != null)
                    .map(item -> item.lore().stream().map(line -> PlainTextComponentSerializer.plainText().serialize(line))
                            .collect(Collectors.joining("\n")))
                    .filter(text -> text.contains("EasyPrefix.group.default")).findFirst().orElseThrow();
            assertTrue(lore.contains("Prefix: «%ep_tag_prefix% | »"), lore);
            assertTrue(lore.contains("Color: Rainbow"), lore);
            assertFalse(lore.contains("<gray>"), lore);
        }

        @Test
        void groupPrefixPreviewShowsWholeChatLine() {
            PlayerMock admin = addAdmin("Admin");
            var group = plugin.getGroupHandler().getGroup("default");
            group.setSuffix("<gray>:");
            FakeInput input = FakeInput.install(true);
            new UserInterface(user(admin)).openGroupProfile(group);
            // pattern "xabcxdehf" starts at slot 9
            click(admin, 10);
            // a prefix with only a color still shows the name and a message
            String preview = input.previewOf("<aqua>");
            assertTrue(preview.startsWith("Admin: Hello, this is what my messages look like!"), preview);
        }

        @Test
        void groupPreviewUsesColorOfTheGroup() {
            PlayerMock admin = addAdmin("Admin");
            user(admin).setColor(Color.of("green"));
            var group = plugin.getGroupHandler().getGroup("default");
            group.setColor(Color.of("red"));
            FakeInput input = FakeInput.install(true);
            new UserInterface(user(admin)).openGroupProfile(group);
            click(admin, 10);
            String preview = LegacyComponentSerializer.legacySection().serialize(input.previewComponent(""));
            assertTrue(preview.contains("§cHello"), preview);
        }

        @Test
        void hoverInputContainsCurrentLines() {
            PlayerMock admin = addAdmin("Admin");
            var group = plugin.getGroupHandler().getGroup("default");
            group.setHover(List.of("first", "second"));
            FakeInput input = FakeInput.install(true);
            new UserInterface(user(admin)).openGroupProfile(group);
            click(admin, 16);
            assertEquals("first|second", input.value);
            input.submit("one|two|three");
            assertEquals(List.of("one", "two", "three"), group.getHover());
        }
    }

    /**
     * the text input of the custom prefix - the dialog itself can't be shown by MockBukkit
     */
    @Nested
    class CustomPrefixInput {

        private FakeInput open(PlayerMock player, boolean showsPreview) {
            FakeInput input = FakeInput.install(showsPreview);
            new UserInterface(user(player)).showCustomPrefixGui();
            assertNotNull(input.consumer, "no input was opened");
            return input;
        }

        @Test
        void containsCurrentCustomPrefix() {
            PlayerMock player = addPlayer("Steve", "easyprefix.custom.prefix");
            user(player).setPrefix(CustomLayout.sanitize(user(player), "VIP 100% "));
            assertEquals("VIP 100% ", open(player, true).value);
        }

        @Test
        void containsGroupPrefixWithoutPlaceholders() {
            PlayerMock player = addPlayer("Steve", "easyprefix.custom.prefix");
            user(player).getGroup().setPrefix("<gray>[%ep_tag_prefix%Member] ");
            assertEquals("<gray>[Member] ", open(player, true).value);
        }

        @Test
        void savesWithoutConfirmationAfterPreview() {
            PlayerMock player = addPlayer("Steve", "easyprefix.custom.prefix");
            assertNull(open(player, true).submit("New "));
            assertEquals("New ", user(player).getPrefix());
            assertContains(messages(player), "Your prefix has been set");
        }

        @Test
        void rejectsBlockedWords() {
            PlayerMock player = addPlayer("Steve", "easyprefix.custom.prefix");
            String before = user(player).getPrefix();
            String error = open(player, true).submit("Admin ");
            assertNotNull(error);
            assertTrue(error.contains("forbidden words"), error);
            assertEquals(before, user(player).getPrefix());
        }

        @Test
        void chatInputAsksForConfirmation() {
            PlayerMock player = addPlayer("Steve", "easyprefix.custom.prefix");
            String before = user(player).getPrefix();
            open(player, false).submit("New ");
            assertEquals(before, user(player).getPrefix());
            assertContains(messages(player), "Do you want to set your prefix");
        }

        @Test
        void previewShowsChatLineWithNewPrefix() {
            PlayerMock player = addPlayer("Steve", "easyprefix.custom.prefix", "easyprefix.color.red");
            String preview = open(player, true).previewOf("&cVIP ");
            assertTrue(preview.startsWith("VIP Steve"), preview);
            assertTrue(preview.contains("Hello, this is what my messages look like!"), preview);
        }

        @Test
        void previewShowsForbiddenColorsAsText() {
            PlayerMock player = addPlayer("Steve", "easyprefix.custom.prefix");
            String preview = open(player, true).previewOf("&cVIP ");
            assertTrue(preview.startsWith("<red>VIP Steve"), preview);
        }
    }

    @AfterEach
    void resetInput() {
        UserInput.setFactory(null);
    }

    /**
     * remembers what the input was opened with, {@link #submit(String)} works like the save button of the dialog
     */
    static class FakeInput extends UserInput {
        private final boolean showsPreview;
        String title;
        String value;
        Consumer<String> consumer;

        FakeInput(boolean showsPreview) {
            this.showsPreview = showsPreview;
        }

        static FakeInput install(boolean showsPreview) {
            FakeInput input = new FakeInput(showsPreview);
            UserInput.setFactory(() -> input);
            return input;
        }

        @Override
        public boolean showsPreview() {
            return showsPreview;
        }

        @Override
        public void build(User user, String title, @Nullable String value, Consumer<String> consumer) {
            this.title = title;
            this.value = value;
            this.consumer = consumer;
        }

        /**
         * @return the error message if the text was not accepted
         */
        String submit(String text) {
            String error = validator.apply(text);
            if (error == null) consumer.accept(text);
            return error;
        }

        Component previewComponent(String text) {
            return preview.apply(text);
        }

        String previewOf(String text) {
            return PlainTextComponentSerializer.plainText().serialize(preview.apply(text));
        }
    }

}
