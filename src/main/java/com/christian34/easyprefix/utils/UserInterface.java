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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.regex.Pattern;
import java.util.*;
import java.util.stream.Collectors;

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
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9EasyPrefix §8» §8Configuration", "xxaxbxcxx");
        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.CHEST), click -> {
            openGroupsList();
            return true;
        }, "§9Groups"));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.NETHER_STAR), click -> {
            openSettingsPage();
            return true;
        }, "§9Settings"));

        gui.addElement(new StaticGuiElement('c', new ItemStack(Material.WRITABLE_BOOK), click -> {
            openSubgroupsList();
            return true;
        }, "§9Tags §8(Subgroups)"));
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

        UserInput.create().build(user, Message.GUI_INPUT_PREFIX.getText(), user.getPrefix(), (input) -> {
            String prefix = CustomLayout.sanitize(user, input);
            if (CustomLayout.isBlocked(user, input, prefix)) {
                user.getPlayer().sendMessage(Message.CHATLAYOUT_INVALID.getText());
                return;
            }

            String text = Message.CHAT_INPUT_PREFIX_CONFIRM.getText().replace("%content%", TextUtils.colorize(prefix));
            ChatButtonConfirm chatButtonConfirm = new ChatButtonConfirm(user.getPlayer(), text, Message.CHAT_BTN_CONFIRM.getText());
            chatButtonConfirm.onClick(() -> {
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                user.setPrefix(prefix);
                user.saveData("custom_prefix_update", currentTime.toString());
                user.getPlayer().sendMessage(Message.CHAT_INPUT_PREFIX_SAVED.getText().replace("%content%", TextUtils.colorize(prefix)));
            });
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

        UserInput.create().build(user, Message.GUI_INPUT_SUFFIX.getText(), user.getSuffix(), (input) -> {
            String suffix = CustomLayout.sanitize(user, input);
            if (CustomLayout.isBlocked(user, input, suffix)) {
                user.getPlayer().sendMessage(Message.CHATLAYOUT_INVALID.getText());
                return;
            }

            String text = Message.CHAT_INPUT_SUFFIX_CONFIRM.getText().replace("%content%", TextUtils.colorize(suffix));
            ChatButtonConfirm chatButtonConfirm = new ChatButtonConfirm(user.getPlayer(), text, Message.CHAT_BTN_CONFIRM.getText());
            chatButtonConfirm.onClick(() -> {
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                user.setSuffix(suffix);
                user.saveData("custom_suffix_update", currentTime.toString());
                user.getPlayer().sendMessage(Message.CHAT_INPUT_SUFFIX_SAVED.getText().replace("%content%", TextUtils.colorize(suffix)));
            });
        });
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
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9EasyPrefix §8» §8Groups", Arrays.asList("aaaaaaaaa", "aaaaaaaaa"));
        GuiElementGroup elementGroup = new GuiElementGroup('a');

        for (Group group : instance.getGroupHandler().getGroups()) {
            String prefix = Optional.ofNullable(group.getPrefix()).orElse("-");
            String suffix = Optional.ofNullable(group.getSuffix()).orElse("-");
            String prefixColor = group.getGroupColor();
            List<String> lore = new ArrayList<>();
            lore.add(prefixColor + group.getName());
            lore.add("§7-------------------------------");
            if (prefix.length() > 25) {
                lore.add("§7Prefix: §7«§f" + prefix.substring(0, 25));
                lore.add("§f" + prefix.substring(25) + "§7»");
            } else {
                lore.add("§7Prefix: §7«§f" + prefix + "§7»");
            }
            lore.add("§7Suffix: §7«§f" + suffix + "§7»");

            lore.add("§7Color: §f" + group.getColor().getDisplayName());
            lore.add("§7Permission: §fEasyPrefix.group." + group.getName());
            elementGroup.addElement(new StaticGuiElement('b', new ItemStack(Material.CHEST), click -> {
                openGroupProfile(group);
                return true;
            }, lore.toArray(new String[0])));
        }
        gui.addElement(elementGroup);

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.NETHER_STAR), click -> {
            openGroupCreator();
            return true;
        }, "§aAdd Group"));

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
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9EasyPrefix §8» §8Settings",
                Arrays.asList(" a b c d ", " e f g h ", " n o p   ", " i j m k "));
        ConfigData config = this.instance.getConfigData();

        gui.addElement(toggleElement('a', Material.OAK_SIGN, "Handle Chat", ConfigData.Keys.HANDLE_CHAT,
                "Formats the chat with prefix, suffix and colors.",
                "Disable it if you use another chat plugin",
                "like EssentialsChat (requires Vault)."));

        ColorIcon colorIcon = ColorIcon.fromConfig();
        gui.addElement(new StaticGuiElement('b', colorIcon.create(sampleColor()), click -> {
            openColorIconPage();
            return true;
        }, "§9Color Icons", DIVIDER, "§7Item shown for each color in /color.", " ",
                "§7Current: §f" + colorIcon.getDisplayName(), " ", "§9Click to choose"));

        gui.addElement(toggleElement('c', Material.NAME_TAG, "Tags", ConfigData.Keys.USE_TAGS,
                "Players can select tags (subgroups)",
                "in addition to their group.",
                "§8The /tags command requires a restart."));

        gui.addElement(toggleElement('d', Material.OAK_DOOR, "Join & Quit Messages", ConfigData.Keys.USE_JOIN_QUIT,
                "Uses the join and quit messages",
                "of the groups (groups.yml)."));

        gui.addElement(toggleElement('n', Material.PAINTING, "Tab List", ConfigData.Keys.DISPLAY_TAB_LIST,
                "Shows prefix and suffix in the tab list."));

        gui.addElement(toggleElement('o', Material.LADDER, "Sort Tab List", ConfigData.Keys.DISPLAY_SORT_TAB_LIST,
                "Sorts the tab list by the priority of the",
                "groups (groups.yml, higher first).",
                "§8Requires 'Tab List'."));

        gui.addElement(toggleElement('p', Material.NAME_TAG, "Name Tags", ConfigData.Keys.DISPLAY_NAME_TAGS,
                "Shows prefix and suffix above the heads.",
                "§8Uses teams of the main scoreboard, disable",
                "§8it if another plugin manages name tags."));

        gui.addElement(toggleElement('e', Material.IRON_DOOR, "Hide Join & Quit Messages", ConfigData.Keys.HIDE_JOIN_QUIT,
                "Hides all join and quit messages.",
                "§8Requires 'Join & Quit Messages'."));

        gui.addElement(toggleElement('f', Material.ANVIL, "Custom Layout", ConfigData.Keys.CUSTOM_LAYOUT,
                "Players can set their own prefix and suffix.",
                "§8Permissions: EasyPrefix.custom.prefix,",
                "§8EasyPrefix.custom.suffix, EasyPrefix.custom.gui"));

        double cooldown = config.getDouble(ConfigData.Keys.CUSTOM_LAYOUT_COOLDOWN);
        gui.addElement(new StaticGuiElement('g', new ItemStack(Material.CLOCK), click -> {
            if (!isAdmin()) return true;
            double step = click.getType().isShiftClick() ? 5 : 0.5;
            if (click.getType().isRightClick()) step = -step;
            double value = Math.max(0, Math.round((cooldown + step) * 10) / 10.0);
            config.save(ConfigData.Keys.CUSTOM_LAYOUT_COOLDOWN, value);
            openSettingsPage();
            return true;
        }, "§9Custom Layout Cooldown", DIVIDER, "§7Time between two changes of a",
                "§7custom prefix or suffix.", "§8Bypass: EasyPrefix.custom.bypass", " ",
                "§7Current: §f" + formatHours(cooldown), " ",
                "§9Left click §7+0.5h  §9Right click §7-0.5h", "§9Shift click §7±5h"));

        List<String> blacklist = config.getList(ConfigData.Keys.CUSTOM_LAYOUT_BLACKLIST);
        List<String> blacklistLore = new ArrayList<>(List.of("§9Custom Layout Blacklist", DIVIDER,
                "§7Words that are not allowed in", "§7custom prefixes and suffixes.",
                "§8Bypass: EasyPrefix.custom.blacklist", " "));
        blacklist.stream().limit(8).forEach(word -> blacklistLore.add("§7- §f" + word.replace("§", "&")));
        if (blacklist.size() > 8) blacklistLore.add("§7... and " + (blacklist.size() - 8) + " more");
        if (blacklist.isEmpty()) blacklistLore.add("§7(empty)");
        blacklistLore.addAll(List.of(" ", "§9Click to edit"));
        gui.addElement(new StaticGuiElement('h', new ItemStack(Material.IRON_BARS), click -> {
            openBlacklistPage();
            return true;
        }, blacklistLore.toArray(new String[0])));

        gui.addElement(aliasElement('i', Material.IRON_INGOT, "Prefix", ConfigData.Keys.PREFIX_ALIAS));
        gui.addElement(aliasElement('j', Material.GOLD_INGOT, "Suffix", ConfigData.Keys.SUFFIX_ALIAS));

        boolean sql = config.getBoolean(ConfigData.Keys.SQL_ENABLED);
        gui.addElement(new StaticGuiElement('m', new ItemStack(Material.ENDER_CHEST), click -> true,
                "§9Storage", DIVIDER, "§7Current: §f" + (sql ? "MySQL" : "Local files"),
                " ", "§8The database connection can only be", "§8changed in config.yml (restart required)."));

        gui.addElement(new StaticGuiElement('k', new ItemStack(Material.RECOVERY_COMPASS), click -> {
            gui.close();
            if (!isAdmin()) return true;
            instance.reload();
            user.sendAdminMessage("&aEasyPrefix has been reloaded!");
            return true;
        }, "§cReload", DIVIDER, "§7Reloads all files (config.yml, groups.yml,", "§7messages.yml) and users.", " ", "§9Click to reload"));

        GuiCreator.addBackButton(gui, this::openPageSetup);

        gui.show(user.getPlayer());
    }

    private StaticGuiElement toggleElement(char slot, Material material, String name, String key, String... description) {
        ConfigData config = this.instance.getConfigData();
        boolean enabled = config.getBoolean(key);

        List<String> lore = new ArrayList<>();
        lore.add("§9" + name);
        lore.add(DIVIDER);
        for (String line : description) lore.add(line.startsWith("§") ? line : "§7" + line);
        lore.add(" ");
        lore.add("§7Status: " + (enabled ? "§aenabled" : "§cdisabled"));
        lore.add(" ");
        lore.add("§9Click to " + (enabled ? "disable" : "enable"));

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

    private StaticGuiElement aliasElement(char slot, Material material, String type, String key) {
        ConfigData config = this.instance.getConfigData();
        String alias = config.getString(key, "");
        return new StaticGuiElement(slot, new ItemStack(material), click -> {
            UserInput.create().build(user, "§cType in the new command for " + type.toLowerCase() + "es (e.g. /" + type.toLowerCase()
                    + "). Type \"-\" to disable it, \"quit\" to cancel.", alias.isEmpty() ? "-" : alias, (input) -> {
                if (!isAdmin()) return;
                String value = input.trim().replace("/", "").replaceAll("[^a-zA-Z0-9_-]", "");
                config.save(key, value.isEmpty() ? "" : "/" + value);
                user.sendAdminMessage("&aThe command has been changed. Please restart the server to apply it!");
                openSettingsPage();
            });
            return true;
        }, "§9" + type + " Command", DIVIDER, "§7Opens the " + type.toLowerCase() + " input directly.",
                "§8Requires EasyPrefix.custom." + type.toLowerCase(), "§8Changes require a restart.", " ",
                "§7Current: §f" + (alias.isEmpty() ? "disabled" : alias), " ", "§9Click to change");
    }

    private void openColorIconPage() {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9Settings §8» §8Color Icons",
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
            }, "§9" + icon.getDisplayName(), DIVIDER, "§7" + icon.getDescription(), " ",
                    selected ? "§aSelected" : "§9Click to select"));
        }

        GuiElementGroup preview = new GuiElementGroup('b');
        for (Color color : instance.getColors()) {
            preview.addElement(new StaticGuiElement('b', current.create(color), click -> true,
                    "§r" + color.getDisplayName(), "§8Preview"));
        }

        gui.addElement(icons);
        gui.addElement(preview);
        GuiCreator.addBackButton(gui, this::openSettingsPage);
        gui.show(user.getPlayer());
    }

    private void openBlacklistPage() {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9Settings §8» §8Blacklist",
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
            }, "§f" + word.replace("§", "&"), " ", "§cClick to remove"));
        }
        gui.addElement(entries);

        GuiCreator.addBackButton(gui, this::openSettingsPage);

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.NETHER_STAR), click -> {
            UserInput.create().build(user, "§cType in the word to block. Write \"quit\" to cancel.", "", (input) -> {
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
        }, "§aAdd Word"));

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
        if (hours <= 0) return "none";
        if (hours < 1) return Math.round(hours * 60) + " minutes";
        return (hours == Math.floor(hours) ? Long.toString((long) hours) : Double.toString(hours)) + " hours";
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
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§4Delete " + easyGroup.getName() + "?", "xxxaxbxxx");
        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.GREEN_TERRACOTTA), click -> {
            if (!isAdmin()) return true;
            easyGroup.delete();
            if (easyGroup instanceof Group) openGroupsList();
            else openSubgroupsList();
            return true;
        }, "§aYes"));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.RED_TERRACOTTA), click -> {
            openProfile(easyGroup);
            return true;
        }, "§cNo"));

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
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9Group §8» §7" + group.getGroupColor() + group.getName(), "xabcxdehf");

        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.IRON_INGOT), click -> {
            String prefix = group.getPrefix();
            prefix = prefix == null ? " " : prefix.replace("§", "&");
            UserInput.create().build(user, "§cPlease type the prefix in the chat. Write \"quit\" to stop the process.", prefix, (input) -> {
                if (!isAdmin()) return;
                group.setPrefix(input);
                user.getPlayer().sendMessage(Message.INPUT_SAVED.getText());
                openGroupProfile(group);
            });
            return true;
        }, "§aChange Prefix", DIVIDER, "§7Current: §7«§f" + group.getPrefix() + "§7»", " "));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.GOLD_INGOT), click -> {
            String suffix = group.getSuffix();
            suffix = suffix == null ? " " : suffix.replace("§", "&");
            UserInput.create().build(user, "§cPlease type the suffix in the chat. Write \"quit\" to stop the process.", suffix, (input) -> {
                if (!isAdmin()) return;
                group.setSuffix(input);
                user.sendMessage(Message.INPUT_SAVED.getText());
                openGroupProfile(group);
            });
            return true;
        }, "§aChange Suffix", DIVIDER, "§7Current: §7«§f" + group.getSuffix() + "§7»", " "));

        gui.addElement(new StaticGuiElement('c', new ItemStack(Material.LIME_DYE), click -> {
            openPageColorGroup(group);
            return true;
        }, "§aChange Color", DIVIDER, "§7Current: §f" + group.getColor().getDisplayName(), " "));

        gui.addElement(new StaticGuiElement('d', new ItemStack(Material.BLAZE_ROD), click -> {
            String joinMsg = group.getJoinMessage();
            joinMsg = joinMsg == null ? " " : joinMsg.replace("§", "&");
            UserInput.create().build(user, "§cType in the join message", joinMsg, (input) -> {
                if (!isAdmin()) return;
                group.setJoinMessage(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
                openGroupProfile(group);
            });
            return true;
        }, "§aJoin Message", DIVIDER, "§7Current: §7«§f" + group.getJoinMessage() + "§7»", " "));

        gui.addElement(new StaticGuiElement('e', new ItemStack(Material.STICK), click -> {
            String quitMsg = group.getQuitMessage();
            quitMsg = quitMsg == null ? " " : quitMsg.replace("§", "&");
            UserInput.create().build(user, "§cType in the quit message", quitMsg, (input) -> {
                if (!isAdmin()) return;
                group.setQuitMessage(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
                openGroupProfile(group);
            });
            return true;
        }, "§cQuit Message", DIVIDER, "§7Current: §7«§f" + group.getQuitMessage() + "§7»", " "));

        gui.addElement(new StaticGuiElement('f', new ItemStack(Material.EXPERIENCE_BOTTLE), click -> {
            if (!isAdmin()) return true;
            int step = click.getType().isShiftClick() ? 10 : 1;
            if (click.getType().isRightClick()) step = -step;
            group.setPriority(group.getPriority() + step);
            openGroupProfile(group);
            return true;
        }, "§aPriority", DIVIDER, "§7Players with several groups get the one", "§7with the highest priority. The tab list",
                "§7is sorted by it (higher first).", " ", "§7Current: §f" + group.getPriority(), " ",
                "§9Left click §7+1  §9Right click §7-1", "§9Shift click §7±10"));

        gui.addElement(hoverElement(group));

        if (!group.getName().equals("default")) {
            gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
                openPageDeleteGroup(group);
                return true;
            }, "§4Delete", " "));
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
        List<String> lore = new ArrayList<>();
        lore.add("§aHover Text");
        lore.add(DIVIDER);
        lore.add("§7Shown when hovering over the name in the chat.");
        lore.add(" ");
        lore.add(!inherited ? "§7Current:" : isDefault ? "§7Current §8(built-in)§7:" : "§7Current §8(of the default group)§7:");
        hover.stream().limit(10).forEach(line -> lore.add("§8| §r" + Message.setColors(line)));
        if (hover.size() > 10) lore.add("§8| §7...");
        lore.add(" ");
        lore.add("§9Left click §7set all lines (separated by |)");
        lore.add("§9Right click §7add a line");
        lore.add(isDefault ? "§9Shift click §7use the built-in text again" : "§9Shift click §7use the text of the default group");

        return new StaticGuiElement('h', new ItemStack(Material.OAK_SIGN), click -> {
            if (!isAdmin()) return true;
            if (click.getType().isShiftClick()) {
                group.setHover(null);
                user.sendAdminMessage(Message.INPUT_SAVED);
                openGroupProfile(group);
            } else if (click.getType().isRightClick()) {
                UserInput.create().build(user, "§cType in the new line. Write \"quit\" to stop the process.", null, (input) -> {
                    if (!isAdmin()) return;
                    List<String> lines = new ArrayList<>(group.getHover());
                    lines.add(input);
                    group.setHover(lines);
                    user.sendAdminMessage(Message.INPUT_SAVED);
                    openGroupProfile(group);
                });
            } else {
                UserInput.create().build(user, "§cType in the lines, separated by |. Write \"quit\" to stop the process.", null, (input) -> {
                    if (!isAdmin()) return;
                    group.setHover(List.of(input.split(Pattern.quote("|"), -1)));
                    user.sendAdminMessage(Message.INPUT_SAVED);
                    openGroupProfile(group);
                });
            }
            return true;
        }, lore.toArray(new String[0]));
    }

    private void openSubgroupsList() {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9EasyPrefix §8» §8Tags", Arrays.asList("aaaaaaaaa", "aaaaaaaaa"));
        GuiElementGroup elementGroup = new GuiElementGroup('a');

        for (final Subgroup subgroup : this.instance.getGroupHandler().getSubgroups()) {
            String prefix = Optional.ofNullable(subgroup.getPrefix()).orElse("-");
            String suffix = Optional.ofNullable(subgroup.getSuffix()).orElse("-");

            String prefixColor = subgroup.getGroupColor();
            List<String> lore = new ArrayList<>();
            lore.add(prefixColor + subgroup.getName());
            lore.add("§7-------------------------");
            if (prefix.length() > 25) {
                lore.add("§7Prefix: §7«§f" + prefix.substring(0, 25));
                lore.add("§f" + prefix.substring(25) + "§7»");
            } else {
                lore.add("§7Prefix: §7«§f" + prefix + "§7»");
            }
            lore.add("§7Suffix: §7«§f" + suffix + "§7»");
            lore.add("§7Permission: §fEasyPrefix.tag." + subgroup.getName());

            ItemStack sgBtn = new ItemStack(Material.WRITABLE_BOOK);
            elementGroup.addElement(new StaticGuiElement('b', sgBtn, click -> {
                openSubgroupProfile(subgroup);
                return true;
            }, lore.toArray(new String[0])));
        }

        gui.addElement(elementGroup);

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.NETHER_STAR), click -> {
            openTagCreator();
            return true;
        }, "§aAdd Tag"));
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
            }, "§r" + color.getDisplayName(), " ", (color.getPermission() != null ? String.format("§7Permission: §f%s", color.getPermission().getName()) : "")));
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
            }, decorationTitle(decoration), " ", (decoration.getPermission() != null ? String.format("§7Permission: §f%s", decoration.getPermission().getName()) : "")));

        }

        // a group always needs a color, so there is no reset here (the formatting can be deselected by clicking it again)
        GuiCreator.addBackButton(gui, () -> openGroupProfile(group));
        gui.addElement(groupColors);
        gui.addElement(groupFormattings);
        gui.show(user.getPlayer());
    }

    private void openSubgroupProfile(Subgroup subgroup) {
        if (!isAdmin()) return;
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9Tag (Subgroup) §8» §7" + subgroup.getGroupColor() + subgroup.getName(), " a b   f ");

        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.IRON_INGOT), click -> {
            String prefix = subgroup.getPrefix();
            prefix = prefix == null ? " " : prefix.replace("§", "&");
            UserInput.create().build(user, "§cType in the prefix", prefix, (input) -> {
                if (!isAdmin()) return;
                subgroup.setPrefix(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
                openSubgroupProfile(subgroup);
            });
            return true;
        }, "§aChange Prefix", DIVIDER, "§7Current: §7«§f" + subgroup.getPrefix() + "§7»", " "));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.GOLD_INGOT), click -> {
            String suffix = subgroup.getSuffix();
            suffix = suffix == null ? " " : suffix.replace("§", "&");
            UserInput.create().build(user, "§cType in the suffix", suffix, (input) -> {
                if (!isAdmin()) return;
                subgroup.setSuffix(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
                openSubgroupProfile(subgroup);
            });
            return true;
        }, "§aChange Suffix", DIVIDER, "§7Current: §7«§f" + subgroup.getSuffix() + "§7»", " "));

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
            openPageDeleteGroup(subgroup);
            return true;
        }, "§4Delete", " "));

        GuiCreator.addBackButton(gui, this::openSubgroupsList);
        gui.show(user.getPlayer());
    }

    private void openGroupCreator() {
        if (!isAdmin()) return;
        UserInput.create().build(user, "§cType in the name", "ExampleGroup", (input) -> {
            if (!isAdmin()) return;
            String name = input.replaceAll("[^a-zA-Z0-9_]", "");
            if (this.instance.getGroupHandler().createGroup(name)) {
                user.sendAdminMessage("&aGroup '" + name + "' has been created!");
            } else {
                user.sendAdminMessage("§cCouldn't create group!");
            }
            openGroupsList();
        });
    }

    private void openTagCreator() {
        if (!isAdmin()) return;
        UserInput.create().build(user, "§cType in the name", "ExampleTag", (input) -> {
            if (!isAdmin()) return;
            String name = input.replaceAll("[^a-zA-Z0-9_]", "");
            if (this.instance.getGroupHandler().createSubgroup(name)) {
                user.sendAdminMessage("&aTag '" + name + "' has been created!");
            } else {
                user.sendAdminMessage("§cCouldn't create tag!");
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
