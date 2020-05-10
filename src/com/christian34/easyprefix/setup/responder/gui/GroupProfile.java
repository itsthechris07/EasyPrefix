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
import com.christian34.easyprefix.utils.VersionController;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
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
        CustomInventory inventory = new CustomInventory("§5EasyPrefix §8» §7" + group.getGroupColor() + group.getName(), 4);
        Button prefixBtn = new Button(Material.IRON_INGOT, Messages.getText(Message.BTN_CHANGE_PREFIX)).setSlot(2, 3);
        String prefix = group.getPrefix(null, false);
        if (prefix.length() > 25) {
            List<String> lore = new ArrayList<>();
            lore.add(this.DIVIDER);
            lore.add(Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + prefix.substring(0, 25));
            lore.add("§f" + prefix.substring(26) + "§7»");
            lore.add(" ");
            lore.add(Messages.getText(Message.LORE_EDIT, user));
            prefixBtn.setLore(lore);
        } else {
            prefixBtn.setLore(this.DIVIDER, Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + prefix + "§7»", " ", Messages.getText(Message.LORE_EDIT, user));
        }
        inventory.addItem(prefixBtn);

        Button suffixBtn = new Button(Material.GOLD_INGOT, Messages.getText(Message.BTN_CHANGE_SUFFIX)).setSlot(2, 5);
        String suffix = group.getSuffix(null, false);

        if (suffix.length() > 25) {
            List<String> lore = new ArrayList<>();
            lore.add(this.DIVIDER);
            lore.add(Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + suffix.substring(0, 25));
            lore.add("§f" + suffix.substring(26) + "§7»");
            lore.add(" ");
            lore.add(Messages.getText(Message.LORE_EDIT, user));
            suffixBtn.setLore(lore);
        } else {
            suffixBtn.setLore(this.DIVIDER, Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + suffix + "§7»", " ", Messages.getText(Message.LORE_EDIT, user));
        }

        inventory.addItem(suffixBtn);


        Button joinBtn = new Button(Material.BLAZE_ROD, "§aJoin Message", null).setSlot(3, 4);
        String joinMsg = group.getJoinMessageText();
        if (joinMsg.length() > 25) {
            List<String> lore = new ArrayList<>();
            lore.add(this.DIVIDER);
            lore.add(Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + joinMsg.substring(0, 25));
            lore.add("§f" + joinMsg.substring(26) + "§7»");
            lore.add(" ");
            lore.add(Messages.getText(Message.LORE_EDIT, user));
            joinBtn.setLore(lore);
        } else {
            joinBtn.setLore(this.DIVIDER, Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + joinMsg + "§7»", " ", Messages.getText(Message.LORE_EDIT, user));
        }

        inventory.addItem(joinBtn);

        Button quitBtn = new Button(Material.STICK, "§aQuit Message", null).setSlot(3, 6);
        String quitMsg = group.getQuitMessageText();
        if (quitMsg.length() > 25) {
            List<String> lore = new ArrayList<>();
            lore.add(this.DIVIDER);
            lore.add(Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + quitMsg.substring(0, 25));
            lore.add("§f" + quitMsg.substring(26) + "§7»");
            lore.add(" ");
            lore.add(Messages.getText(Message.LORE_EDIT, user));
            quitBtn.setLore(lore);
        } else {
            quitBtn.setLore(this.DIVIDER, Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + quitMsg + "§7»", " ", Messages.getText(Message.LORE_EDIT, user));
        }
        inventory.addItem(quitBtn);

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
                chatColor = new Button(Material.valueOf("INK_SACK"), Messages.getText(Message.BTN_CHANGE_CHATCOLOR), loreChatColor);
            } catch(Exception e) {
                chatColor = new Button(Material.BARRIER, Messages.getText(Message.BTN_CHANGE_CHATCOLOR), loreChatColor);
            }
        } else {
            chatColor = new Button(Material.LIME_DYE, Messages.getText(Message.BTN_CHANGE_CHATCOLOR), loreChatColor);
        }
        chatColor = chatColor.setSlot(2, 7);
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
                } else if (name.equals("§aJoin Message")) {
                    editGroupPage.editJoinMessage();
                } else if (name.equals("§aQuit Message")) {
                    editGroupPage.editQuitMessage();
                }
            }
        });
    }

    private void openSubgroupProfile(Subgroup subgroup) {
        CustomInventory inventory = new CustomInventory("§5EasyPrefix §8» §7" + subgroup.getGroupColor() + subgroup.getName(), 5);
        Button prefixBtn = new Button(Material.IRON_INGOT, Messages.getText(Message.BTN_CHANGE_PREFIX)).setSlot(3, 3);

        prefixBtn.setLore(this.DIVIDER, Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + subgroup.getPrefix(null, false) + "§7»", " ", Messages.getText(Message.LORE_EDIT, user));
        inventory.addItem(prefixBtn);
        Button suffixBtn = new Button(Material.GOLD_INGOT, Messages.getText(Message.BTN_CHANGE_SUFFIX)).setSlot(3, 5);
        suffixBtn.setLore(this.DIVIDER, Messages.getText(Message.LORE_GROUP_DETAIL, user) + "§7«§f" + subgroup.getSuffix(null, false) + "§7»", " ", Messages.getText(Message.LORE_EDIT, user));
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