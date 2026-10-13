package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.user.CustomLayout;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.textinput.UserInput;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.regex.Pattern;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Consumer;

/**
 * EasyPrefix 2026.
 * <p>
 * All inventory menus: the player settings (/ep settings), the color menu and the setup for admins (/ep setup).
 *
 * @author Christian34
 */
@SuppressWarnings("DataFlowIssue")
public class UserInterface {
    private static final String DIVIDER = "§7-------------------------";
    private final String TITLE = Message.GUI_SETTINGS_TITLE.getText();
    private final User user;
    private final EasyPrefix instance;

    public UserInterface(User user) {
        this.user = user;
        this.instance = EasyPrefix.getInstance();
    }

    public void openPageSetup() {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), Message.SETUP_TITLE.getText(), "xxaxbxcxx");
        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.CHEST), click -> {
            openGroupsList();
            return true;
        }, Message.SETUP_BTN_GROUPS.getText()));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.NETHER_STAR), click -> {
            openSettingsPage();
            return true;
        }, Message.SETUP_BTN_SETTINGS.getText()));

        gui.addElement(new StaticGuiElement('c', new ItemStack(Material.WRITABLE_BOOK), click -> {
            openSubgroupsList();
            return true;
        }, Message.SETUP_BTN_TAGS.getText()));
        gui.show(user.getPlayer());
    }

    public void openUserSettings() {
        ConfigData config = instance.getConfigData();
        boolean tags = config.getBoolean(ConfigData.Keys.USE_TAGS) && !user.getAvailableSubgroups().isEmpty();
        boolean customLayout = config.getBoolean(ConfigData.Keys.CUSTOM_LAYOUT) && user.hasPermission("custom.gui");
        // buttons the player can't use are left out, the others are centered
        List<Character> buttons = new ArrayList<>(List.of('a', 'c'));
        if (tags) buttons.add('t');
        if (customLayout) buttons.add('l');
        boolean mentions = config.getBoolean(ConfigData.Keys.MENTIONS);
        if (mentions) buttons.add('m');
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_MAIN),
                Arrays.asList("    i    ", GuiCreator.centeredRow(buttons), "   v r   "));

        String none = Message.GUI_VALUE_NONE.getText();
        Map<String, String> values = new HashMap<>();
        values.put("group", user.getGroup().getGroupColor() + user.getGroup().getName());
        values.put("tag", user.getSubgroup() != null ? user.getSubgroup().getGroupColor() + user.getSubgroup().getName() : none);
        values.put("user_prefix", formatValue(ChatListener.formatPrefix(user), none));
        values.put("user_suffix", formatValue(ChatListener.formatSuffix(user), none));
        values.put("color", user.getColor() != null ? user.getColor().getDisplayName() : none);
        values.put("formatting", user.getDecoration() != null ? TextUtils.colorize(user.getDecoration().getDisplayName()) : none);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (head.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(user.getPlayer());
            head.setItemMeta(skullMeta);
        }
        gui.addElement(new StaticGuiElement('i', head, click -> true,
                withLore(Message.BTN_USER_INFO.get("player", user.getName()), Message.BTN_USER_INFO_LORE, values)));

        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.NAME_TAG), click -> {
            int userGroups = user.getAvailableGroups().size();
            if (userGroups <= 1 && config.getBoolean(ConfigData.Keys.USE_TAGS)) {
                openUserSubgroupsListPage();
            } else {
                openUserGroupsListPage();
            }
            return true;
        }, withLore(Message.BTN_MY_PREFIXES.getText(), Message.BTN_MY_PREFIXES_LORE, values)));

        ItemStack colorItem = user.getColor() != null ? ColorIcon.fromConfig().create(user.getColor()) : new ItemStack(Material.BOOKSHELF);
        gui.addElement(new StaticGuiElement('c', colorItem, click -> {
            openPageUserColors();
            return true;
        }, withLore(Message.BTN_MY_FORMATTINGS.getText(), Message.BTN_MY_FORMATTINGS_LORE, values)));

        if (tags) {
            gui.addElement(new StaticGuiElement('t', new ItemStack(Material.WRITABLE_BOOK), click -> {
                openUserSubgroupsListPage();
                return true;
            }, Message.BTN_SETTINGS_TAGS.getText(), DIVIDER, Message.GUI_SETTINGS_TITLE_TAGS.getText() + "§7: §f" + values.get("tag")));
        }

        if (customLayout) {
            List<String> layoutLore = new ArrayList<>();
            layoutLore.add(Message.BTN_CUSTOM_LAYOUT.getText());
            layoutLore.addAll(Message.BTN_CUSTOM_LAYOUT_LORE.getList());
            gui.addElement(new StaticGuiElement('l', new ItemStack(Material.ANVIL), click -> {
                openCustomLayoutPage();
                return true;
            }, layoutLore.toArray(new String[0])));
        }

        if (mentions) {
            boolean mentionable = user.isMentionable();
            Map<String, String> mentionValues = Map.of("player", user.getName(),
                    "status", (mentionable ? Message.GUI_VALUE_ENABLED : Message.GUI_VALUE_DISABLED).getText());
            ItemStack bell = new ItemStack(Material.BELL);
            if (mentionable) highlight(bell);
            gui.addElement(new StaticGuiElement('m', bell, click -> {
                user.setMentionable(!mentionable);
                openUserSettings();
                return true;
            }, withLore(Message.BTN_MENTIONS.getText(), Message.BTN_MENTIONS_LORE, mentionValues)));
        }

        gui.addElement(new StaticGuiElement('v', new ItemStack(Material.SPYGLASS), click -> {
            gui.close();
            ChatListener.sendPreview(user.getPlayer(), user);
            return true;
        }, withLore(Message.BTN_PREVIEW.getText(), Message.BTN_PREVIEW_LORE, values)));

        gui.addElement(new StaticGuiElement('r', new ItemStack(Material.BARRIER), click -> {
            user.setColor(null);
            user.setDecoration(null);
            // also stored ones that are not loaded (e.g. the custom layout is disabled), so they don't come back later
            user.setPrefix(null);
            user.setSuffix(null);
            user.sendMessage(Message.COLOR_RESET.getText());
            openUserSettings();
            return true;
        }, withLore(Message.BTN_RESET.getText(), Message.BTN_RESET_LORE, values)));

        if (user.hasPermission(UserPermission.ADMIN)) {
            gui.addElement(new StaticGuiElement('q', new ItemStack(Material.COMPARATOR), click -> {
                openSettingsPage();
                return true;
            }, withLore(Message.BTN_PLUGIN_SETTINGS.getText(), Message.BTN_PLUGIN_SETTINGS_LORE, values)));
        }

        gui.show(user.getPlayer());
    }

    public void showCustomPrefixGui() {
        if (!user.hasPermission(UserPermission.CUSTOM_PREFIX)) {
            user.sendMessage(Message.CHAT_NO_PERMS.getText());
            return;
        }
        Timestamp next = getNextTimestamp(user.getLastPrefixUpdate());
        if (!next.before(new Timestamp(System.currentTimeMillis())) && !user.hasPermission(UserPermission.CUSTOM_BYPASS)) {
            user.getPlayer().sendMessage(getTimeMessage(next));
            return;
        }

        UserInput input = UserInput.create()
                .preview(text -> previewLayout(CustomLayout.sanitize(user, text), user.getSuffix()));
        openCustomLayoutInput(input, Message.GUI_INPUT_PREFIX, user.getPrefix(), user.hasCustomPrefix(), Message.CHAT_INPUT_PREFIX_CONFIRM, prefix -> {
            user.setPrefix(prefix);
            user.saveData("custom_prefix_update", new Timestamp(System.currentTimeMillis()).toString());
            user.getPlayer().sendMessage(Message.CHAT_INPUT_PREFIX_SAVED.getText().replace("%content%", TextUtils.colorize(prefix)));
        });
    }

    public void showCustomSuffixGui() {
        if (!user.hasPermission(UserPermission.CUSTOM_SUFFIX)) {
            user.sendMessage(Message.CHAT_NO_PERMS.getText());
            return;
        }
        Timestamp next = getNextTimestamp(user.getLastSuffixUpdate());
        if (!next.before(new Timestamp(System.currentTimeMillis())) && !user.hasPermission(UserPermission.CUSTOM_BYPASS)) {
            user.getPlayer().sendMessage(getTimeMessage(next));
            return;
        }

        UserInput input = UserInput.create()
                .preview(text -> previewLayout(user.getPrefix(), CustomLayout.sanitize(user, text)));
        openCustomLayoutInput(input, Message.GUI_INPUT_SUFFIX, user.getSuffix(), user.hasCustomSuffix(), Message.CHAT_INPUT_SUFFIX_CONFIRM, suffix -> {
            user.setSuffix(suffix);
            user.saveData("custom_suffix_update", new Timestamp(System.currentTimeMillis()).toString());
            user.getPlayer().sendMessage(Message.CHAT_INPUT_SUFFIX_SAVED.getText().replace("%content%", TextUtils.colorize(suffix)));
        });
    }

    /**
     * asks for a custom prefix or suffix: the field contains the current one, blocked words are rejected. The chat
     * input has no preview, so the sanitized text has to be confirmed there.
     *
     * @param save gets the sanitized text
     */
    private void openCustomLayoutInput(UserInput input, Message title, @Nullable String current, boolean custom, Message confirm, Consumer<String> save) {
        // stored custom layouts are MiniMessage with escaped percent signs, group prefixes may contain §
        String value = current == null ? "" : current.replace(CustomLayout.PERCENT_TAG, "%").replace("§", "&");
        // placeholders of the group (e.g. %ep_tag_prefix%) would only be text in a custom layout
        if (!custom) value = value.replaceAll("%[^%\\s]+%", "");
        input.validate(text -> CustomLayout.isBlocked(user, text, CustomLayout.sanitize(user, text))
                        ? Message.CHATLAYOUT_INVALID.getText() : null)
                .build(user, title.getText(), value, text -> {
                    String sanitized = CustomLayout.sanitize(user, text);
                    if (input.showsPreview()) {
                        save.accept(sanitized);
                        return;
                    }
                    String question = confirm.getText().replace("%content%", TextUtils.colorize(sanitized));
                    new ChatButtonConfirm(user.getPlayer(), question, Message.CHAT_BTN_CONFIRM.getText())
                            .onClick(() -> save.accept(sanitized));
                });
    }

    /**
     * @return a chat line of the user with another prefix or suffix
     */
    private Component previewLayout(@Nullable String prefix, @Nullable String suffix) {
        return ChatListener.formatName(user, prefix, suffix).appendSpace()
                .append(ChatListener.formatMessage(user, Message.COLOR_PREVIEW_TEXT.getText()));
    }

    /**
     * @return a chat line as members of the group see it without an own chat color and formatting
     */
    private Component previewLayout(@Nullable String prefix, @Nullable String suffix, Group group) {
        return previewLayout(prefix, suffix, group, Message.COLOR_PREVIEW_TEXT);
    }

    private Component previewLayout(@Nullable String prefix, @Nullable String suffix, Group group, Message text) {
        return ChatListener.formatName(user, prefix, suffix).appendSpace()
                .append(ChatListener.formatMessage(user, text.getText(), group.getColor(), group.getDecoration()));
    }

    /**
     * @return the text as it looks (tags and colors rendered, placeholders as they are), "-" if there is none
     */
    private static String layoutValue(@Nullable String text) {
        if (text == null || text.isBlank()) return "-";
        return TextUtils.colorize(TextUtils.escapeLegacyColors(text));
    }

    /**
     * tags have no chat color of their own
     */
    private Group defaultGroup() {
        return instance.getGroupHandler().getGroup("default");
    }

    /**
     * @return a join or quit message of the user, like the join listener shows it
     */
    private Component previewMessage(String message) {
        String resolved = Optional.ofNullable(instance.setPlaceholders(user, message)).orElse("");
        return TextUtils.miniMessage().deserialize(TextUtils.escapeLegacyColors(resolved));
    }

    public void openPageUserColors() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_FORMATTINGS), Arrays.asList("a".repeat(9), "a".repeat(9), "b".repeat(9)));

        Collection<Color> colors = user.getColors();
        GuiElementGroup groupColors = new GuiElementGroup('a');
        ColorIcon colorIcon = ColorIcon.fromConfig();
        for (Color color : colors) {
            if (!user.hasPermission(color.getPermission())) continue;

            ItemStack itemStack = colorIcon.create(color);
            if (user.getColor() != null && user.getColor().equals(color)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemStack.setItemMeta(meta);
                }
            }

            groupColors.addElement(new StaticGuiElement('a', itemStack, click -> {
                if (user.hasPermission(color.getPermission())) {
                    user.setColor(color);
                    user.sendMessage(Message.COLOR_PLAYER_SELECT.getText().replace("%color%", user.getColor().getDisplayName()));
                    openPageUserColors();
                } else {
                    user.sendMessage(Message.CHAT_NO_PERMS.getText());
                }
                return true;
            }, "§r" + color.getDisplayName()));
        }

        Collection<Decoration> decorations = user.getDecorations();
        GuiElementGroup groupFormattings = new GuiElementGroup('b');

        for (Decoration decoration : decorations) {
            if (!user.hasPermission(decoration.getPermission())) continue;

            ItemStack itemStack = new ItemStack(Material.BOOKSHELF);
            if (user.getDecoration() != null && user.getDecoration().equals(decoration)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemStack.setItemMeta(meta);
                }
            }

            groupFormattings.addElement(new StaticGuiElement('b', itemStack, click -> {
                if (user.hasPermission(decoration.getPermission())) {
                    Decoration crntDecoration = decoration;
                    if (user.getDecoration() != null && user.getDecoration().equals(decoration)) {
                        crntDecoration = null;
                    }
                    user.setDecoration(crntDecoration);
                    user.sendMessage(Message.COLOR_PLAYER_SELECT.getText().replace("%color%", user.getColor().getDisplayName()));
                    openPageUserColors();
                } else {
                    user.sendMessage(Message.CHAT_NO_PERMS.getText());
                }
                return true;
            }, decorationTitle(decoration)));

        }

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
            user.setColor(null);
            user.setDecoration(null);
            openPageUserColors();
            return true;
        }, Message.BTN_RESET.getText(), " "));

        GuiCreator.addBackButton(gui, this::openUserSettings);
        gui.addElement(groupColors);
        gui.addElement(groupFormattings);
        gui.show(user.getPlayer());
    }

    public void openUserSubgroupsListPage() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_TAGS), Arrays.asList("aaaaaaaaa", "aaaaaaaaa"));
        GuiElementGroup elementGroup = new GuiElementGroup('a');

        final String loreSelectTag = Message.BTN_SETTINGS_SELECT_TAG.getText();
        for (Subgroup subgroup : user.getAvailableSubgroups()) {
            List<String> lore = new ArrayList<>();
            lore.add(subgroup.getGroupColor() + subgroup.getName());
            ItemStack bookItem = new ItemStack(Material.BOOK);

            if (user.getSubgroup() != null && user.getSubgroup().equals(subgroup)) {
                bookItem.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
                ItemMeta meta = bookItem.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    bookItem.setItemMeta(meta);
                }
            } else {
                lore.add(" ");
                lore.add(loreSelectTag);
            }

            elementGroup.addElement(new StaticGuiElement('b', bookItem, click -> {
                if (user.getSubgroup() != null && user.getSubgroup().equals(subgroup)) {
                    user.setSubgroup(null);
                } else {
                    user.setSubgroup(subgroup);
                }
                openUserSubgroupsListPage();
                return true;
            }, lore.toArray(new String[0])));
        }

        if (instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT) && user.hasPermission("custom.gui")) {
            gui.addElement(new StaticGuiElement('q', new ItemStack(Material.NETHER_STAR), click -> {
                openCustomLayoutPage();
                return true;
            }, Message.BTN_CUSTOM_LAYOUT.getText(), " "));
        }

        GuiCreator.addBackButton(gui, this::openUserSettings);
        gui.addElement(elementGroup);
        gui.show(user.getPlayer());
    }

    /**
     * the name of a formatting in its own style (e.g. bold), only obfuscated stays readable
     */
    private static String decorationTitle(Decoration decoration) {
        String style = decoration.getTextDecoration() == TextDecoration.OBFUSCATED ? "" : "<" + decoration.getName() + ">";
        return TextUtils.colorize("<white>" + style + decoration.getDisplayName());
    }

    private String getTimeMessage(Timestamp timestamp) {
        long min = (timestamp.getTime() - System.currentTimeMillis()) / 1000 / 60;
        int minutes = (int) (min % 60);
        int hours = (int) ((min / 60) % 24);
        String msg = Message.CHAT_LAYOUT_UPDATE_COOLDOWN.getText();
        return msg.replace("%h%", Integer.toString(hours)).replace("%m%", (minutes == 0) ? "<1" : Integer.toString(minutes));
    }

    private Timestamp getNextTimestamp(long last) {
        double delay = instance.getConfigData().getDouble(ConfigData.Keys.CUSTOM_LAYOUT_COOLDOWN);
        long newTime = (long) (last + (delay * 60 * 60 * 1000));
        return new Timestamp(newTime);
    }

    private void openGroupsList() {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), Message.SETUP_TITLE_GROUPS.getText(), Arrays.asList("aaaaaaaaa", "aaaaaaaaa"));
        GuiElementGroup elementGroup = new GuiElementGroup('a');

        for (Group group : instance.getGroupHandler().getGroups()) {
            elementGroup.addElement(new StaticGuiElement('b', new ItemStack(Material.CHEST), click -> {
                openGroupProfile(group);
                return true;
            }, Message.SETUP_GROUP.lines(
                    "name", group.getGroupColor() + group.getName(),
                    "group_prefix", layoutValue(group.getPrefix()),
                    "group_suffix", layoutValue(group.getSuffix()),
                    // a short text, the tooltip would get too wide otherwise
                    "preview", TextUtils.serialize(previewLayout(group.getPrefix(), group.getSuffix(), group, Message.SETUP_GROUP_PREVIEW_TEXT)),
                    "color", group.getColor().getDisplayName(),
                    "group", group.getName())));
        }
        gui.addElement(elementGroup);

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.NETHER_STAR), click -> {
            openGroupCreator();
            return true;
        }, Message.SETUP_BTN_ADD_GROUP.getText()));

        GuiCreator.addBackButton(gui, this::openPageSetup);
        gui.show(user.getPlayer());
    }

    private void openCustomLayoutPage() {
        if (!instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT) || !user.hasPermission("custom.gui")) {
            openUserSettings();
            return;
        }

        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_LAYOUT), "xxxaxbxxx");
        List<String> lorePrefix = new ArrayList<>();
        lorePrefix.add(Message.BTN_CHANGE_PREFIX.getText());
        lorePrefix.addAll(replaceInList(Message.LORE_CHANGE_PREFIX.getList(), Optional.ofNullable(user.getPrefix()).orElse("-")));
        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.IRON_INGOT), click -> {
            showCustomPrefixGui();
            return true;
        }, lorePrefix.toArray(new String[0])));

        List<String> loreSuffix = new ArrayList<>();
        loreSuffix.add(Message.BTN_CHANGE_SUFFIX.getText());
        loreSuffix.addAll(replaceInList(Message.LORE_CHANGE_SUFFIX.getList(), Optional.ofNullable(user.getSuffix()).orElse("-")));
        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.GOLD_INGOT), click -> {
            showCustomSuffixGui();
            return true;
        }, loreSuffix.toArray(new String[0])));

        GuiCreator.addBackButton(gui, this::openUserSettings);
        gui.show(user.getPlayer());
    }

    private void openSettingsPage() {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), Message.SETUP_TITLE_SETTINGS.getText(),
                Arrays.asList(" a b c d ", " e f g h ", " n o p q ", " i j m k "));
        ConfigData config = this.instance.getConfigData();

        gui.addElement(toggleElement('a', Material.OAK_SIGN, Message.SETUP_TOGGLE_HANDLE_CHAT, ConfigData.Keys.HANDLE_CHAT));

        ColorIcon colorIcon = ColorIcon.fromConfig();
        gui.addElement(new StaticGuiElement('b', colorIcon.create(sampleColor()), click -> {
            openColorIconPage();
            return true;
        }, Message.SETUP_BTN_COLOR_ICONS.lines("current", colorIcon.getDisplayName())));

        gui.addElement(toggleElement('c', Material.NAME_TAG, Message.SETUP_TOGGLE_TAGS, ConfigData.Keys.USE_TAGS));
        gui.addElement(toggleElement('d', Material.OAK_DOOR, Message.SETUP_TOGGLE_JOIN_QUIT, ConfigData.Keys.USE_JOIN_QUIT));
        gui.addElement(toggleElement('n', Material.PAINTING, Message.SETUP_TOGGLE_TAB_LIST, ConfigData.Keys.DISPLAY_TAB_LIST));
        gui.addElement(toggleElement('o', Material.LADDER, Message.SETUP_TOGGLE_SORT_TAB_LIST, ConfigData.Keys.DISPLAY_SORT_TAB_LIST));
        gui.addElement(toggleElement('p', Material.NAME_TAG, Message.SETUP_TOGGLE_NAME_TAGS, ConfigData.Keys.DISPLAY_NAME_TAGS));
        gui.addElement(toggleElement('q', Material.BELL, Message.SETUP_TOGGLE_MENTIONS, ConfigData.Keys.MENTIONS));
        gui.addElement(toggleElement('e', Material.IRON_DOOR, Message.SETUP_TOGGLE_HIDE_JOIN_QUIT, ConfigData.Keys.HIDE_JOIN_QUIT));
        gui.addElement(toggleElement('f', Material.ANVIL, Message.SETUP_TOGGLE_CUSTOM_LAYOUT, ConfigData.Keys.CUSTOM_LAYOUT));

        double cooldown = config.getDouble(ConfigData.Keys.CUSTOM_LAYOUT_COOLDOWN);
        gui.addElement(new StaticGuiElement('g', new ItemStack(Material.CLOCK), click -> {
            if (!isAdmin()) return true;
            double step = click.getType().isShiftClick() ? 5 : 0.5;
            if (click.getType().isRightClick()) step = -step;
            double value = Math.max(0, Math.round((cooldown + step) * 10) / 10.0);
            config.save(ConfigData.Keys.CUSTOM_LAYOUT_COOLDOWN, value);
            openSettingsPage();
            return true;
        }, Message.SETUP_BTN_COOLDOWN.lines("current", formatHours(cooldown))));

        List<String> blacklist = config.getList(ConfigData.Keys.CUSTOM_LAYOUT_BLACKLIST);
        List<String> words = new ArrayList<>();
        blacklist.stream().limit(8).forEach(word -> words.add(Message.SETUP_BLACKLIST_WORD.get("word", word.replace("§", "&"))));
        if (blacklist.size() > 8) words.add(Message.SETUP_BLACKLIST_MORE.get("amount", Integer.toString(blacklist.size() - 8)));
        if (blacklist.isEmpty()) words.add(Message.SETUP_BLACKLIST_EMPTY.getText());
        gui.addElement(new StaticGuiElement('h', new ItemStack(Material.IRON_BARS), click -> {
            openBlacklistPage();
            return true;
        }, Message.SETUP_BTN_BLACKLIST.lines("words", String.join("\n", words))));

        gui.addElement(aliasElement('i', Material.IRON_INGOT, Message.SETUP_ALIAS_PREFIX, "prefix", ConfigData.Keys.PREFIX_ALIAS));
        gui.addElement(aliasElement('j', Material.GOLD_INGOT, Message.SETUP_ALIAS_SUFFIX, "suffix", ConfigData.Keys.SUFFIX_ALIAS));

        boolean sql = config.getBoolean(ConfigData.Keys.SQL_ENABLED);
        gui.addElement(new StaticGuiElement('m', new ItemStack(Material.ENDER_CHEST), click -> true,
                Message.SETUP_BTN_STORAGE.lines("current", (sql ? Message.SETUP_STORAGE_MYSQL : Message.SETUP_STORAGE_LOCAL).getText())));

        gui.addElement(new StaticGuiElement('k', new ItemStack(Material.RECOVERY_COMPASS), click -> {
            gui.close();
            if (!isAdmin()) return true;
            instance.reload();
            user.sendAdminMessage(Message.CMD_RELOADED);
            return true;
        }, Message.SETUP_BTN_RELOAD.lines()));

        GuiCreator.addBackButton(gui, this::openPageSetup);

        gui.show(user.getPlayer());
    }

    /**
     * @param text the name of the setting, followed by its description
     */
    private StaticGuiElement toggleElement(char slot, Material material, Message text, String key) {
        ConfigData config = this.instance.getConfigData();
        boolean enabled = config.getBoolean(key);

        List<String> lore = text.getList();
        lore.addAll((enabled ? Message.SETUP_TOGGLE_ENABLED : Message.SETUP_TOGGLE_DISABLED).getList());

        ItemStack itemStack = new ItemStack(material);
        if (enabled) highlight(itemStack);
        return new StaticGuiElement(slot, itemStack, click -> {
            if (!isAdmin()) return true;
            config.save(key, !enabled);
            // tab list and name tags depend on the config
            instance.getDisplayManager().start();
            openSettingsPage();
            return true;
        }, lore.toArray(new String[0]));
    }

    /**
     * @param command "prefix" or "suffix", as in the permission EasyPrefix.custom.prefix
     */
    private StaticGuiElement aliasElement(char slot, Material material, Message type, String command, String key) {
        ConfigData config = this.instance.getConfigData();
        String alias = config.getString(key, "");
        return new StaticGuiElement(slot, new ItemStack(material), click -> {
            String title = Message.SETUP_INPUT_ALIAS.get("type", type.getText(), "command", command);
            UserInput.create().build(user, title, alias.isEmpty() ? "-" : alias, (input) -> {
                if (!isAdmin()) return;
                String value = input.trim().replace("/", "").replaceAll("[^a-zA-Z0-9_-]", "");
                config.save(key, value.isEmpty() ? "" : "/" + value);
                user.sendAdminMessage(Message.SETUP_ALIAS_CHANGED);
                openSettingsPage();
            });
            return true;
        }, Message.SETUP_BTN_ALIAS.lines("type", type.getText(), "command", command,
                "current", alias.isEmpty() ? Message.SETUP_ALIAS_DISABLED.getText() : alias));
    }

    private void openColorIconPage() {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), Message.SETUP_TITLE_COLOR_ICONS.getText(),
                Arrays.asList("  aaaaaa ", "         ", "bbbbbbbbb", "bbbbbbbbb"));
        ConfigData config = this.instance.getConfigData();
        ColorIcon current = ColorIcon.fromConfig();

        GuiElementGroup icons = new GuiElementGroup('a');
        for (ColorIcon icon : ColorIcon.values()) {
            ItemStack itemStack = icon.create(sampleColor());
            boolean selected = icon == current;
            if (selected) highlight(itemStack);
            icons.addElement(new StaticGuiElement('a', itemStack, click -> {
                if (!isAdmin()) return true;
                config.save(ConfigData.Keys.COLOR_ICON, icon.getConfigValue());
                openColorIconPage();
                return true;
            }, Message.SETUP_COLOR_ICON.lines("name", icon.getDisplayName(), "description", icon.getDescription(),
                    "status", (selected ? Message.SETUP_COLOR_ICON_SELECTED : Message.SETUP_COLOR_ICON_SELECT).getText())));
        }

        GuiElementGroup preview = new GuiElementGroup('b');
        for (Color color : instance.getColors()) {
            preview.addElement(new StaticGuiElement('b', current.create(color), click -> true,
                    "§r" + color.getDisplayName(), Message.SETUP_COLOR_ICON_PREVIEW.getText()));
        }

        gui.addElement(icons);
        gui.addElement(preview);
        GuiCreator.addBackButton(gui, this::openSettingsPage);
        gui.show(user.getPlayer());
    }

    private void openBlacklistPage() {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), Message.SETUP_TITLE_BLACKLIST.getText(),
                Arrays.asList("aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa"));
        ConfigData config = this.instance.getConfigData();
        List<String> blacklist = config.getList(ConfigData.Keys.CUSTOM_LAYOUT_BLACKLIST);

        GuiElementGroup entries = new GuiElementGroup('a');
        for (String word : blacklist) {
            entries.addElement(new StaticGuiElement('a', new ItemStack(Material.PAPER), click -> {
                if (!isAdmin()) return true;
                List<String> updated = new ArrayList<>(blacklist);
                updated.remove(word);
                config.save(ConfigData.Keys.CUSTOM_LAYOUT_BLACKLIST, updated);
                openBlacklistPage();
                return true;
            }, Message.SETUP_BLACKLIST_ENTRY.lines("word", word.replace("§", "&"))));
        }
        gui.addElement(entries);

        GuiCreator.addBackButton(gui, this::openSettingsPage);

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.NETHER_STAR), click -> {
            UserInput.create().build(user, Message.SETUP_INPUT_BLACKLIST.getText(), "", (input) -> {
                if (!isAdmin()) return;
                String word = input.trim();
                List<String> updated = new ArrayList<>(config.getList(ConfigData.Keys.CUSTOM_LAYOUT_BLACKLIST));
                if (!word.isEmpty() && updated.stream().noneMatch(word::equalsIgnoreCase)) {
                    updated.add(word);
                    config.save(ConfigData.Keys.CUSTOM_LAYOUT_BLACKLIST, updated);
                }
                openBlacklistPage();
            });
            return true;
        }, Message.SETUP_BTN_ADD_WORD.getText()));

        gui.show(user.getPlayer());
    }

    /**
     * @return a color to show the color icons with
     */
    private Color sampleColor() {
        Color red = Color.of("red");
        if (red != null) return red;
        return instance.getColors().stream().findFirst().orElseThrow();
    }

    private static ItemStack highlight(ItemStack itemStack) {
        itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return itemStack;
    }

    private static String formatHours(double hours) {
        if (hours <= 0) return Message.SETUP_COOLDOWN_NONE.getText();
        if (hours < 1) return Message.SETUP_COOLDOWN_MINUTES.get("amount", Long.toString(Math.round(hours * 60)));
        return Message.SETUP_COOLDOWN_HOURS.get("amount", hours == Math.floor(hours) ? Long.toString((long) hours) : Double.toString(hours));
    }

    /**
     * @return the text with legacy colors for the lore, the alternative if nothing would be visible
     */
    private static String formatValue(Component text, String alternative) {
        if (PlainTextComponentSerializer.plainText().serialize(text).isBlank()) return alternative;
        return LegacyComponentSerializer.legacySection().serialize(text);
    }

    private static String[] withLore(String name, Message lore, Map<String, String> values) {
        List<String> lines = new ArrayList<>();
        lines.add(name);
        for (String line : lore.getList()) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                line = line.replace("%" + entry.getKey() + "%", entry.getValue());
            }
            lines.add(line);
        }
        return lines.toArray(new String[0]);
    }

    private void openProfile(EasyGroup easyGroup) {
        if (easyGroup instanceof Group) {
            openGroupProfile((Group) easyGroup);
        } else {
            openSubgroupProfile((Subgroup) easyGroup);
        }
    }

    private void openPageDeleteGroup(EasyGroup easyGroup) {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), Message.SETUP_TITLE_DELETE.get("name", easyGroup.getName()), "xxxaxbxxx");
        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.GREEN_TERRACOTTA), click -> {
            if (!isAdmin()) return true;
            easyGroup.delete();
            if (easyGroup instanceof Group) openGroupsList();
            else openSubgroupsList();
            return true;
        }, Message.SETUP_BTN_YES.getText()));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.RED_TERRACOTTA), click -> {
            openProfile(easyGroup);
            return true;
        }, Message.SETUP_BTN_NO.getText()));

        gui.show(user.getPlayer());
    }

    private void openUserGroupsListPage() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_LAYOUT), Arrays.asList("aaaaaaaaa", "aaaaaaaaa"));
        GuiElementGroup elementGroup = new GuiElementGroup('a');

        final List<String> defaultLore = Message.BTN_SELECT_PREFIX_LORE.getList();

        for (Group group : user.getAvailableGroups()) {
            String prefixColor = group.getGroupColor();
            List<String> lore = new ArrayList<>();
            lore.add(prefixColor + group.getName());
            ItemStack itemStack = new ItemStack(Material.BOOK);

            if (user.getGroup().equals(group)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemStack.setItemMeta(meta);
                }
            }
            String layout = TextUtils.colorize(group.getPrefix() + user.getName() + group.getSuffix());
            for (String line : defaultLore) {
                line = line.replace("%LAYOUT%", layout);
                lore.add(line);
            }

            elementGroup.addElement(new StaticGuiElement('b', itemStack, click -> {
                user.setGroup(group, false);
                openUserGroupsListPage();
                return true;
            }, lore.toArray(new String[0])));
        }

        if (!user.getAvailableSubgroups().isEmpty()) {
            ItemStack subgroupsMaterial = new ItemStack(Material.WRITABLE_BOOK);
            gui.addElement(new StaticGuiElement('w', subgroupsMaterial, click -> {
                openUserSubgroupsListPage();
                return true;
            }, Message.BTN_SETTINGS_TAGS.getText(), " "));
        }

        if (instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT) && user.hasPermission("custom.gui")) {
            gui.addElement(new StaticGuiElement('q', new ItemStack(Material.NETHER_STAR), click -> {
                openCustomLayoutPage();
                return true;
            }, Message.BTN_CUSTOM_LAYOUT.getText(), " "));
        }

        GuiCreator.addBackButton(gui, this::openUserSettings);
        gui.addElement(elementGroup);
        gui.show(user.getPlayer());
    }

    void openGroupProfile(Group group) {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), Message.SETUP_TITLE_GROUP.get("name", group.getGroupColor() + group.getName()), "xabcxdehf");

        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.IRON_INGOT), click -> {
            String prefix = group.getPrefix();
            prefix = prefix == null ? "" : prefix.replace("§", "&");
            UserInput.create().preview(text -> previewLayout(text, group.getSuffix(), group)).build(user, Message.SETUP_INPUT_PREFIX.get("name", group.getName()), prefix, (input) -> {
                if (!isAdmin()) return;
                group.setPrefix(input);
                user.getPlayer().sendMessage(Message.INPUT_SAVED.getText());
                openGroupProfile(group);
            });
            return true;
        }, Message.SETUP_BTN_PREFIX.lines("current", layoutValue(group.getPrefix()))));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.GOLD_INGOT), click -> {
            String suffix = group.getSuffix();
            suffix = suffix == null ? "" : suffix.replace("§", "&");
            UserInput.create().preview(text -> previewLayout(group.getPrefix(), text, group)).build(user, Message.SETUP_INPUT_SUFFIX.get("name", group.getName()), suffix, (input) -> {
                if (!isAdmin()) return;
                group.setSuffix(input);
                user.sendMessage(Message.INPUT_SAVED.getText());
                openGroupProfile(group);
            });
            return true;
        }, Message.SETUP_BTN_SUFFIX.lines("current", layoutValue(group.getSuffix()))));

        gui.addElement(new StaticGuiElement('c', new ItemStack(Material.LIME_DYE), click -> {
            openPageColorGroup(group);
            return true;
        }, Message.SETUP_BTN_COLOR.lines("current", group.getColor().getDisplayName())));

        gui.addElement(new StaticGuiElement('d', new ItemStack(Material.BLAZE_ROD), click -> {
            String joinMsg = group.getJoinMessage();
            joinMsg = joinMsg == null ? "" : joinMsg.replace("§", "&");
            UserInput.create().preview(this::previewMessage).build(user, Message.SETUP_INPUT_JOIN_MESSAGE.get("name", group.getName()), joinMsg, (input) -> {
                if (!isAdmin()) return;
                group.setJoinMessage(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
                openGroupProfile(group);
            });
            return true;
        }, Message.SETUP_BTN_JOIN_MESSAGE.lines("current", layoutValue(group.getJoinMessage()))));

        gui.addElement(new StaticGuiElement('e', new ItemStack(Material.STICK), click -> {
            String quitMsg = group.getQuitMessage();
            quitMsg = quitMsg == null ? "" : quitMsg.replace("§", "&");
            UserInput.create().preview(this::previewMessage).build(user, Message.SETUP_INPUT_QUIT_MESSAGE.get("name", group.getName()), quitMsg, (input) -> {
                if (!isAdmin()) return;
                group.setQuitMessage(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
                openGroupProfile(group);
            });
            return true;
        }, Message.SETUP_BTN_QUIT_MESSAGE.lines("current", layoutValue(group.getQuitMessage()))));

        gui.addElement(new StaticGuiElement('f', new ItemStack(Material.EXPERIENCE_BOTTLE), click -> {
            if (!isAdmin()) return true;
            int step = click.getType().isShiftClick() ? 10 : 1;
            if (click.getType().isRightClick()) step = -step;
            group.setPriority(group.getPriority() + step);
            openGroupProfile(group);
            return true;
        }, Message.SETUP_BTN_PRIORITY.lines("current", Integer.toString(group.getPriority()))));

        gui.addElement(hoverElement(group));

        if (!group.getName().equals("default")) {
            gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
                openPageDeleteGroup(group);
                return true;
            }, Message.SETUP_BTN_DELETE.getText(), " "));
        }

        GuiCreator.addBackButton(gui, this::openGroupsList);
        gui.show(user.getPlayer());
    }

    /**
     * the hover text of the name in the chat: groups without their own one use the one of the default group
     */
    private StaticGuiElement hoverElement(Group group) {
        boolean isDefault = group.getName().equals("default");
        boolean inherited = group.getOwnHover() == null;
        List<String> hover = group.getHover();
        List<String> shown = new ArrayList<>();
        hover.stream().limit(10).forEach(line -> shown.add(Message.SETUP_HOVER_LINE.get("line", Message.setColors(line))));
        if (hover.size() > 10) shown.add(Message.SETUP_HOVER_MORE.getText());
        Message current = !inherited ? Message.SETUP_HOVER_CURRENT
                : isDefault ? Message.SETUP_HOVER_CURRENT_BUILTIN : Message.SETUP_HOVER_CURRENT_DEFAULT;
        String[] lore = Message.SETUP_BTN_HOVER.lines("current", current.getText(), "lines", String.join("\n", shown),
                "reset", (isDefault ? Message.SETUP_HOVER_RESET_BUILTIN : Message.SETUP_HOVER_RESET_DEFAULT).getText());

        return new StaticGuiElement('h', new ItemStack(Material.OAK_SIGN), click -> {
            if (!isAdmin()) return true;
            if (click.getType().isShiftClick()) {
                group.setHover(null);
                user.sendAdminMessage(Message.INPUT_SAVED);
                openGroupProfile(group);
            } else if (click.getType().isRightClick()) {
                UserInput.create().build(user, Message.SETUP_INPUT_HOVER_LINE.getText(), "", (input) -> {
                    if (!isAdmin()) return;
                    List<String> lines = new ArrayList<>(group.getHover());
                    lines.add(input);
                    group.setHover(lines);
                    user.sendAdminMessage(Message.INPUT_SAVED);
                    openGroupProfile(group);
                });
            } else {
                UserInput.create().build(user, Message.SETUP_INPUT_HOVER_LINES.getText(), String.join("|", hover), (input) -> {
                    if (!isAdmin()) return;
                    group.setHover(List.of(input.split(Pattern.quote("|"), -1)));
                    user.sendAdminMessage(Message.INPUT_SAVED);
                    openGroupProfile(group);
                });
            }
            return true;
        }, lore);
    }

    private void openSubgroupsList() {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), Message.SETUP_TITLE_TAGS.getText(), Arrays.asList("aaaaaaaaa", "aaaaaaaaa"));
        GuiElementGroup elementGroup = new GuiElementGroup('a');

        for (final Subgroup subgroup : this.instance.getGroupHandler().getSubgroups()) {
            ItemStack sgBtn = new ItemStack(Material.WRITABLE_BOOK);
            elementGroup.addElement(new StaticGuiElement('b', sgBtn, click -> {
                openSubgroupProfile(subgroup);
                return true;
            }, Message.SETUP_TAG.lines(
                    "name", subgroup.getGroupColor() + subgroup.getName(),
                    "tag_prefix", layoutValue(subgroup.getPrefix()),
                    "tag_suffix", layoutValue(subgroup.getSuffix()),
                    "tag", subgroup.getName())));
        }

        gui.addElement(elementGroup);

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.NETHER_STAR), click -> {
            openTagCreator();
            return true;
        }, Message.SETUP_BTN_ADD_TAG.getText()));
        GuiCreator.addBackButton(gui, this::openPageSetup);
        gui.show(user.getPlayer());
    }

    void openPageColorGroup(Group group) {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), group.getGroupColor() + group.getName() + " §8» " + Message.GUI_SETTINGS_TITLE_FORMATTINGS.getText(), Arrays.asList("a".repeat(9), "a".repeat(9), "b".repeat(9)));

        GuiElementGroup groupColors = new GuiElementGroup('a');
        ColorIcon colorIcon = ColorIcon.fromConfig();
        for (Color color : EasyPrefix.getInstance().getColors()) {
            ItemStack itemStack = colorIcon.create(color);
            if (group.getColor().equals(color)) highlight(itemStack);

            groupColors.addElement(new StaticGuiElement('a', itemStack, click -> {
                if (!isAdmin()) return true;
                group.setColor(color);
                openPageColorGroup(group);
                return true;
            }, "§r" + color.getDisplayName(), " ", permissionLine(color.getPermission())));
        }

        GuiElementGroup groupFormattings = new GuiElementGroup('b');
        for (Decoration decoration : instance.getDecorations()) {
            ItemStack itemStack = new ItemStack(Material.BOOKSHELF);
            if (group.getDecoration() != null && group.getDecoration().equals(decoration)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemStack.setItemMeta(meta);
                }
            }

            groupFormattings.addElement(new StaticGuiElement('b', itemStack, click -> {
                if (!isAdmin()) return true;
                Decoration deco = decoration;
                if (group.getDecoration() != null && group.getDecoration().equals(decoration)) {
                    deco = null;
                }
                group.setDecoration(deco);
                openPageColorGroup(group);
                return true;
            }, decorationTitle(decoration), " ", permissionLine(decoration.getPermission())));

        }

        // a group always needs a color, so there is no reset here (the formatting can be deselected by clicking it again)
        GuiCreator.addBackButton(gui, () -> openGroupProfile(group));
        gui.addElement(groupColors);
        gui.addElement(groupFormattings);
        gui.show(user.getPlayer());
    }

    private static String permissionLine(@Nullable Permission permission) {
        return permission == null ? "" : Message.SETUP_COLOR_PERMISSION.get("permission", permission.getName());
    }

    private void openSubgroupProfile(Subgroup subgroup) {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), Message.SETUP_TITLE_TAG.get("name", subgroup.getGroupColor() + subgroup.getName()), " a b   f ");

        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.IRON_INGOT), click -> {
            String prefix = subgroup.getPrefix();
            prefix = prefix == null ? "" : prefix.replace("§", "&");
            UserInput.create().preview(text -> previewLayout(text, subgroup.getSuffix(), defaultGroup())).build(user, Message.SETUP_INPUT_PREFIX.get("name", subgroup.getName()), prefix, (input) -> {
                if (!isAdmin()) return;
                subgroup.setPrefix(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
                openSubgroupProfile(subgroup);
            });
            return true;
        }, Message.SETUP_BTN_PREFIX.lines("current", layoutValue(subgroup.getPrefix()))));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.GOLD_INGOT), click -> {
            String suffix = subgroup.getSuffix();
            suffix = suffix == null ? "" : suffix.replace("§", "&");
            UserInput.create().preview(text -> previewLayout(subgroup.getPrefix(), text, defaultGroup())).build(user, Message.SETUP_INPUT_SUFFIX.get("name", subgroup.getName()), suffix, (input) -> {
                if (!isAdmin()) return;
                subgroup.setSuffix(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
                openSubgroupProfile(subgroup);
            });
            return true;
        }, Message.SETUP_BTN_SUFFIX.lines("current", layoutValue(subgroup.getSuffix()))));

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
            openPageDeleteGroup(subgroup);
            return true;
        }, Message.SETUP_BTN_DELETE.getText(), " "));

        GuiCreator.addBackButton(gui, this::openSubgroupsList);
        gui.show(user.getPlayer());
    }

    private void openGroupCreator() {
        if (!isAdmin()) return;
        UserInput.create().build(user, Message.SETUP_INPUT_GROUP_NAME.getText(), "ExampleGroup", (input) -> {
            if (!isAdmin()) return;
            String name = input.replaceAll("[^a-zA-Z0-9_]", "");
            if (this.instance.getGroupHandler().createGroup(name)) {
                user.sendAdminMessage(Message.SETUP_GROUP_CREATED, "name", name);
            } else {
                user.sendAdminMessage(Message.SETUP_GROUP_NOT_CREATED);
            }
            openGroupsList();
        });
    }

    private void openTagCreator() {
        if (!isAdmin()) return;
        UserInput.create().build(user, Message.SETUP_INPUT_TAG_NAME.getText(), "ExampleTag", (input) -> {
            if (!isAdmin()) return;
            String name = input.replaceAll("[^a-zA-Z0-9_]", "");
            if (this.instance.getGroupHandler().createSubgroup(name)) {
                user.sendAdminMessage(Message.SETUP_TAG_CREATED, "name", name);
            } else {
                user.sendAdminMessage(Message.SETUP_TAG_NOT_CREATED);
            }
            openSubgroupsList();
        });
    }

    /**
     * the setup pages can be reopened later (e.g. after a chat input), so the permission is checked on every page
     */
    private boolean isAdmin() {
        if (user.hasPermission(UserPermission.ADMIN)) return true;
        user.getPlayer().closeInventory();
        user.sendMessage(Message.CHAT_NO_PERMS.getText());
        return false;
    }

    private String setTitle(Message sub) {
        return TITLE.replace("%page%", sub.getText());
    }

    private List<String> replaceInList(@NotNull List<String> list, @NotNull String value) {
        return list.stream().map(val -> val.replace("%content%", value)).collect(Collectors.toList());
    }

}
