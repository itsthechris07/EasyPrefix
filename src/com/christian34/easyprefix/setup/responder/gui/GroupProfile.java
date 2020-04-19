package com.christian34.easyprefix.setup.responder.gui;

import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.Button;
import com.christian34.easyprefix.setup.CustomInventory;
import com.christian34.easyprefix.setup.responder.GuiRespond;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Reflection;
import com.christian34.easyprefix.utils.VersionController;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GroupProfile {
    private final String DIVIDER = "§7-------------------------";
    private User user;

    public GroupProfile(User user, EasyGroup easyGroup) {
        this.user = user;
        if (easyGroup instanceof Group) {
            openGroupProfile((Group) easyGroup);
        } else if (easyGroup instanceof Subgroup) {
            openSubgroupProfile((Subgroup) easyGroup);
        }
    }

    public void openGroupProfile(Group group) {
        CustomInventory inventory = new CustomInventory("§5EasyPrefix §8» §7" + group.getGroupColor() + group.getName(), 5);
        Button prefixBtn = new Button(Material.IRON_INGOT, Messages.getText(Message.BTN_CHANGE_PREFIX)).setSlot(3, 3);
        String prefix = group.getRawPrefix();
        if (prefix.length() > 25) {
            List<String> lore = new ArrayList<>();
            lore.add(this.DIVIDER);
            lore.add(Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + prefix.substring(0, 25));
            lore.add("§f" + prefix.substring(26) + "§7»");
            lore.add(" ");
            lore.add(Messages.getText(Message.LORE_EDIT, user));
            prefixBtn.setLore(lore);
        } else {
            prefixBtn.setLore(this.DIVIDER, Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + group.getRawPrefix() + "§7»", " ", Messages.getText(Message.LORE_EDIT, user));
        }
        inventory.addItem(prefixBtn);

        Button suffixBtn = new Button(Material.GOLD_INGOT, Messages.getText(Message.BTN_CHANGE_SUFFIX)).setSlot(3, 5);
        String suffix = group.getRawSuffix();

        if (suffix.length() > 25) {
            List<String> lore = new ArrayList<>();
            lore.add(this.DIVIDER);
            lore.add(Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + suffix.substring(0, 25));
            lore.add("§f" + suffix.substring(26) + "§7»");
            lore.add(" ");
            lore.add(Messages.getText(Message.LORE_EDIT, user));
            suffixBtn.setLore(lore);
        } else {
            suffixBtn.setLore(this.DIVIDER, Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + group.getRawSuffix() + "§7»", " ", Messages.getText(Message.LORE_EDIT, user));
        }

        inventory.addItem(suffixBtn);

        String groupChatColor = "-";

        if (group.getChatColor() != null) {
            groupChatColor = group.getChatColor().getCode();
            if (group.getChatFormatting() != null && !group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                groupChatColor += group.getChatFormatting().getCode();
            }
        } else {
            if (group.getChatFormatting() != null && group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                groupChatColor = Messages.getText(Message.FORMATTING_RAINBOW);
            }
        }

        if (!group.getName().equals("default")) {
            inventory.addItem(new Button(Material.BARRIER, Messages.getText(Message.BTN_DELETE)).setSlot(inventory.getLines() * 9 - 1));
        }
        List<String> loreChatColor = Arrays.asList(this.DIVIDER, Messages.getText(Message.LORE_GROUP_DETAIL, user) + groupChatColor.replace("§", "&"), " ", Messages.getText(Message.LORE_EDIT, user));
        Button chatColor = null;
        if (VersionController.getMinorVersion() < 13) {
            try {
                Class material = Reflection.getClass("org.bukkit", "Material");
                Field field = Objects.requireNonNull(material).getDeclaredField("INK_SACK");
                chatColor = new Button(Material.valueOf(field.getName()), Messages.getText(Message.BTN_CHANGE_CHATCOLOR), loreChatColor);
            } catch(NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else {
            chatColor = new Button(Material.LIME_DYE, Messages.getText(Message.BTN_CHANGE_CHATCOLOR), loreChatColor);
        }
        chatColor = chatColor.setSlot(3, 7);
        inventory.addItem(chatColor);
        new GuiRespond(user, inventory, (respond) -> {
            String name = respond.getDisplayName();
            if (name.equals(Messages.getText(Message.BTN_BACK))) {
                new GroupsList(user);
            } else {
                EditGroup editGroupPage = new EditGroup(user, group);
                if (name.equals(Messages.getText(Message.BTN_CHANGE_PREFIX))) {
                    editGroupPage.editPrefix();
                } else if (name.equals(Messages.getText(Message.BTN_CHANGE_SUFFIX))) {
                    editGroupPage.editSuffix();
                } else if (name.equals(Messages.getText(Message.BTN_CHANGE_CHATCOLOR))) {
                    editGroupPage.editChatColor();
                } else if (name.equals(Messages.getText(Message.BTN_DELETE))) {
                    editGroupPage.deleteConfirmation();
                }
            }
        });
    }

    private void openSubgroupProfile(Subgroup subgroup) {
        CustomInventory inventory = new CustomInventory("§5EasyPrefix §8» §7" + subgroup.getGroupColor() + subgroup.getName(), 5);
        Button prefixBtn = new Button(Material.IRON_INGOT, Messages.getText(Message.BTN_CHANGE_PREFIX)).setSlot(3, 3);

        prefixBtn.setLore(this.DIVIDER, Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + subgroup.getRawPrefix() + "§7»", " ", Messages.getText(Message.LORE_EDIT, user));
        inventory.addItem(prefixBtn);
        Button suffixBtn = new Button(Material.GOLD_INGOT, Messages.getText(Message.BTN_CHANGE_SUFFIX)).setSlot(3, 5);
        suffixBtn.setLore(this.DIVIDER, Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + subgroup.getRawSuffix() + "§7»", " ", Messages.getText(Message.LORE_EDIT, user));
        inventory.addItem(suffixBtn);

        if (!subgroup.getName().equals("default")) {
            inventory.addItem(new Button(Material.BARRIER, Messages.getText(Message.BTN_DELETE)).setSlot(inventory.getLines() * 9 - 1));
        }
        new GuiRespond(user, inventory, (respond) -> {
            String name = respond.getDisplayName();
            if (name.equals(Messages.getText(Message.BTN_BACK))) {
                new SubgroupsList(user);
            } else {
                EditGroup editGroupPage = new EditGroup(user, subgroup);
                if (name.equals(Messages.getText(Message.BTN_CHANGE_PREFIX))) {
                    editGroupPage.editPrefix();
                } else if (name.equals(Messages.getText(Message.BTN_CHANGE_SUFFIX))) {
                    editGroupPage.editSuffix();
                } else if (name.equals(Messages.getText(Message.BTN_CHANGE_CHATCOLOR))) {
                    editGroupPage.editChatColor();
                } else if (name.equals(Messages.getText(Message.BTN_DELETE))) {
                    editGroupPage.deleteConfirmation();
                }
            }
        });
    }

}