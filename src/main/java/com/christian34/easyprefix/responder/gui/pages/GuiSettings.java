package com.christian34.easyprefix.responder.gui.pages;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.responder.ChatRespond;
import com.christian34.easyprefix.responder.GuiRespond;
import com.christian34.easyprefix.responder.gui.ClickAction;
import com.christian34.easyprefix.responder.gui.Icon;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.VersionController;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GuiSettings {
    private final String TITLE = Message.SETTINGS_TITLE.toString();
    private final User user;

    public GuiSettings(User user) {
        this.user = user;
    }

    public void openWelcomePage() {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.SETTINGS_TITLE_MAIN), 3);
        Icon prefix = guiRespond.addIcon(XMaterial.CHEST.parseItem(), Message.BTN_MY_PREFIXES, 2, 3).onClick(() -> {
            int userGroups = user.getAvailableGroups().size();
            if (userGroups <= 1) {
                if (user.getAvailableSubgroups().size() > 1) {
                    openSubgroupsPage(this::openWelcomePage);
                } else {
                    openCustomLayoutPage(this::openWelcomePage);
                }
            } else {
                openGroupsListPage();
            }
        });
        Icon formattings = guiRespond.addIcon(XMaterial.CHEST.parseItem(), Message.BTN_MY_FORMATTINGS, 2, 7).onClick(this::openColorsPage);
        if (ConfigKeys.USE_GENDER.toBoolean()) {
            guiRespond.addIcon(Icon.playerHead(user.getPlayer().getName()), Message.CHANGE_GENDER, 2, 5).onClick(this::openGenderSelectPage);
        } else {
            prefix.setSlot(2, 4);
            formattings.setSlot(2, 6);
        }

        guiRespond.addCloseButton();
        guiRespond.openInventory();
    }

    public void openGenderSelectPage() {
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.TITLE_GENDER), 3);
        String genderName = "n/A";
        if (user.getGenderType() != null) genderName = user.getGenderType().getDisplayName();
        List<String> lore = Arrays.asList(" ", Message.LORE_CHANGE_GENDER.toString());
        guiRespond.addIcon(Icon.playerHead(user.getPlayer().getName()), genderName, 2, 5).setLore(lore).onClick(() -> {
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
            openGenderSelectPage();
        });

        guiRespond.addCloseButton().onClick(this::openWelcomePage);
        guiRespond.openInventory();
    }

    public void openGroupsListPage() {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.SETTINGS_TITLE_LAYOUT), 5);

        for (Group group : user.getAvailableGroups()) {
            ChatColor prefixColor = group.getGroupColor();
            List<String> lore = new ArrayList<>();
            ItemStack itemStack = XMaterial.BOOK.parseItem();

            if (user.getGroup().getName().equals(group.getName())) {
                if (itemStack != null) {
                    itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
                }
            } else {
                lore.add(" ");
                lore.add(Message.BTN_SELECT_PREFIX.toString());
            }

            guiRespond.addIcon(itemStack, prefixColor + group.getName()).setLore(lore).onClick(() -> {
                user.setGroup(group, false);
                openGroupsListPage();
            });
        }

        if (ConfigKeys.CUSTOM_LAYOUT.toBoolean() && user.hasPermission("custom.gui")) {
            guiRespond.addIcon(new ItemStack(Material.NETHER_STAR), Message.BTN_CUSTOM_PREFIX, 5, 9)
                    .onClick(() -> openCustomLayoutPage(this::openGroupsListPage));
        }

        if (user.getAvailableSubgroups().size() > 0) {
            ItemStack subgroupsMaterial = VersionController.getMinorVersion() <= 12
                    ? XMaterial.CHEST.parseItem()
                    : XMaterial.WRITABLE_BOOK.parseItem();
            guiRespond.addIcon(subgroupsMaterial, Message.BTN_SUBGROUPS, 5, 5).onClick(() -> openSubgroupsPage(this::openGroupsListPage));
        }

        guiRespond.addCloseButton().onClick(this::openWelcomePage);
        guiRespond.openInventory();
    }

    public void openColorsPage() {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.SETTINGS_TITLE_FORMATTINGS), 5);
        boolean showAll = ConfigKeys.GUI_SHOW_ALL_CHATCOLORS.toBoolean();

        int line = 2, slot = 1;
        for (Color color : Color.getValues()) {
            if (!showAll && !user.getPlayer().hasPermission("EasyPrefix.Color." + color.name().toLowerCase())) {
                continue;
            }
            if (line == 3 && slot == 1) slot++;
            ItemStack itemStack = color.toItemStack();
            if (user.getChatColor().equals(color) && (user.getChatFormatting() == null || !user.getChatFormatting().equals(ChatFormatting.RAINBOW)))
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);

            guiRespond.addIcon(itemStack, "§r" + Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName(), line, +slot).onClick(() -> {
                if (user.getPlayer().hasPermission("EasyPrefix.Color." + color.name().toLowerCase())) {
                    user.setChatColor(color);
                    openColorsPage();
                } else {
                    user.sendMessage(Message.NO_PERMS.toString());
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
        for (ChatFormatting chatFormatting : ChatFormatting.getValues()) {
            if (!showAll && !user.getPlayer().hasPermission("EasyPrefix.Color." + chatFormatting.name().toLowerCase())) {
                continue;
            }
            List<String> lore = Messages.getList(Message.LORE_SELECT_COLOR);
            if (chatFormatting.equals(ChatFormatting.RAINBOW)) {
                lore.remove(lore.size() - 1);
            }
            ItemStack itemStack = new ItemStack(Material.BOOKSHELF);
            if (user.getChatFormatting() != null && user.getChatFormatting().equals(chatFormatting)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
            }
            guiRespond.addIcon(itemStack, "§r" + chatFormatting.toString(), line, slot).setLore(lore).onClick(() -> {
                ChatFormatting formatting = chatFormatting;
                if (user.getPlayer().hasPermission("EasyPrefix.Color." + formatting.name().toLowerCase())) {
                    if (user.getChatFormatting() != null && user.getChatFormatting().equals(formatting)) {
                        if (!user.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                            formatting = null;
                        }
                    }
                    user.setChatFormatting(formatting);
                    openColorsPage();
                } else {
                    user.sendMessage(Message.NO_PERMS.toString());
                }
            });
            slot++;
        }

        guiRespond.addIcon(Material.BARRIER, Message.BTN_RESET, 5, 9).onClick(() -> {
            user.setChatColor(null);
            user.setChatFormatting(null);
            openColorsPage();
        });

        guiRespond.addCloseButton().onClick(this::openWelcomePage);
        guiRespond.openInventory();
    }

    public void openSubgroupsPage(ClickAction backAction) {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.SETTINGS_TITLE_LAYOUT), 5);
        for (Subgroup subgroup : user.getAvailableSubgroups()) {
            List<String> lore = new ArrayList<>();
            ItemStack bookItem = XMaterial.BOOK.parseItem();

            if (user.getSubgroup() != null && user.getSubgroup().equals(subgroup)) {
                if (bookItem != null) {
                    bookItem.addUnsafeEnchantment(Enchantment.LUCK, 1);
                }
            } else {
                lore.add(" ");
                lore.add(Message.BTN_SELECT_PREFIX.toString());
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

        if (ConfigKeys.CUSTOM_LAYOUT.toBoolean() && user.hasPermission("custom.gui")) {
            guiRespond.addIcon(new ItemStack(Material.NETHER_STAR), Message.BTN_CUSTOM_PREFIX, 5, 9)
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
        if (!ConfigKeys.CUSTOM_LAYOUT.toBoolean() || !user.hasPermission("custom.gui")) {
            if (backAction != null) {
                backAction.execute();
            } else openGroupsListPage();
        }
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.SETTINGS_TITLE_LAYOUT), 3);
        final String divider = "§7--------------------";

        String loreDetail = Message.LORE_GROUP_DETAIL.toString();
        String loreEdit = Message.LORE_EDIT.toString();

        List<String> prefixLore = Arrays.asList(divider, loreDetail + user.getPrefix().replace("§", "&"), " ", loreEdit);

        guiRespond.addIcon(Material.IRON_INGOT, Message.BTN_CHANGE_PREFIX, 2, 4).setLore(prefixLore).onClick(() -> {
            ChatRespond responder = new ChatRespond(user, Message.CHAT_INPUT_PREFIX.toString()
                    .replace("%prefix%", user.getPrefix().replace("§", "&")));

            responder.getInput((respond) -> Bukkit.getScheduler().runTask(EasyPrefix.getInstance(), () ->
                    user.getPlayer().performCommand("ep setprefix " + respond))
            );
        });

        List<String> suffixLore = Arrays.asList(divider, loreDetail + user.getSuffix().replace("§", "&"), " ", loreEdit);

        guiRespond.addIcon(Material.GOLD_INGOT, Message.BTN_CHANGE_SUFFIX.toString(), 2, 6).setLore(suffixLore).onClick(() -> {
            ChatRespond responder = new ChatRespond(user, Message.CHAT_INPUT_SUFFIX.toString()
                    .replace("%suffix%", user.getSuffix().replace("§", "&")));

            responder.getInput((respond) -> Bukkit.getScheduler().runTask(EasyPrefix.getInstance(), () ->
                    user.getPlayer().performCommand("ep setsuffix " + respond))
            );
        });

        guiRespond.addIcon(Material.BARRIER, Message.BTN_RESET.toString(), 3, 9).onClick(() -> {
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

    private String setTitle(Message sub) {
        return TITLE.replace("%page%", sub.toString());
    }

}
