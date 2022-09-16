package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.groups.gender.GenderedLayout;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiStateElement;
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
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
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

    private void openPageGroupGender(EasyGroup easyGroup) {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§8Select a gender", "xaaaaaaax");
        GenderedLayout genderedLayout = easyGroup.getGenderedLayout();
        GuiElementGroup elementGroup = new GuiElementGroup('a');
        for (Gender gender : instance.getGroupHandler().getGenderTypes()) {
            String prefix = "-/-";
            String suffix = "-/-";
            if (genderedLayout != null) {
                prefix = genderedLayout.getPrefix(gender);
                suffix = genderedLayout.getSuffix(gender);
            }

            elementGroup.addElement(new StaticGuiElement('a', getPlayerHead(), click -> {
                openPageModifyGenderLayout(easyGroup, gender);
                return true;
            }, gender.getDisplayName(), " ", "§7Prefix: §7«§f" + prefix + "§7»", "§7Suffix: §7«§f" + suffix + "§7»"));
        }

        gui.addElement(elementGroup);
        gui.show(user.getPlayer());
    }

    public void openUserSettings() {
        String pattern = instance.getConfigData().getBoolean(ConfigData.Keys.USE_GENDER) ? "xxaxbxcxx" : "xxxaxcxxx";
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_MAIN), pattern);
        gui.addElement(new StaticGuiElement('a', XMaterial.CHEST.parseItem(), click -> {
            int userGroups = user.getAvailableGroups().size();
            if (userGroups <= 1 && instance.getConfigData().getBoolean(ConfigData.Keys.USE_TAGS)) {
                openUserSubgroupsListPage();
            } else {
                openUserGroupsListPage();
            }
            return true;
        }, Message.BTN_MY_PREFIXES.getText()));

        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        if (head != null) {
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                SkullUtils.applySkin(meta, user.getPlayer().getName());
            }
            head.setItemMeta(meta);
        }
        gui.addElement(new StaticGuiElement('b', head, click -> {
            openSelectGenderPage();
            return true;
        }, Message.BTN_CHANGE_GENDER.getText()));

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

        TextInput textInput = new TextInput(user, Message.GUI_INPUT_PREFIX.getText(), user.getPrefix());
        textInput.onComplete((input) -> {
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
        }).build();
    }

    public void showCustomSuffixGui() {
        Timestamp next = getNextTimestamp(user.getLastSuffixUpdate());
        if (!next.before(new Timestamp(System.currentTimeMillis())) && !user.hasPermission(UserPermission.CUSTOM_BYPASS)) {
            user.getPlayer().sendMessage(getTimeMessage(next));
            return;
        }

        TextInput textInput = new TextInput(user, Message.GUI_INPUT_SUFFIX.getText(), user.getSuffix());
        textInput.onComplete((input) -> {
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
        }).build();
    }

    public void openPageUserColors() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_FORMATTINGS), Arrays.asList("aaaaaaaaa", " aaaaaaa ", "  bbbbb  "));
        final boolean showAll = instance.getConfigData().getBoolean(ConfigData.Keys.GUI_SHOW_ALL_CHATCOLORS);

        List<Color> colors = showAll ? Arrays.asList(Color.getValues()) : new ArrayList<>(user.getColors());
        GuiElementGroup groupColors = new GuiElementGroup('a');
        for (Color color : colors) {
            if (!showAll && !user.hasPermission("color." + color.name())) continue;

            ItemStack itemStack = color.toItemStack();
            if (user.getChatColor().equals(color)) {
                if (user.getChatFormatting() == null || !user.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                    itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        itemStack.setItemMeta(meta);
                    }
                }
            }

            groupColors.addElement(new StaticGuiElement('a', itemStack, click -> {
                if (user.hasPermission("color." + color.name())) {
                    user.setChatColor(color);
                    user.sendMessage(Message.COLOR_PLAYER_SELECT.getText().replace("%color%", user.getChatColorName()));
                    openPageUserColors();
                } else {
                    user.sendMessage(Message.CHAT_NO_PERMS.getText());
                }
                return true;
            }, "§r" + color));
        }

        List<ChatFormatting> formattings = showAll ? Arrays.asList(ChatFormatting.getValues()) : new ArrayList<>(user.getChatFormattings());
        GuiElementGroup groupFormattings = new GuiElementGroup('b');
        for (ChatFormatting chatFormatting : formattings) {
            if (!showAll && !user.hasPermission("color." + chatFormatting.name())) continue;

            ItemStack itemStack = new ItemStack(Material.BOOKSHELF);
            if (user.getChatFormatting() != null && user.getChatFormatting().equals(chatFormatting)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemStack.setItemMeta(meta);
                }
            }

            groupFormattings.addElement(new StaticGuiElement('b', itemStack, click -> {
                ChatFormatting formatting = chatFormatting;
                if (user.getPlayer().hasPermission("color." + formatting.name().toLowerCase())) {
                    if (user.getChatFormatting() != null && user.getChatFormatting().equals(formatting)) {
                        formatting = ChatFormatting.UNDEFINED;
                    }
                    user.setChatFormatting(formatting);
                    user.sendMessage(Message.COLOR_PLAYER_SELECT.getText().replace("%color%", user.getChatColorName()));
                    openPageUserColors();
                } else {
                    user.sendMessage(Message.CHAT_NO_PERMS.getText());
                }
                return true;
            }, "§r" + user.getChatColor().getCode() + chatFormatting));
        }

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
            user.setChatColor(null);
            user.setChatFormatting(null);
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

    public void openSelectGenderPage() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_GENDER), "xxxxaxxxx");
        GroupHandler groupHandler = this.instance.getGroupHandler();
        List<GuiStateElement.State> states = new ArrayList<>();

        for (Gender gender : groupHandler.getGenderTypes()) {
            GuiStateElement.State state = new GuiStateElement.State(change -> user.setGenderType(gender), gender.getName(), getPlayerHead(), gender.getDisplayName());
            states.add(state);
        }
        GuiStateElement selectElement = new GuiStateElement('a', states.toArray(new GuiStateElement.State[states.size() - 1]));
        if (user.getGenderType() != null) {
            selectElement.setState(user.getGenderType().getName());
        }

        gui.addElement(selectElement);
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

    private void openPageModifyGenderLayout(EasyGroup group, Gender gender) {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9Group §8» §8" + group.getName() + " (" + gender.getName() + ")", "xabcxdexf");
        GenderedLayout genderedLayout = group.getGenderedLayout();

        String prefix = null, suffix = null;
        if (genderedLayout != null) {
            prefix = genderedLayout.getPrefix(gender);
            suffix = genderedLayout.getSuffix(gender);
        }

        String finalPrefix = prefix == null ? " " : prefix;
        gui.addElement(new StaticGuiElement('a', XMaterial.IRON_INGOT.parseItem(), click -> {
            TextInput textInput = new TextInput(user, "§cType in the prefix", finalPrefix);
            textInput.onComplete((input) -> {
                group.setPrefix(input, gender);
                user.sendAdminMessage("The prefix for group " + group.getName() + " (" + gender.getName() + ") has been updated!");
            }).build();
            return true;
        }, "§aChange Prefix", DIVIDER, "§7Current: §7«§f" + ((prefix == null) ? "-/-" : prefix) + "§7»", " "));

        String finalSuffix = suffix == null ? " " : suffix;
        gui.addElement(new StaticGuiElement('b', XMaterial.GOLD_INGOT.parseItem(), click -> {
            TextInput textInput = new TextInput(user, "§cType in the suffix", finalSuffix);
            textInput.onComplete((input) -> {
                group.setSuffix(input, gender);
                user.sendAdminMessage("The prefix for group " + group.getName() + " (" + gender.getName() + ") has been updated!");
            }).build();
            return true;
        }, "§aChange Suffix", DIVIDER, "§7Current: §7«§f" + ((suffix == null) ? "-/-" : suffix) + "§7»", " "));

        gui.show(user.getPlayer());
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

            String groupChatColor = group.getChatColor().getCode();
            if (group.getChatFormatting() != null) {
                if (group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                    groupChatColor += group.getChatFormatting().getCode();
                } else {
                    groupChatColor = Message.FORMATTING_RAINBOW.getText();
                }
            }

            lore.add("§7Color: §f" + groupChatColor.replace("§", "&"));
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
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), "§9EasyPrefix §8» §8Settings", "xxxaxbxxx");
        ConfigData config = this.instance.getConfigData();
        GuiStateElement gender = new GuiStateElement('a', new GuiStateElement.State(change -> config.save(ConfigData.Keys.USE_GENDER, true), "true", XMaterial.CHAINMAIL_HELMET.parseItem(), "§aGender §7(§aenabled§7)"), new GuiStateElement.State(change -> config.save(ConfigData.Keys.USE_GENDER, false), "false", XMaterial.CHAINMAIL_HELMET.parseItem(), "§aGender §7(§cdisabled§7)"));
        gender.setState(instance.getConfigData().getBoolean(ConfigData.Keys.USE_GENDER).toString());
        gui.addElement(gender);

        GuiStateElement colors = new GuiStateElement('b', new GuiStateElement.State(change -> config.save(ConfigData.Keys.HANDLE_COLORS, true), "true", XMaterial.LIME_DYE.parseItem(), "§aHandle colors §7(§aenabled§7)"), new GuiStateElement.State(change -> config.save(ConfigData.Keys.HANDLE_COLORS, false), "false", XMaterial.LIME_DYE.parseItem(), "§aHandle colors §7(§cdisabled§7)"));
        colors.setState(instance.getConfigData().getBoolean(ConfigData.Keys.HANDLE_COLORS).toString());
        gui.addElement(colors);

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
        gui.addElement(new StaticGuiElement('a', Color.GREEN.toItemStack(), click -> {
            easyGroup.delete();
            if (easyGroup instanceof Group) openGroupsList();
            else openSubgroupsList();
            return true;
        }, "§aYes"));

        gui.addElement(new StaticGuiElement('b', Color.RED.toItemStack(), click -> {
            openProfile(easyGroup);
            return true;
        }, "§cNo"));

        gui.show(user.getPlayer());
    }

    private void openUserGroupsListPage() {
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), setTitle(Message.GUI_SETTINGS_TITLE_LAYOUT), Arrays.asList("aaaaaaaaa", "aaaaaaaaa"));
        GuiElementGroup elementGroup = new GuiElementGroup('a');

        final String loreSelectPrefix = Message.BTN_SELECT_PREFIX.getText();
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
            } else {
                lore.add(" ");
                lore.add(loreSelectPrefix);
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
            TextInput textInput = new TextInput(user, "§cType in the prefix", prefix);
            textInput.onComplete((input) -> {
                group.setPrefix(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
            }).build();
            return true;
        }, "§aChange Prefix", DIVIDER, "§7Current: §7«§f" + group.getPrefix() + "§7»", " "));

        gui.addElement(new StaticGuiElement('b', XMaterial.GOLD_INGOT.parseItem(), click -> {
            String suffix = group.getSuffix();
            suffix = suffix == null ? " " : suffix.replace("§", "&");
            TextInput textInput = new TextInput(user, "§cType in the prefix", suffix);
            textInput.onComplete((input) -> {
                group.setSuffix(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
            }).build();
            return true;
        }, "§aChange Suffix", DIVIDER, "§7Current: §7«§f" + group.getSuffix() + "§7»", " "));

        String groupChatColor = group.getChatColor().getCode().replace("§", "&");
        if (group.getChatFormatting() != null) {
            if (group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                groupChatColor = ChatFormatting.RAINBOW.toString();
            } else {
                groupChatColor += group.getChatFormatting().getCode().replace("§", "&");
            }
        }
        gui.addElement(new StaticGuiElement('c', XMaterial.LIME_DYE.parseItem(), click -> {
            openPageColorGroup(group);
            return true;
        }, "§aChange Color", DIVIDER, "§7Current: §f" + groupChatColor, " "));

        gui.addElement(new StaticGuiElement('d', XMaterial.BLAZE_ROD.parseItem(), click -> {
            String joinMsg = group.getJoinMessage();
            joinMsg = joinMsg == null ? " " : joinMsg.replace("§", "&");
            TextInput textInput = new TextInput(user, "§cType in the join message", joinMsg);
            textInput.onComplete((input) -> {
                group.setJoinMessage(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
            }).build();
            return true;
        }, "§aJoin Message", DIVIDER, "§7Current: §7«§f" + group.getJoinMessage() + "§7»", " "));

        gui.addElement(new StaticGuiElement('e', XMaterial.STICK.parseItem(), click -> {
            String quitMsg = group.getQuitMessage();
            quitMsg = quitMsg == null ? " " : quitMsg.replace("§", "&");
            TextInput textInput = new TextInput(user, "§cType in the quit message", quitMsg);
            textInput.onComplete((input) -> {
                group.setQuitMessage(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
            }).build();
            return true;
        }, "§cQuit Message", DIVIDER, "§7Current: §7«§f" + group.getQuitMessage() + "§7»", " "));

        gui.addElement(new StaticGuiElement('f', getPlayerHead(), click -> {
            openPageGroupGender(group);
            return true;
        }, "§aGendered Layout", " "));

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
        InventoryGui gui = GuiCreator.createStatic(user.getPlayer(), group.getGroupColor() + group.getName() + " §8» " + Message.GUI_SETTINGS_TITLE_FORMATTINGS.getText(), Arrays.asList("aaaaaaaaa", " aaaaaaa ", "  bbbbb  "));

        GuiElementGroup groupColors = new GuiElementGroup('a');
        for (Color color : Color.getValues()) {
            ItemStack itemStack = color.toItemStack();
            if (group.getChatColor().equals(color) && (group.getChatFormatting() == null || !group.getChatFormatting().equals(ChatFormatting.RAINBOW))) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemStack.setItemMeta(meta);
                }
            }

            groupColors.addElement(new StaticGuiElement('a', itemStack, click -> {
                group.setChatColor(color);
                openPageColorGroup(group);
                return true;
            }, "§r" + color, " ", "§7Permission: §fEasyPrefix.color." + color.name().toLowerCase()));
        }

        GuiElementGroup groupFormattings = new GuiElementGroup('b');
        for (ChatFormatting chatFormatting : ChatFormatting.getValues()) {
            ItemStack itemStack = new ItemStack(Material.BOOKSHELF);
            if (group.getChatFormatting() != null && group.getChatFormatting().equals(chatFormatting)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemStack.setItemMeta(meta);
                }
            }

            groupFormattings.addElement(new StaticGuiElement('b', itemStack, click -> {
                ChatFormatting formatting = chatFormatting;
                if (group.getChatFormatting() != null && group.getChatFormatting().equals(chatFormatting)) {
                    if (!group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                        formatting = null;
                    }
                }
                group.setChatFormatting(formatting);
                openPageColorGroup(group);
                return true;
            }, "§r" + group.getChatColor().getCode() + chatFormatting, " ", "§7Permission: §fEasyPrefix.color." + chatFormatting.name().toLowerCase()));
        }

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
            user.setChatColor(null);
            user.setChatFormatting(null);
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
            TextInput textInput = new TextInput(user, "§cType in the prefix", prefix);
            textInput.onComplete((input) -> {
                subgroup.setPrefix(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
            }).build();
            return true;
        }, "§aChange Prefix", DIVIDER, "§7Current: §7«§f" + subgroup.getPrefix() + "§7»", " "));

        gui.addElement(new StaticGuiElement('b', XMaterial.GOLD_INGOT.parseItem(), click -> {
            String suffix = subgroup.getSuffix();
            suffix = suffix == null ? " " : suffix.replace("§", "&");
            TextInput textInput = new TextInput(user, "§cType in the suffix", suffix);
            textInput.onComplete((input) -> {
                subgroup.setSuffix(input);
                user.sendAdminMessage(Message.INPUT_SAVED);
            }).build();
            return true;
        }, "§aChange Suffix", DIVIDER, "§7Current: §7«§f" + subgroup.getSuffix() + "§7»", " "));


        gui.addElement(new StaticGuiElement('f', getPlayerHead(), click -> {
            openPageGroupGender(subgroup);
            return true;
        }, "§aGendered Layout", " "));

        gui.addElement(new StaticGuiElement('q', new ItemStack(Material.BARRIER), click -> {
            openPageDeleteGroup(subgroup);
            return true;
        }, "§4Delete", " "));

        gui.show(user.getPlayer());
    }

    private void openGroupCreator() {
        TextInput textInput = new TextInput(user, "§cType in the name", "ExampleGroup");
        textInput.onComplete((input) -> {
            String name = input.replaceAll("[^a-zA-Z0-9_]", "");
            if (this.instance.getGroupHandler().createGroup(name)) {
                user.sendAdminMessage("&aGroup '" + name + "' has been created!");
            } else {
                user.sendAdminMessage("§cCouldn't create group!");
            }
        }).build();
    }

    private void openTagCreator() {
        TextInput textInput = new TextInput(user, "§cType in the name", "ExampleGroup");
        textInput.onComplete((input) -> {
            String name = input.replaceAll("[^a-zA-Z0-9_]", "");
            if (this.instance.getGroupHandler().createSubgroup(name)) {
                user.sendAdminMessage("&aTag '" + name + "' has been created!");
            } else {
                user.sendAdminMessage("§cCouldn't create tag!");
            }
        }).build();
    }

    private String setTitle(Message sub) {
        return TITLE.replace("%page%", sub.getText());
    }

    private List<String> replaceInList(@NotNull List<String> list, @NotNull String value) {
        return list.stream().map(val -> val.replace("%content%", value)).collect(Collectors.toList());
    }

    private ItemStack getPlayerHead() {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        if (head != null) {
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                SkullUtils.applySkin(meta, user.getPlayer().getName());
            }
            head.setItemMeta(meta);
        }
        return head;
    }

}
