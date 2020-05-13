package com.christian34.easyprefix.setup.responder.gui;

import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.Button;
import com.christian34.easyprefix.setup.CustomInventory;
import com.christian34.easyprefix.setup.responder.GuiRespond;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.VersionController;
import org.bukkit.Material;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class WelcomePage {
    private final User user;

    public WelcomePage(User user) {
        this.user = user;
        open();
    }

    private void open() {
        CustomInventory inventory = new CustomInventory("§5EasyPrefix §8» " + Messages.getText(Message.SETTINGS_TITLE_MAIN), 3);
        Button groups = new Button(Material.CHEST, Messages.getText(Message.BTN_GROUPS)).setSlot(2, 3);
        inventory.addItem(groups);
        Button settings = new Button(Material.NETHER_STAR, Messages.getText(Message.SETTINGS_TITLE_MAIN)).setSlot(2, 5);
        inventory.addItem(settings);
        Material icon = (VersionController.getMinorVersion() < 12) ? Material.valueOf("CHEST") : Material.valueOf("WRITABLE_BOOK");
        Button subgroups = new Button(icon, Messages.getText(Message.BTN_SUBGROUPS)).setSlot(2, 7);
        inventory.addItem(subgroups);
        new GuiRespond(this.user, inventory, (respond) -> {
            String name = respond.getDisplayName();
            if (name.equals(Messages.getText(Message.BTN_BACK))) {
                user.getPlayer().closeInventory();
            } else if (respond.equals(settings)) {
                new Settings(user);
            } else if (respond.equals(groups)) {
                new GroupsList(user);
            } else if (respond.equals(subgroups)) {
                new SubgroupsList(user);
            }
        });
    }

}
