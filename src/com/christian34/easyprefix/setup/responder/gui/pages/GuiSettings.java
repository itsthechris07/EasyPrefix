package com.christian34.easyprefix.setup.responder.gui.pages;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.GenderType;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.responder.ChatRespond;
import com.christian34.easyprefix.setup.responder.GuiRespond;
import com.christian34.easyprefix.setup.responder.gui.Icon;
import com.christian34.easyprefix.setup.responder.gui.Page;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.VersionController;
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
@SuppressWarnings("UnusedReturnValue")
public class GuiSettings extends Page {
    private final String TITLE = Message.SETTINGS_TITLE.toString();
    private final User user;

    public GuiSettings(User user) {
        super(user);
        this.user = user;
    }

    public GuiRespond mainPage() {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.SETTINGS_TITLE_MAIN), 3);
        Icon prefix = guiRespond.addIcon(Material.CHEST, Message.BTN_MY_PREFIXES, 2, 3).addClickAction(() -> {
            if (user.getAvailableGroups().size() <= 1 && user.getAvailableSubgroups().size() > 1) {
                openSubgroupsPage();
            } else {
                openGroupsPage();
            }
        });
        Icon formattings = guiRespond.addIcon(Material.CHEST, Message.BTN_MY_FORMATTINGS, 2, 7).addClickAction(this::openColorsPage);
        if (EasyPrefix.getInstance().getFileManager().getConfig().getBoolean(ConfigData.ConfigKeys.USE_GENDER)) {
            guiRespond.addIcon(Icon.playerHead(user.getPlayer().getName()), Message.CHANGE_GENDER, 2, 5).addClickAction(this::openGenderPage);
        } else {
            prefix.setSlot(2, 4);
            formattings.setSlot(2, 6);
        }

        guiRespond.addCloseButton();
        guiRespond.openInventory();
        return guiRespond;
    }

    public GuiSettings openGenderPage() {
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.TITLE_GENDER), 3);
        String genderName = "n/A";
        if (user.getGenderType() != null) genderName = user.getGenderType().getDisplayName();
        List<String> lore = Arrays.asList(" ", Message.LORE_CHANGE_GENDER.toString());
        guiRespond.addIcon(Icon.playerHead(user.getPlayer().getName()), genderName, 2, 5).setLore(lore).addClickAction(() -> {
            if (user.getGenderType() == null) {
                user.setGenderType(groupHandler.getGenderTypes().get(0));
            } else {
                ArrayList<GenderType> genderTypes = groupHandler.getGenderTypes();
                int index = genderTypes.indexOf(user.getGenderType());
                if (index + 1 >= genderTypes.size()) {
                    index = 0;
                } else index++;
                GenderType nextGenderType = groupHandler.getGenderTypes().get(index);
                user.setGenderType(nextGenderType);
            }
            openGenderPage();
        });

        guiRespond.addCloseButton().addClickAction(this::mainPage);
        guiRespond.openInventory();
        return this;
    }

    public GuiSettings openGroupsPage() {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.SETTINGS_TITLE_LAYOUT), 5);
        ConfigData configData = EasyPrefix.getInstance().getFileManager().getConfig();

        for (Group group : user.getAvailableGroups()) {
            ChatColor prefixColor = group.getGroupColor();
            List<String> lore = new ArrayList<>();
            ItemStack itemStack = new ItemStack(Material.BOOK);

            if (user.getGroup().getName().equals(group.getName())) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
            } else {
                lore.add(" ");
                lore.add(Message.BTN_SELECT_PREFIX.toString());
            }

            guiRespond.addIcon(itemStack, prefixColor + group.getName()).setLore(lore).addClickAction(() -> {
                user.setGroup(group, false);
                openGroupsPage();
            });
        }

        if (configData.getBoolean(ConfigData.ConfigKeys.CUSTOM_LAYOUT) && user.hasPermission("easyprefix.custom.gui")) {
            guiRespond.addIcon(new ItemStack(Material.NETHER_STAR), Message.BTN_CUSTOM_PREFIX, 5, 9).addClickAction(this::openCustomPrefixPage);
        }

        if (user.getAvailableSubgroups().size() > 0) {
            Material subgroupsMaterial = Material.BARRIER;
            try {
                if (VersionController.getMinorVersion() < 12) {
                    subgroupsMaterial = Material.valueOf("CHEST");
                } else {
                    subgroupsMaterial = Material.WRITABLE_BOOK;
                }
            } catch (Exception ignored) {
            }
            guiRespond.addIcon(subgroupsMaterial, Message.BTN_SUBGROUPS, 5, 5).addClickAction(this::openSubgroupsPage);
        }

        guiRespond.addCloseButton().addClickAction(this::mainPage);
        guiRespond.openInventory();
        return this;
    }

    public GuiSettings openSubgroupsPage() {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.SETTINGS_TITLE_LAYOUT), 5);
        for (Subgroup subgroup : user.getAvailableSubgroups()) {
            List<String> lore = new ArrayList<>();
            ItemStack book = new ItemStack(Material.BOOK);

            if (user.getSubgroup() != null && user.getSubgroup().equals(subgroup)) {
                book.addUnsafeEnchantment(Enchantment.LUCK, 1);
            } else {
                lore.add(" ");
                lore.add(Message.BTN_SELECT_PREFIX.toString());
            }

            guiRespond.addIcon(book, subgroup.getGroupColor() + subgroup.getName()).setLore(lore).addClickAction(() -> {
                if (user.getSubgroup() != null && user.getSubgroup().equals(subgroup)) {
                    user.setSubgroup(null);
                } else {
                    user.setSubgroup(subgroup);
                }
                openSubgroupsPage();
            });
        }

        guiRespond.addCloseButton().addClickAction(this::mainPage);
        guiRespond.openInventory();
        return this;
    }

    public GuiSettings openColorsPage() {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.SETTINGS_TITLE_FORMATTINGS), 5);
        boolean showAll = EasyPrefix.getInstance().getFileManager().getConfig().getBoolean(ConfigData.ConfigKeys.GUI_SHOW_ALL_CHATCOLORS);

        int line = 2, slot = 1;
        for (Color color : Color.getValues()) {
            if (!showAll && !user.getPlayer().hasPermission("EasyPrefix.Color." + color.name().toLowerCase())) {
                continue;
            }
            if (line == 3 && slot == 1) slot++;
            ItemStack itemStack = color.toItemStack();
            if (user.getChatColor() != null && user.getChatColor().equals(color) && (user.getChatFormatting() == null || !user.getChatFormatting().equals(ChatFormatting.RAINBOW)))
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);

            guiRespond.addIcon(itemStack, "§r" + Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName(), line, +slot).addClickAction(() -> {
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
            guiRespond.addIcon(itemStack, "§r" + chatFormatting.toString(), line, slot).setLore(lore).addClickAction(() -> {
                ChatFormatting formatting = chatFormatting;
                if (user.getPlayer().hasPermission("EasyPrefix.Color." + formatting.name().toLowerCase())) {
                    if (user.getChatFormatting() != null && user.getChatFormatting().equals(formatting)) {
                        if (!user.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                            formatting = null;
                        }
                    }
                    if (formatting != null && !formatting.equals(ChatFormatting.RAINBOW) && user.getChatColor() == null) {
                        return;
                    }
                    user.setChatFormatting(formatting);
                    openColorsPage();
                } else {
                    user.sendMessage(Message.NO_PERMS.toString());
                }
            });
            slot++;
        }

        guiRespond.addCloseButton().

                addClickAction(this::mainPage);
        guiRespond.openInventory();
        return this;
    }

    public GuiSettings openCustomPrefixPage() {
        GuiRespond guiRespond = new GuiRespond(user, setTitle(Message.SETTINGS_TITLE_LAYOUT), 3);
        final String divider = "§7--------------------";

        String loreDetail = Message.LORE_GROUP_DETAIL.toString();
        String loreEdit = Message.LORE_EDIT.toString();

        List<String> prefixLore = Arrays.asList(divider, loreDetail + user.getPrefix().replace("§", "&"), " ", loreEdit);

        guiRespond.addIcon(Material.IRON_INGOT, Message.BTN_CHANGE_PREFIX, 2, 4).setLore(prefixLore).addClickAction(() -> {
            ChatRespond responder = new ChatRespond(user, Message.CHAT_INPUT_PREFIX.toString().replace("%prefix%", user.getPrefix().replace("§", "&")));
            responder.getInput((respond) -> {
                user.setPrefix(respond);
                user.sendMessage(Message.INPUT_SAVED.toString());
            });
        });

        List<String> suffixLore = Arrays.asList(divider, loreDetail + user.getSuffix().replace("§", "&"), " ", loreEdit);

        guiRespond.addIcon(Material.GOLD_INGOT, Message.BTN_CHANGE_SUFFIX.toString(), 2, 6).setLore(suffixLore).addClickAction(() -> {
            ChatRespond responder = new ChatRespond(user, Message.CHAT_INPUT_SUFFIX.toString().replace("%suffix%", user.getSuffix().replace("§", "&")));
            responder.getInput((respond) -> {
                user.setSuffix(respond);
                user.sendMessage(Message.INPUT_SAVED.toString());
            });
        });

        guiRespond.addIcon(Material.BARRIER, Message.BTN_RESET.toString(), 3, 9).addClickAction(() -> {
            user.setPrefix(null);
            user.setSuffix(null);
            openCustomPrefixPage();
        });

        guiRespond.addCloseButton().addClickAction(this::openGroupsPage);
        guiRespond.openInventory();
        return this;
    }

    private String setTitle(Message sub) {
        return TITLE.replace("%page%", sub.toString());
    }

}