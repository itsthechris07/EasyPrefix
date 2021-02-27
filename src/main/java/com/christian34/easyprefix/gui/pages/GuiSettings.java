package com.christian34.easyprefix.gui.pages;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.gui.ClickAction;
import com.christian34.easyprefix.gui.GuiRespond;
import com.christian34.easyprefix.gui.Icon;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.*;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * EasyPrefix 2021.
 *
 * @author Christian34
 */
public class GuiSettings {
    private final String TITLE = Message.GUI_SETTINGS_TITLE.getText();
    private final User user;
    private final EasyPrefix instance;

    public GuiSettings(User user) {
        this.user = user;
        this.instance = EasyPrefix.getInstance();
    }

    public void openWelcomePage() {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.GUI_SETTINGS_TITLE_MAIN), 3);
        Icon prefix = guiRespond.addIcon(XMaterial.CHEST.parseItem(), Message.BTN_MY_PREFIXES, 2, 3).onClick(() -> {
            int userGroups = user.getAvailableGroups().size();
            if (userGroups <= 1) {
                openSubgroupsPage(this::openWelcomePage);
            } else {
                openGroupsListPage();
            }
        });
        Icon formattings = guiRespond.addIcon(XMaterial.CHEST.parseItem(), Message.BTN_MY_FORMATTINGS, 2, 7).onClick(() -> openColorsPage(null));
        if (instance.getConfigData().getBoolean(ConfigData.Keys.USE_GENDER)) {
            guiRespond.addIcon(Icon.playerHead(user.getPlayer().getName()), Message.BTN_CHANGE_GENDER, 2, 5).onClick(() -> openGenderSelectPage(null));
        } else {
            prefix.setSlot(2, 4);
            formattings.setSlot(2, 6);
        }

        guiRespond.addCloseButton();
        guiRespond.openInventory();
    }

    public void openGenderSelectPage(ClickAction backAction) {
        GroupHandler groupHandler = this.instance.getGroupHandler();
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.GUI_SETTINGS_TITLE_GENDER), 3);
        String genderName = "n/A";
        if (user.getGenderType() != null) genderName = user.getGenderType().getDisplayName();
        guiRespond.addIcon(Icon.playerHead(user.getPlayer().getName()), genderName, 2, 5).onClick(() -> {
            if (user.getGenderType() == null) {
                user.setGenderType(groupHandler.getGenderTypes().get(0));
            } else {
                List<Gender> genders = groupHandler.getGenderTypes();
                int index = genders.indexOf(user.getGenderType());
                if (index + 1 >= genders.size()) {
                    index = 0;
                } else index++;
                Gender nextGender = groupHandler.getGenderTypes().get(index);
                user.setGenderType(nextGender);
            }
            openGenderSelectPage(backAction);
        });

        if (backAction != null) {
            guiRespond.addCloseButton().onClick(backAction);
        } else {
            guiRespond.addCloseButton().onClick(this::openWelcomePage);
        }
        guiRespond.openInventory();
    }

    public void openGroupsListPage() {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.GUI_SETTINGS_TITLE_LAYOUT), 5);

        final String loreSelectPrefix = Message.BTN_SELECT_PREFIX.getText();
        Material book = XMaterial.BOOK.parseMaterial();
        for (Group group : user.getAvailableGroups()) {
            ChatColor prefixColor = group.getGroupColor();
            List<String> lore = new ArrayList<>();
            ItemStack itemStack = new ItemStack(Objects.requireNonNull(book));

            if (user.getGroup().getName().equals(group.getName())) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
            } else {
                lore.add(" ");
                lore.add(loreSelectPrefix);
            }

            guiRespond.addIcon(itemStack, prefixColor + group.getName()).setLore(lore).onClick(() -> {
                user.setGroup(group, false);
                openGroupsListPage();
            });
        }

        if (instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT) && user.hasPermission("custom.gui")) {
            guiRespond.addIcon(new ItemStack(Material.NETHER_STAR), Message.BTN_CUSTOM_LAYOUT, 5, 9)
                    .setLore(Message.BTN_CUSTOM_LAYOUT_LORE.getList())
                    .onClick(() -> openCustomLayoutPage(this::openGroupsListPage));
        }

        if (!user.getAvailableSubgroups().isEmpty()) {
            ItemStack subgroupsMaterial = VersionController.getMinorVersion() <= 12
                    ? XMaterial.CHEST.parseItem()
                    : XMaterial.WRITABLE_BOOK.parseItem();
            guiRespond.addIcon(subgroupsMaterial, Message.BTN_SETTINGS_TAGS, 5, 5).onClick(() -> openSubgroupsPage(this::openGroupsListPage));
        }

        guiRespond.addCloseButton().onClick(this::openWelcomePage);
        guiRespond.openInventory();
    }

    public void openColorsPage(ClickAction backAction) {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.GUI_SETTINGS_TITLE_FORMATTINGS), 5);
        final boolean showAll = instance.getConfigData().getBoolean(ConfigData.Keys.GUI_SHOW_ALL_CHATCOLORS);

        int line = 2, slot = 1;
        List<Color> colors = showAll ? Arrays.asList(Color.getValues()) : new ArrayList<>(user.getColors());
        for (Color color : colors) {
            if (!showAll && !user.hasPermission("color." + color.name())) {
                continue;
            }

            if (line == 3 && slot == 1) slot++;

            ItemStack itemStack = color.toItemStack();
            if (user.getChatColor().equals(color)) {
                if (user.getChatFormatting() == null || !user.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                    itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
                }
            }

            guiRespond.addIcon(itemStack, "§r" + color.toString(), line, +slot).onClick(() -> {
                if (user.hasPermission("color." + color.name())) {
                    user.setChatColor(color);

                    String name = color.getCode();
                    ChatFormatting formatting = user.getChatFormatting();
                    if (formatting != null && (!formatting.equals(ChatFormatting.UNDEFINED)
                            && !formatting.equals(ChatFormatting.INHERIT))) {
                        name += formatting.getCode() + color.getName() + " " + formatting.getName();
                    } else {
                        name += color.getName();
                    }

                    user.sendMessage(Message.COLOR_PLAYER_SELECT.getText().replace("%color%", name));
                    openColorsPage(backAction);
                } else {
                    user.sendMessage(Message.CHAT_NO_PERMS.getText());
                }
            });

            slot++;
            if (slot == 10) {
                slot = 1;
                line++;
            }
        }

        line = 4;
        slot = 3;
        final List<String> infoNotCompatible = Message.LORE_SELECT_COLOR_NC.getList();
        final List<String> loreList = Message.LORE_SELECT_COLOR.getList();
        List<ChatFormatting> formattings = showAll
                ? Arrays.asList(ChatFormatting.getValues())
                : new ArrayList<>(user.getChatFormattings());
        for (ChatFormatting chatFormatting : formattings) {
            if (!showAll && !user.hasPermission("color." + chatFormatting.name())) {
                continue;
            }
            List<String> lore = new ArrayList<>(loreList);
            if (!chatFormatting.equals(ChatFormatting.RAINBOW)) {
                lore.addAll(infoNotCompatible);
            }
            ItemStack itemStack = new ItemStack(Material.BOOKSHELF);
            if (user.getChatFormatting() != null && user.getChatFormatting().equals(chatFormatting)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
            }
            guiRespond.addIcon(itemStack, "§r" + chatFormatting.toString(), line, slot).setLore(lore).onClick(() -> {
                ChatFormatting formatting = chatFormatting;
                if (user.getPlayer().hasPermission("color." + formatting.name().toLowerCase())) {
                    if (user.getChatFormatting() != null && user.getChatFormatting().equals(formatting)) {
                        formatting = ChatFormatting.UNDEFINED;
                    }
                    user.setChatFormatting(formatting);

                    Color color = user.getChatColor();
                    String name;
                    if (formatting.equals(ChatFormatting.RAINBOW)) {
                        name = formatting.toString();
                    } else {
                        if (formatting.equals(ChatFormatting.UNDEFINED)) {
                            name = color.toString();
                        } else {
                            name = color.getCode() + formatting.getCode() + color.getName() + " " + formatting.getName();
                        }
                    }
                    user.sendMessage(Message.COLOR_PLAYER_SELECT.getText().replace("%color%", name));
                    openColorsPage(backAction);
                } else {
                    user.sendMessage(Message.CHAT_NO_PERMS.getText());
                }
            });
            slot++;
        }

        guiRespond.addIcon(Material.BARRIER, Message.BTN_RESET.getText(), 5, 9).onClick(() -> {
            user.setChatColor(null);
            user.setChatFormatting(null);
            openColorsPage(backAction);
        });

        if (backAction != null) {
            guiRespond.addCloseButton().onClick(backAction);
        } else {
            guiRespond.addCloseButton().onClick(this::openWelcomePage);
        }
        guiRespond.openInventory();
    }

    public void openSubgroupsPage(ClickAction backAction) {
        List<Subgroup> subgroups = user.getAvailableSubgroups();
        int lines = (subgroups.size() <= 9) ? 3 : ((subgroups.size() <= 18) ? 4 : 5);
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.GUI_SETTINGS_TITLE_TAGS), lines);

        final Material book = XMaterial.BOOK.parseMaterial();
        final String loreSelectTag = Message.BTN_SETTINGS_SELECT_TAG.getText();
        for (Subgroup subgroup : subgroups) {
            List<String> lore = new ArrayList<>();
            ItemStack bookItem = new ItemStack(Objects.requireNonNull(book));

            if (user.getSubgroup() != null && user.getSubgroup().equals(subgroup)) {
                bookItem.addUnsafeEnchantment(Enchantment.LUCK, 1);
            } else {
                lore.add(" ");
                lore.add(loreSelectTag);
            }

            guiRespond.addIcon(bookItem, subgroup.getGroupColor() + subgroup.getName()).setLore(lore).onClick(() -> {
                if (user.getSubgroup() != null && user.getSubgroup().equals(subgroup)) {
                    user.setSubgroup(null);
                } else {
                    user.setSubgroup(subgroup);
                }
                openSubgroupsPage(backAction);
            });
        }

        if (instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT) && user.hasPermission("custom.gui")) {
            guiRespond.addIcon(new ItemStack(Material.NETHER_STAR), Message.BTN_CUSTOM_LAYOUT, lines, 9)
                    .setLore(Message.BTN_CUSTOM_LAYOUT_LORE.getList())
                    .onClick(() -> openCustomLayoutPage(() -> openSubgroupsPage(this::openWelcomePage)));
        }

        if (backAction != null) {
            guiRespond.addCloseButton().onClick(backAction);
        } else {
            guiRespond.addCloseButton().onClick(this::openWelcomePage);
        }

        guiRespond.openInventory();
    }

    public void openCustomLayoutPage(ClickAction backAction) {
        if (!instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT) || !user.hasPermission("custom.gui")) {
            if (backAction != null) {
                backAction.execute();
            } else openGroupsListPage();
        }
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.GUI_SETTINGS_TITLE_LAYOUT), 3);

        List<String> prefixLore = replaceInList(Message.LORE_CHANGE_PREFIX.getList(),
                Optional.ofNullable(user.getPrefix()).orElse("-"));

        guiRespond.addIcon(Material.IRON_INGOT, Message.BTN_CHANGE_PREFIX.getText(), 2, 4)
                .setLore(prefixLore)
                .onClick(() -> {
                    ChatRespond responder = new ChatRespond(user, Message.CHAT_INPUT_PREFIX.getText()
                            .replace("%content%", Optional.ofNullable(user.getPrefix()).orElse("-")));
                    responder.getInput((respond) -> {
                        // this is just a temporary solution, will be updated in v1.8
                        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(user.getPlayer(),
                                "/ep setprefix " + respond);
                        Bukkit.getScheduler().runTask(instance, () -> {
                            Bukkit.getServer().getPluginManager().callEvent(event);
                        });
                    });
                });

        List<String> suffixLore = replaceInList(Message.LORE_CHANGE_SUFFIX.getList(),
                Optional.ofNullable(user.getSuffix()).orElse("-"));
        guiRespond.addIcon(Material.GOLD_INGOT, Message.BTN_CHANGE_SUFFIX.getText(), 2, 6)
                .setLore(suffixLore)
                .onClick(() -> {
                    ChatRespond responder = new ChatRespond(user, Message.CHAT_INPUT_SUFFIX.getText()
                            .replace("%content%", Optional.ofNullable(user.getSuffix()).orElse("-")));
                    responder.getInput((respond) -> {
                        // this is just a temporary solution, will be updated in v1.8
                        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(user.getPlayer(),
                                "/ep setsuffix " + respond);
                        Bukkit.getScheduler().runTask(instance, () -> {
                            Bukkit.getServer().getPluginManager().callEvent(event);
                        });
                    });
                });

        guiRespond.addIcon(Material.BARRIER, Message.BTN_RESET.getText(), 3, 9).onClick(() -> {
            user.setPrefix(null);
            user.setSuffix(null);
            openCustomLayoutPage(backAction);
        });

        if (backAction != null) {
            guiRespond.addCloseButton().onClick(backAction);
        } else {
            guiRespond.addCloseButton().onClick(this::openGroupsListPage);
        }

        guiRespond.openInventory();
    }

    private List<String> replaceInList(@NotNull List<String> list, @NotNull String value) {
        return list.stream().map(val -> val.replace("%content%", value)).collect(Collectors.toList());
    }

    private String setTitle(Message sub) {
        return TITLE.replace("%page%", sub.getText());
    }

}
