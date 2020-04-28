package com.christian34.easyprefix.setup.responder.gui;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.Button;
import com.christian34.easyprefix.setup.CustomInventory;
import com.christian34.easyprefix.setup.responder.GuiRespond;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class GroupsList {
    private User user;

    public GroupsList(User user) {
        this.user = user;
        open();
    }

    private void open() {
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        CustomInventory inventory = new CustomInventory("§5EasyPrefix §8» " + Messages.getText(Message.SETUP_GROUPS_TITLE), 5);
        int counter = 9;
        for (Group group : groupHandler.getGroups()) {
            String prefix = group.getRawPrefix();
            String suffix = group.getRawSuffix();
            ChatColor prefixColor = group.getGroupColor();
            List<String> lore = new ArrayList<>();
            StringBuilder line = new StringBuilder("§7-------------------------------");
            for (int x = 1; x <= group.getName().length(); x++) {
                line.append("-");
            }
            lore.add(line.toString());
            if (prefix.length() > 25) {
                lore.add(Messages.getText(Message.LORE_PREFIX).replace("%value%", "§7«§f" + prefix.substring(0, 25)));
                lore.add("§f" + prefix.substring(26) + "§7»");
            } else {
                lore.add(Messages.getText(Message.LORE_PREFIX).replace("%value%", "§7«§f" + prefix + "§7»"));
            }
            lore.add(Messages.getText(Message.LORE_SUFFIX).replace("%value%", "§7«§f" + suffix + "§7»"));

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

            lore.add(Messages.getText(Message.LORE_COLOR).replace("%value%", groupChatColor.replace("§", "&")));
            lore.add(Messages.getText(Message.LORE_PERMISSION).replace("%value%", "EasyPrefix.group." + group.getName()));
            inventory.addItem(new Button(Material.CHEST, prefixColor + group.getName(), lore).setSlot(counter));
            counter++;
        }
        inventory.addItem(new Button(Material.NETHER_STAR, Messages.getText(Message.BTN_ADDGROUP, user), null).setSlot(inventory.getLines() * 9 - 1));
        new GuiRespond(user, inventory, (respond) -> {
            String name = respond.getDisplayName();
            if (name.equals(Messages.getText(Message.BTN_BACK))) {
                new WelcomePage(user);
            } else if (name.equals(Messages.getText(Message.BTN_ADDGROUP, user))) {
                new CreateGroupPage(user);
            } else if (groupHandler.isGroup(name.substring(2))) {
                new GroupProfile(user, groupHandler.getGroup(name.substring(2)));
            }
        });
    }

}