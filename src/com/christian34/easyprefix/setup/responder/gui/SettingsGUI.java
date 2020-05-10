package com.christian34.easyprefix.setup.responder.gui;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.GenderType;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.Button;
import com.christian34.easyprefix.setup.CustomInventory;
import com.christian34.easyprefix.setup.responder.ChatRespond;
import com.christian34.easyprefix.setup.responder.GuiRespond;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.VersionController;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class SettingsGUI {
    private final User user;
    private GroupHandler groupHandler;

    public SettingsGUI(User user) {
        this.user = user;
        this.groupHandler = EasyPrefix.getInstance().getGroupHandler();
        openMainPage();
    }

    private User getUser() {
        return user;
    }

    public void openGenderPage() {
        CustomInventory inventory = new CustomInventory(Messages.getText(Message.SETTINGS_TITLE).replace("%page%", Messages.getText(Message.TITLE_GENDER)), 3);
        String genderName = "n/A";
        if (user.getGenderType() != null) genderName = user.getGenderType().getDisplayName();
        Button crntGender = new Button(Button.playerHead(user.getName()), genderName).setSlot(2, 5);
        crntGender.setLore(" ", Messages.getText(Message.LORE_CHANGE_GENDER));
        inventory.addItem(crntGender);
        new GuiRespond(getUser(), inventory, (btn) -> {
            if (btn.equals(crntGender)) {
                if (user.getGenderType() == null) {
                    user.setGenderType(this.groupHandler.getGenderTypes().get(0));
                } else {
                    ArrayList<GenderType> genderTypes = this.groupHandler.getGenderTypes();
                    int index = genderTypes.indexOf(user.getGenderType());
                    if (index + 1 >= genderTypes.size()) {
                        index = 0;
                    } else index++;
                    GenderType nextGenderType = this.groupHandler.getGenderTypes().get(index);
                    user.setGenderType(nextGenderType);
                }
                openGenderPage();
            } else {
                openMainPage();
            }
        });
    }

    public void openGroupsPage() {
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        CustomInventory inventory = new CustomInventory(Messages.getText(Message.SETTINGS_TITLE).replace("%page%", Messages.getText(Message.SETTINGS_TITLE_PREFIXES)), 5);
        int counter = 9;
        for (Group group : user.getAvailableGroups()) {
            ChatColor prefixColor = group.getGroupColor();
            List<String> lore = new ArrayList<>();
            Material material = Material.BOOK;
            Button button = new Button(material, prefixColor + group.getName()).setSlot(counter);
            if (user.getGroup().getName().equals(group.getName())) {
                button.addEnchantment();
            } else {
                lore.add(" ");
                lore.add(Messages.getText(Message.BTN_SELECT_PREFIX, user));
            }
            button.setLore(lore);
            inventory.addItem(button);
            counter++;
        }
        Material subgroupsMaterial = Material.BARRIER;
        try {
            if (VersionController.getMinorVersion() < 12) {
                subgroupsMaterial = Material.valueOf("CHEST");
            } else {
                subgroupsMaterial = Material.WRITABLE_BOOK;
            }
        } catch(Exception ignored) {
        }
        Button subgroups = new Button(subgroupsMaterial, Messages.getText(Message.BTN_SUBGROUPS)).setSlot(5, 5);
        if (user.getAvailableSubgroups().size() > 0) {
            inventory.addItem(subgroups);
        }
        Button custom = new Button(Material.NETHER_STAR, Messages.getText(Message.BTN_CUSTOM_PREFIX)).setSlot(counter);
        if (user.getPlayer().hasPermission("easyprefix.settings.custom")) {
            inventory.addItem(custom);
        }
        new GuiRespond(user, inventory, (btn) -> {
            if (btn.getDisplayName().equals(Messages.getText(Message.BTN_BACK))) {
                openMainPage();
            } else if (btn.getDisplayName().equals(Messages.getText(Message.BTN_CUSTOM_PREFIX))) {
                openCustomPrefixPage();
            } else if (groupHandler.isGroup(btn.getDisplayName().substring(2))) {
                user.setGroup(groupHandler.getGroup(btn.getDisplayName().substring(2)), false);
                openGroupsPage();
            } else if (btn.equals(subgroups)) {
                openSubgroupsPage();
            }
        });
    }

    public void openSubgroupsPage() {
        CustomInventory inventory = new CustomInventory(Messages.getText(Message.SETTINGS_TITLE).replace("%page%", Messages.getText(Message.SETTINGS_TITLE_PREFIXES)), 5);
        int counter = 9;
        for (Subgroup subgroup : user.getAvailableSubgroups()) {
            List<String> lore = new ArrayList<>();

            Button button = new Button(Material.BOOK, subgroup.getGroupColor() + subgroup.getName()).setSlot(counter);
            if (user.getSubgroup() != null && user.getSubgroup().equals(subgroup)) {
                button.addEnchantment();
            } else {
                lore.add(" ");
                lore.add(Messages.getText(Message.BTN_SELECT_PREFIX, user));
            }
            button.setLore(lore);
            inventory.addItem(button);
            counter++;
        }
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        new GuiRespond(user, inventory, (btn) -> {
            if (btn.getDisplayName().equals(Messages.getText(Message.BTN_BACK))) {
                openMainPage();
            } else if (btn.getDisplayName().equals(Messages.getText(Message.BTN_CUSTOM_PREFIX))) {
                openCustomPrefixPage();
            } else if (groupHandler.isSubgroup(btn.getDisplayName().substring(2))) {
                Subgroup subgroup = groupHandler.getSubgroup(btn.getDisplayName().substring(2));
                if (user.getSubgroup() != null && user.getSubgroup().equals(subgroup)) {
                    user.setSubgroup(null);
                } else {
                    user.setSubgroup(subgroup);
                }
                openSubgroupsPage();
            }
        });
    }

    public void openColorsPage() {
        CustomInventory inventory = new CustomInventory(Messages.getText(Message.SETTINGS_TITLE).replace("%page%", Messages.getText(Message.SETTINGS_TITLE_FORMATTINGS)), 5);
        int colorSlot = 9;
        boolean showAll = EasyPrefix.getInstance().getFileManager().getConfig().getBoolean(ConfigData.ConfigKeys.GUI_SHOW_ALL_CHATCOLORS);
        for (Color color : Color.values()) {
            if (color.equals(Color.UNDEFINED)) continue;
            if (showAll || user.getPlayer().hasPermission("EasyPrefix.Color." + color.name().toLowerCase())) {
                if (showAll && colorSlot == 18) colorSlot++;
                Button button = color.toTerracotta().setSlot(colorSlot);
                button.setDisplayName(color.toString());
                button.setData("color", color.name());
                if (user.getChatColor().equals(color) && (user.getChatFormatting() == null || !user.getChatFormatting().equals(ChatFormatting.RAINBOW))) {
                    button.addEnchantment();
                }
                inventory.addItem(button);
                colorSlot++;
            }
        }
        int formattingsSlot = (showAll) ? 29 : 27;
        for (ChatFormatting chatFormatting : ChatFormatting.getValues()) {
            if (showAll || user.getPlayer().hasPermission("EasyPrefix.Color." + chatFormatting.name().toLowerCase())) {
                List<String> lore = Messages.getList(Message.LORE_SELECT_COLOR);
                Button button = new Button(Material.BOOKSHELF, chatFormatting.toString()).setSlot(formattingsSlot);
                if (!chatFormatting.equals(ChatFormatting.RAINBOW)) {
                    button.setLore(lore);
                }
                button.setData("formatting", chatFormatting.name());
                if (user.getChatFormatting() != null && user.getChatFormatting().equals(chatFormatting))
                    button.addEnchantment();
                inventory.addItem(button);
                formattingsSlot++;
            }
        }
        inventory.addItem(new Button(Material.BARRIER, Messages.getText(Message.BTN_RESET), null).setSlot(5, 9));
        new GuiRespond(user, inventory, (respond) -> {
            String name = respond.getDisplayName();
            if (name.equals(Messages.getText(Message.BTN_BACK))) {
                openMainPage();
            } else if (name.equals(Messages.getText(Message.BTN_RESET))) {
                user.setChatColor(null);
                user.setChatFormatting(null);
                openColorsPage();
            } else {
                if (respond.getData("color") != null) {
                    Color color = Color.valueOf(respond.getData("color"));
                    if (user.getPlayer().hasPermission("EasyPrefix.Color." + color.name().toLowerCase())) {
                        if (!color.equals(Color.UNDEFINED)) {
                            if (user.getChatColor() != null && user.getChatColor().equals(color)) {
                                return;
                            }
                            user.setChatColor(color);
                            openColorsPage();
                        }
                    } else {
                        user.sendMessage(Messages.getText(Message.NO_PERMS, user));
                    }
                } else if (respond.getData("formatting") != null) {
                    ChatFormatting formatting = ChatFormatting.valueOf(respond.getData("formatting"));
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
                        user.sendMessage(Messages.getText(Message.NO_PERMS, user));
                    }
                }
            }
        });
    }

    private void openMainPage() {
        CustomInventory inventory = new CustomInventory(Messages.getText(Message.SETTINGS_TITLE).replace("%page%", Messages.getText(Message.SETTINGS_TITLE_MAIN)), 3);
        Button prefix = new Button(Material.CHEST, Messages.getText(Message.BTN_MY_PREFIXES)).setSlot(2, 3).setLore(Messages.getText(Message.LORE_CHANGE_PREFIX, user), " ");
        Button formattings = new Button(Material.CHEST, Messages.getText(Message.BTN_MY_FORMATTINGS)).setSlot(2, 7).setLore(Messages.getText(Message.LORE_CHANGE_CHATCOLOR, user), " ");
        if (EasyPrefix.getInstance().getFileManager().getConfig().getBoolean(ConfigData.ConfigKeys.USE_GENDER)) {
            Button gender = new Button(Button.playerHead(user.getName()), Messages.getText(Message.CHANGE_GENDER)).setSlot(2, 5).setLore(Messages.getText(Message.LORE_CHANGE_GENDER), " ");
            inventory.addItem(gender);
        } else {
            prefix.setSlot(2, 4);
            formattings.setSlot(2, 6);
        }
        inventory.addItem(prefix);
        inventory.addItem(formattings);
        new GuiRespond(user, inventory, (btn) -> {
            String name = btn.getDisplayName();
            if (name.equals(Messages.getText(Message.BTN_BACK))) {
                user.getPlayer().closeInventory();
            } else if (btn.equals(prefix)) {
                if (user.getAvailableGroups().size() <= 1 && user.getAvailableSubgroups().size() > 1) {
                    openSubgroupsPage();
                } else {
                    openGroupsPage();
                }
            } else if (btn.equals(formattings)) {
                openColorsPage();
            } else if (btn.getDisplayName().equals(Messages.getText(Message.CHANGE_GENDER))) {
                openGenderPage();
            }
        });
    }

    private void openCustomPrefixPage() {
        CustomInventory inventory = new CustomInventory(Messages.getText(Message.SETTINGS_TITLE).replace("%page%", Messages.getText(Message.SETTINGS_TITLE_PREFIXES)), 5);
        String divider = "§7--------------------";
        inventory.addItem(new Button(Material.IRON_INGOT, Messages.getText(Message.BTN_CHANGE_PREFIX)).setSlot(3, 4).setLore(divider, Messages.getText(Message.LORE_GROUP_DETAIL) + user.getPrefix().replace("§", "&"), " ", Messages.getText(Message.LORE_EDIT)));
        inventory.addItem(new Button(Material.GOLD_INGOT, Messages.getText(Message.BTN_CHANGE_SUFFIX)).setSlot(3, 6).setLore(divider, Messages.getText(Message.LORE_GROUP_DETAIL) + user.getSuffix().replace("§", "&"), " ", Messages.getText(Message.LORE_EDIT)));
        inventory.addItem(new Button(Material.BARRIER, Messages.getText(Message.BTN_RESET)).setSlot(5, 9));
        new GuiRespond(user, inventory, (btn) -> {
            String name = btn.getDisplayName();
            if (name.equals(Messages.getText(Message.BTN_BACK))) {
                openGroupsPage();
            } else if (name.equals(Messages.getText(Message.BTN_RESET))) {
                user.setPrefix(null);
                user.setSuffix(null);
                openCustomPrefixPage();
            } else {
                if (name.equals(Messages.getText(Message.BTN_CHANGE_PREFIX))) {
                    new ChatRespond(user, Messages.getText(Message.CHAT_INPUT_PREFIX).replace("%prefix%", user.getPrefix().replace("§", "&")), (answer) -> {
                        if (answer.equals("cancelled")) {
                            getUser().sendMessage(Messages.getText(Message.INPUT_CANCELLED));
                            return null;
                        } else {
                            user.setPrefix(answer);
                            getUser().sendMessage(Messages.getText(Message.INPUT_SAVED));
                            return "correct";
                        }
                    });
                } else if (name.equals(Messages.getText(Message.BTN_CHANGE_SUFFIX))) {
                    new ChatRespond(user, Messages.getText(Message.CHAT_INPUT_SUFFIX).replace("%suffix%", user.getSuffix().replace("§", "&")), (answer) -> {
                        if (answer.equals("cancelled")) {
                            getUser().sendMessage(Messages.getText(Message.INPUT_CANCELLED));
                            return null;
                        } else {
                            user.setSuffix(answer);
                            getUser().sendMessage(Messages.getText(Message.INPUT_SAVED));
                            return "correct";
                        }
                    });
                }
            }
        });
    }

}