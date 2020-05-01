package com.christian34.easyprefix.setup.responder.gui;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.Button;
import com.christian34.easyprefix.setup.CustomInventory;
import com.christian34.easyprefix.setup.responder.GuiRespond;
import com.christian34.easyprefix.user.Gender;
import com.christian34.easyprefix.user.User;
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
public class SubgroupsList {
    private User user;

    public SubgroupsList(User user) {
        this.user = user;
        open();
    }

    private void open() {
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        CustomInventory inventory = new CustomInventory("§5EasyPrefix §8» " + Messages.getText(Message.TITLE_SUBGROUPS), 5);
        int counter = 9;
        for (final Subgroup subgroup : groupHandler.getSubgroups()) {
            String prefix = subgroup.getRawPrefix();
            String suffix = subgroup.getRawSuffix();
            suffix = (suffix == null) ? "-" : suffix;
            ChatColor prefixColor = subgroup.getGroupColor();
            List<String> lore = new ArrayList<>();
            lore.add("§7-------------------------");
            if (prefix.length() > 25) {
                lore.add(Messages.getText(Message.LORE_PREFIX).replace("%value%", "§7«§f" + prefix.substring(0, 25)));
                lore.add("§f" + prefix.substring(26) + "§7»");
            } else {
                lore.add(Messages.getText(Message.LORE_PREFIX).replace("%value%", "§7«§f" + prefix + "§7»"));
            }
            lore.add(Messages.getText(Message.LORE_SUFFIX).replace("%value%", "§7«§f" + suffix + "§7»"));
            if (!Gender.getTypes().contains(subgroup.getName().toLowerCase())) {
                lore.add(Messages.getText(Message.LORE_PERMISSION).replace("%value%", "EasyPrefix.subgroup." + subgroup.getName()));
            }
            Button button;
            if (VersionController.getMinorVersion() < 12) {
                button = new Button(Material.CHEST, prefixColor + subgroup.getName(), lore);
            } else {
                button = new Button(Material.WRITABLE_BOOK, prefixColor + subgroup.getName(), lore);
            }
            inventory.addItem(button.setSlot(counter));
            counter++;
        }

        new GuiRespond(this.user, inventory, (respond) -> {
            String name = respond.getDisplayName();
            if (name.equals(Messages.getText(Message.BTN_BACK))) {
                new WelcomePage(user);
            } else if (groupHandler.isSubgroup(name.substring(2))) {
                new GroupProfile(user, groupHandler.getSubgroup(name.substring(2)));
            }
        });
    }

}