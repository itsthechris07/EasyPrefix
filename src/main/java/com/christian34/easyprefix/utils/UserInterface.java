package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.textinput.UserInput;
import com.cryptomorin.xseries.XMaterial;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EasyPrefix 2023.
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
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9EasyPrefix §8» §8Configuration", "xxaxbxcxx");
        gui.addElement(new StaticGuiElement('a', XMaterial.CHEST.parseItem(), click -> {
            openGroupsList();
            return true;
        }, "§9Groups"));

        gui.addElement(new StaticGuiElement('b', XMaterial.NETHER_STAR.parseItem(), click -> {
            openSettingsPage();
            return true;
        }, "§9Settings"));

        gui.addElement(new StaticGuiElement('c', ((VersionController.getMinorVersion() <= 12) ? XMaterial.CHEST : XMaterial.WRITABLE_BOOK).parseItem(), click -> {
            openSubgroupsList();
            return true;
        }, "§9Tags §8(Subgroups)"));
        gui.show(user.getPlayer());
    }

    public void openUserSettings() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_MAIN), "xxxaxcxxx");
        gui.addElement(new StaticGuiElement('a', XMaterial.CHEST.parseItem(), click -> {
            int userGroups = user.getAvailableGroups().size();
            if (userGroups <= 1 && instance.getConfigData().getBoolean(ConfigData.Keys.USE_TAGS)) {
                openUserSubgroupsListPage();
            } else {
                openUserGroupsListPage();
            }
            return true;
        }, Message.BTN_MY_PREFIXES.getText()));

        gui.addElement(new StaticGuiElement('c', XMaterial.CHEST.parseItem(), click -> {
            openPageUserColors();
            return true;
        }, Message.BTN_MY_FORMATTINGS.getText()));

        gui.show(user.getPlayer());
    }

    public void showCustomPrefixGui() {
        Timestamp next = getNextTimestamp(user.getLastPrefixUpdate());
        if (!next.before(new Timestamp(System.currentTimeMillis())) && !user.hasPermission(UserPermission.CUSTOM_BYPASS)) {
            user.getPlayer().sendMessage(getTimeMessage(next));
            return;
        }

        UserInput.create().build(user, Message.GUI_INPUT_PREFIX.getText(), user.getPrefix(), (input) -> {
            if (!user.hasPermission(UserPermission.CUSTOM_BLACKLIST)) {
                for (String blocked : this.instance.getConfigData().getList(ConfigData.Keys.CUSTOM_LAYOUT_BLACKLIST)) {
                    if (input.toLowerCase().contains(blocked.toLowerCase())) {
                        user.getPlayer().sendMessage(Message.CHATLAYOUT_INVALID.getText());
                        return;
                    }
                }
            }

            String text = Message.CHAT_INPUT_PREFIX_CONFIRM.getText().replace("%content%", input);
            ChatButtonConfirm chatButtonConfirm = new ChatButtonConfirm(user.getPlayer(), text, Message.CHAT_BTN_CONFIRM.getText());
            chatButtonConfirm.onClick(() -> {
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                user.setPrefix(input);
                user.saveData("custom_prefix_update", currentTime.toString());
                user.getPlayer().sendMessage(Message.CHAT_INPUT_PREFIX_SAVED.getText().replace("%content%", Optional.ofNullable(user.getPrefix()).orElse("-")));
            });
        });
    }

    public void showCustomSuffixGui() {
        Timestamp next = getNextTimestamp(user.getLastSuffixUpdate());
        if (!next.before(new Timestamp(System.currentTimeMillis())) && !user.hasPermission(UserPermission.CUSTOM_BYPASS)) {
            user.getPlayer().sendMessage(getTimeMessage(next));
            return;
        }

        UserInput.create().build(user, Message.GUI_INPUT_SUFFIX.getText(), user.getSuffix(), (input) -> {
            if (!user.hasPermission(UserPermission.CUSTOM_BLACKLIST)) {
                for (String blocked : this.instance.getConfigData().getList(ConfigData.Keys.CUSTOM_LAYOUT_BLACKLIST)) {
                    if (input.toLowerCase().contains(blocked.toLowerCase())) {
                        user.getPlayer().sendMessage(Message.CHATLAYOUT_INVALID.getText());
                        return;
                    }
                }
            }

            String text = Message.CHAT_INPUT_SUFFIX_CONFIRM.getText().replace("%content%", input);
            ChatButtonConfirm chatButtonConfirm = new ChatButtonConfirm(user.getPlayer(), text, Message.CHAT_BTN_CONFIRM.getText());
            chatButtonConfirm.onClick(() -> {
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                user.setSuffix(input);
                user.saveData("custom_suffix_update", currentTime.toString());
                user.getPlayer().sendMessage(Message.CHAT_INPUT_SUFFIX_SAVED.getText().replace("%content%", Optional.ofNullable(user.getSuffix()).orElse("-")));
            });
        });
    }

    public void openPageUserColors() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_FORMATTINGS), Arrays.asList("a".repeat(9), "a".repeat(9), "b".repeat(9)));

        Collection<Color> colors = user.getColors();
        GuiElementGroup groupColors = new GuiElementGroup('a');
        for (Color color : colors) {
            if (!user.hasPermission(color.getPermission())) continue;

            ItemStack itemStack = new ItemStack(Material.BOOK);
            if (user.getColor() != null && user.getColor().equals(color)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
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
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
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
            }, TextUtils.colorize("<reset>" + decoration.getDisplayName())));

        }

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
            user.setColor(null);
            user.setDecoration(null);
            openPageUserColors();
            return true;
        }, Message.BTN_RESET.getText(), " "));

        gui.addElement(groupColors);
        gui.addElement(groupFormattings);
        gui.show(user.getPlayer());
    }

    public void openUserSubgroupsListPage() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_TAGS), Arrays.asList("aaaaaaaaa", "aaaaaaaaa"));
        GuiElementGroup elementGroup = new GuiElementGroup('a');

        final String loreSelectTag = Message.BTN_SETTINGS_SELECT_TAG.getText();
        Material book = XMaterial.BOOK.parseMaterial();
        for (Subgroup subgroup : user.getAvailableSubgroups()) {
            List<String> lore = new ArrayList<>();
            lore.add(subgroup.getGroupColor() + subgroup.getName());
            ItemStack bookItem = new ItemStack(Objects.requireNonNull(book));

            if (user.getSubgroup() != null && user.getSubgroup().equals(subgroup)) {
                bookItem.addUnsafeEnchantment(Enchantment.LUCK, 1);
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

        gui.addElement(elementGroup);
        gui.show(user.getPlayer());
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
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9EasyPrefix §8» §8Groups", Arrays.asList("aaaaaaaaa", "aaaaaaaaa"));
        GuiElementGroup elementGroup = new GuiElementGroup('a');

        for (Group group : instance.getGroupHandler().getGroups()) {
            String prefix = Optional.ofNullable(group.getPrefix()).orElse("-");
            String suffix = Optional.ofNullable(group.getSuffix()).orElse("-");
            ChatColor prefixColor = group.getGroupColor();
            List<String> lore = new ArrayList<>();
            lore.add(prefixColor + group.getName());
            lore.add("§7-------------------------------");
            if (prefix.length() > 25) {
                lore.add("§7Prefix: §7«§f" + prefix.substring(0, 25));
                lore.add("§f" + prefix.substring(26) + "§7»");
            } else {
                lore.add("§7Prefix: §7«§f" + prefix + "§7»");
            }
            lore.add("§7Suffix: §7«§f" + suffix + "§7»");

            lore.add("§7Color: §f" + group.getColor().getDisplayName());
            lore.add("§7Permission: §fEasyPrefix.group." + group.getName());
            elementGroup.addElement(new StaticGuiElement('b', XMaterial.CHEST.parseItem(), click -> {
                openGroupProfile(group);
                return true;
            }, lore.toArray(new String[0])));
        }
        gui.addElement(elementGroup);

        gui.addElement(new StaticGuiElement('q', XMaterial.NETHER_STAR.parseItem(), click -> {
            openGroupCreator();
            return true;
        }, "§aAdd Group"));

        gui.show(user.getPlayer());
    }

    private void openCustomLayoutPage() {
        if (!instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT) || !user.hasPermission("custom.gui")) {
            openUserSettings();
        }

        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_LAYOUT), "xxxaxbxxx");
        List<String> lorePrefix = new ArrayList<>();
        lorePrefix.add(Message.BTN_CHANGE_PREFIX.getText());
        lorePrefix.addAll(replaceInList(Message.LORE_CHANGE_PREFIX.getList(), Optional.ofNullable(user.getPrefix()).orElse("-")));
        gui.addElement(new StaticGuiElement('a', XMaterial.IRON_INGOT.parseItem(), click -> {
            showCustomPrefixGui();
            return true;
        }, lorePrefix.toArray(new String[0])));

        List<String> loreSuffix = new ArrayList<>();
        loreSuffix.add(Message.BTN_CHANGE_SUFFIX.getText());
        loreSuffix.addAll(replaceInList(Message.LORE_CHANGE_SUFFIX.getList(), Optional.ofNullable(user.getSuffix()).orElse("-")));
        gui.addElement(new StaticGuiElement('b', XMaterial.GOLD_INGOT.parseItem(), click -> {
            showCustomSuffixGui();
            return true;
        }, loreSuffix.toArray(new String[0])));

        gui.show(user.getPlayer());
    }

    private void openSettingsPage() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9EasyPrefix §8» §8Settings", "xxxxbxxxx");
        ConfigData config = this.instance.getConfigData();

        //GuiStateElement colors = new GuiStateElement('b', new GuiStateElement.State(change -> config.save(ConfigData.Keys.HANDLE_COLORS, true), "true", XMaterial.LIME_DYE.parseItem(), "§aHandle colors §7(§aenabled§7)"), new GuiStateElement.State(change -> config.save(ConfigData.Keys.HANDLE_COLORS, false), "false", XMaterial.LIME_DYE.parseItem(), "§aHandle colors §7(§cdisabled§7)"));
        //colors.setState(instance.getConfigData().getBoolean(ConfigData.Keys.HANDLE_COLORS).toString());
        //gui.addElement(colors);

        gui.show(user.getPlayer());
    }

    private void openProfile(EasyGroup easyGroup) {
        if (easyGroup instanceof Group) {
            openGroupProfile((Group) easyGroup);
        } else {
            openSubgroupProfile((Subgroup) easyGroup);
        }
    }

    private void openPageDeleteGroup(EasyGroup easyGroup) {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§4Delete " + easyGroup.getName() + "?", "xxxaxbxxx");
        gui.addElement(new StaticGuiElement('a', XMaterial.GREEN_TERRACOTTA.parseItem(), click -> {
            easyGroup.delete();
            if (easyGroup instanceof Group) openGroupsList();
            else openSubgroupsList();
            return true;
        }, "§aYes"));

        gui.addElement(new StaticGuiElement('b', XMaterial.RED_TERRACOTTA.parseItem(), click -> {
            openProfile(easyGroup);
            return true;
        }, "§cNo"));

        gui.show(user.getPlayer());
    }

    private void openUserGroupsListPage() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_LAYOUT), Arrays.asList("aaaaaaaaa", "aaaaaaaaa"));
        GuiElementGroup elementGroup = new GuiElementGroup('a');

        final List<String> defaultLore = Message.BTN_SELECT_PREFIX_LORE.getList();

        Material book = XMaterial.BOOK.parseMaterial();
        for (Group group : user.getAvailableGroups()) {
            ChatColor prefixColor = group.getGroupColor();
            List<String> lore = new ArrayList<>();
            lore.add(prefixColor + group.getName());
            ItemStack itemStack = new ItemStack(Objects.requireNonNull(book));

            if (user.getGroup().equals(group)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemStack.setItemMeta(meta);
                }
            }
            String prefix = group.getPrefix();
            for (String line : defaultLore) {
                line = line.replace("%LAYOUT%", prefix + user.getName() + group.getSuffix());
                lore.add(line);
            }

            elementGroup.addElement(new StaticGuiElement('b', itemStack, click -> {
                user.setGroup(group, false);
                openUserGroupsListPage();
                return true;
            }, lore.toArray(new String[0])));
        }

        if (!user.getAvailableSubgroups().isEmpty()) {
            ItemStack subgroupsMaterial = VersionController.getMinorVersion() <= 12 ? XMaterial.CHEST.parseItem() : XMaterial.WRITABLE_BOOK.parseItem();
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

        gui.addElement(elementGroup);
        gui.show(user.getPlayer());
    }

    private void openGroupProfile(Group group) {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9Group §8» §7" + group.getGroupColor() + group.getName(), "xabcxdexf");

        gui.addElement(new StaticGuiElement('a', XMaterial.IRON_INGOT.parseItem(), click -> {
            String prefix = group.getPrefix();
            prefix = prefix == null ? " " : prefix.replace("§", "&");
            UserInput.create().build(user, "§cPlease type the prefix in the chat. Write \"quit\" to stop the process.", prefix, (input) -> {
                group.setPrefix(input);
                user.getPlayer().sendMessage(Message.INPUT_SAVED.getText());
            });
            return true;
        }, "§aChange Prefix", DIVIDER, "§7Current: §7«§f" + group.getPrefix() + "§7»", " "));

        gui.addElement(new StaticGuiElement('b', XMaterial.GOLD_INGOT.parseItem(), click -> {
            String suffix = group.getSuffix();
            suffix = suffix == null ? " " : suffix.replace("§", "&");
            UserInput.create().build(user, "§cPlease type the suffix in the chat. Write \"quit\" to stop the process.", suffix, (input) -> {
                group.setSuffix(input);
                user.sendMessage(Message.INPUT_SAVED.getText());
            });
            return true;
        }, "§aChange Suffix", DIVIDER, "§7Current: §7«§f" + group.getSuffix() + "§7»", " "));

        gui.addElement(new StaticGuiElement('c', XMaterial.LIME_DYE.parseItem(), click -> {
            openPageColorGroup(group);
            return true;
        }, "§aChange Color", DIVIDER, "§7Current: §f" + group.getColor().getDisplayName(), " "));

        gui.addElement(new StaticGuiElement('d', XMaterial.BLAZE_ROD.parseItem(), click -> {
            String joinMsg = group.getJoinMessage();
            joinMsg = joinMsg == null ? " " : joinMsg.replace("§", "&");
            UserInput.create().build(user, "§cType in the join message", joinMsg, (input) -> {
                group.setJoinMessage(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
            });
            return true;
        }, "§aJoin Message", DIVIDER, "§7Current: §7«§f" + group.getJoinMessage() + "§7»", " "));

        gui.addElement(new StaticGuiElement('e', XMaterial.STICK.parseItem(), click -> {
            String quitMsg = group.getQuitMessage();
            quitMsg = quitMsg == null ? " " : quitMsg.replace("§", "&");
            UserInput.create().build(user, "§cType in the quit message", quitMsg, (input) -> {
                group.setQuitMessage(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
            });
            return true;
        }, "§cQuit Message", DIVIDER, "§7Current: §7«§f" + group.getQuitMessage() + "§7»", " "));

        if (!group.getName().equals("default")) {
            gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
                openPageDeleteGroup(group);
                return true;
            }, "§4Delete", " "));
        }

        gui.show(user.getPlayer());
    }

    private void openSubgroupsList() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9EasyPrefix §8» §8Groups", Arrays.asList("aaaaaaaaa", "aaaaaaaaa"));
        GuiElementGroup elementGroup = new GuiElementGroup('a');

        for (final Subgroup subgroup : this.instance.getGroupHandler().getSubgroups()) {
            String prefix = Optional.ofNullable(subgroup.getPrefix()).orElse("-");
            String suffix = Optional.ofNullable(subgroup.getSuffix()).orElse("-");

            ChatColor prefixColor = subgroup.getGroupColor();
            List<String> lore = new ArrayList<>();
            lore.add(prefixColor + subgroup.getName());
            lore.add("§7-------------------------");
            if (prefix.length() > 25) {
                lore.add("§7Prefix: §7«§f" + prefix.substring(0, 25));
                lore.add("§f" + prefix.substring(26) + "§7»");
            } else {
                lore.add("§7Prefix: §7«§f" + prefix + "§7»");
            }
            lore.add("§7Suffix: §7«§f" + suffix + "§7»");
            lore.add("§7Permission: §fEasyPrefix.tag." + subgroup.getName());

            ItemStack sgBtn = VersionController.getMinorVersion() <= 12 ? XMaterial.CHEST.parseItem() : XMaterial.WRITABLE_BOOK.parseItem();
            elementGroup.addElement(new StaticGuiElement('b', sgBtn, click -> {
                openSubgroupProfile(subgroup);
                return true;
            }, lore.toArray(new String[0])));
        }

        gui.addElement(elementGroup);

        gui.addElement(new StaticGuiElement('q', XMaterial.NETHER_STAR.parseItem(), click -> {
            openTagCreator();
            return true;
        }, "§aAdd Tag"));
        gui.show(user.getPlayer());
    }

    private void openPageColorGroup(Group group) {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), group.getGroupColor() + group.getName() + " §8» " + Message.GUI_SETTINGS_TITLE_FORMATTINGS.getText(), Arrays.asList("a".repeat(9), "a".repeat(9), "b".repeat(9)));

        GuiElementGroup groupColors = new GuiElementGroup('a');
        for (Color color : EasyPrefix.getInstance().getColors()) {
            ItemStack itemStack = new ItemStack(Material.BOOK);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (group.getColor().equals(color)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                itemStack.setItemMeta(itemMeta);
            }
            itemStack.setItemMeta(itemMeta);

            groupColors.addElement(new StaticGuiElement('a', itemStack, click -> {
                group.setColor(color);
                openPageColorGroup(group);
                return true;
            }, "§r" + color.getDisplayName(), " ", (color.getPermission() != null ? String.format("§7Permission: §f%s", color.getPermission().getName()) : "")));
        }

        GuiElementGroup groupFormattings = new GuiElementGroup('b');
        for (Decoration decoration : instance.getDecorations()) {
            ItemStack itemStack = new ItemStack(Material.BOOKSHELF);
            if (group.getDecoration() != null && group.getDecoration().equals(decoration)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemStack.setItemMeta(meta);
                }
            }

            groupFormattings.addElement(new StaticGuiElement('b', itemStack, click -> {
                Decoration deco = decoration;
                if (group.getDecoration() != null && group.getDecoration().equals(decoration)) {
                    deco = null;
                }
                group.setDecoration(deco);
                openPageColorGroup(group);
                return true;
            }, "<reset>" + decoration.getDisplayName(), " ", (decoration.getPermission() != null ? String.format("§7Permission: §f%s", decoration.getPermission().getName()) : "")));

        }

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
            user.setColor(null);
            user.setDecoration(null);
            openPageUserColors();
            return true;
        }, Message.BTN_RESET.getText(), " "));

        gui.addElement(groupColors);
        gui.addElement(groupFormattings);
        gui.show(user.getPlayer());
    }

    private void openSubgroupProfile(Subgroup subgroup) {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9Tag (Subgroup) §8» §7" + subgroup.getGroupColor() + subgroup.getName(), " a b   f ");

        gui.addElement(new StaticGuiElement('a', XMaterial.IRON_INGOT.parseItem(), click -> {
            String prefix = subgroup.getPrefix();
            prefix = prefix == null ? " " : prefix.replace("§", "&");
            UserInput.create().build(user, "§cType in the prefix", prefix, (input) -> {
                subgroup.setPrefix(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
            });
            return true;
        }, "§aChange Prefix", DIVIDER, "§7Current: §7«§f" + subgroup.getPrefix() + "§7»", " "));

        gui.addElement(new StaticGuiElement('b', XMaterial.GOLD_INGOT.parseItem(), click -> {
            String suffix = subgroup.getSuffix();
            suffix = suffix == null ? " " : suffix.replace("§", "&");
            UserInput.create().build(user, "§cType in the suffix", suffix, (input) -> {
                subgroup.setSuffix(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
            });
            return true;
        }, "§aChange Suffix", DIVIDER, "§7Current: §7«§f" + subgroup.getSuffix() + "§7»", " "));

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
            openPageDeleteGroup(subgroup);
            return true;
        }, "§4Delete", " "));

        gui.show(user.getPlayer());
    }

    private void openGroupCreator() {
        UserInput.create().build(user, "§cType in the name", "ExampleGroup", (input) -> {
            String name = input.replaceAll("[^a-zA-Z0-9_]", "");
            if (this.instance.getGroupHandler().createGroup(name)) {
                user.sendAdminMessage("&aGroup '" + name + "' has been created!");
            } else {
                user.sendAdminMessage("§cCouldn't create group!");
            }
        });
    }

    private void openTagCreator() {
        UserInput.create().build(user, "§cType in the name", "ExampleGroup", (input) -> {
            String name = input.replaceAll("[^a-zA-Z0-9_]", "");
            if (this.instance.getGroupHandler().createSubgroup(name)) {
                user.sendAdminMessage("&aTag '" + name + "' has been created!");
            } else {
                user.sendAdminMessage("§cCouldn't create tag!");
            }
        });
    }

    private String setTitle(Message sub) {
        return TITLE.replace("%page%", sub.getText());
    }

    private List<String> replaceInList(@NotNull List<String> list, @NotNull String value) {
        return list.stream().map(val -> val.replace("%content%", value)).collect(Collectors.toList());
    }

}
