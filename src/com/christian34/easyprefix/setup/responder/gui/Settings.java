package com.christian34.easyprefix.setup.responder.gui;

import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.Button;
import com.christian34.easyprefix.setup.CustomInventory;
import com.christian34.easyprefix.setup.responder.GuiRespond;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.VersionController;
import org.bukkit.Material;

import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Settings {
    private User user;

    public Settings(User user) {
        this.user = user;
        open();
    }

    private void open() {
        CustomInventory inventory = new CustomInventory("§5EasyPrefix §8» " + Messages.getText(Message.SETTINGS_TITLE_MAIN), 3);
        String langName = Messages.getText(Message.BTN_CHANGE_LANG).replace("%lang%", Messages.langToName());
        Button lang;
        if (VersionController.getMinorVersion() < 12) {
            lang = new Button(Material.valueOf("SIGN"), langName).setSlot(2, 3);
        } else {
            lang = new Button(Material.OAK_SIGN, langName).setSlot(2, 3);
        }
        List<String> lore = Messages.getList(Message.LORE_CHANGE_LANG);
        lang.setLore(lore);
        inventory.addItem(lang);

        ConfigData configData = FileManager.getConfig();

        boolean useCp = configData.getBoolean(ConfigData.Values.CUSTOM_PREFIX);
        String cpText = Messages.getText(Message.BTN_SWITCH_CP).replace("%active%", (useCp) ? Messages.getText(Message.ENABLED) : Messages.getText(Message.DISABLED));
        Button customPrefix = new Button(Material.BEACON, cpText).setSlot(2, 5);
        customPrefix.setLore(Messages.getText(Message.LORE_SWITCH_CP));
        inventory.addItem(customPrefix);

        boolean useGender = configData.getBoolean(ConfigData.Values.USE_GENDER);
        String genderText = Messages.getText(Message.BTN_SWITCH_GENDER).replace("%active%", (useGender) ? Messages.getText(Message.ENABLED) : Messages.getText(Message.DISABLED));
        Button gender = new Button(Material.CHAINMAIL_HELMET, genderText).setSlot(2, 7);
        gender.setLore(Messages.getText(Message.LORE_SWITCH_GENDER));
        inventory.addItem(gender);

        new GuiRespond(this.user, inventory, (respond) -> {
            String name = respond.getDisplayName();
            if (name.equals(Messages.getText(Message.BTN_BACK))) {
                new WelcomePage(user);
            } else if (respond.equals(lang)) {
                String crntLang = Messages.getLanguage();
                String nextLang = "en_EN";
                switch (crntLang) {
                    case "en_EN":
                        nextLang = "de_DE";
                        break;
                    case "de_DE":
                        nextLang = "it_IT";
                        break;
                }
                Messages.setLanguage(nextLang);
                open();
            } else if (respond.equals(customPrefix)) {
                configData.set(ConfigData.Values.CUSTOM_PREFIX.toString(), !useCp);
                open();
            } else if (respond.equals(gender)) {
                configData.set(ConfigData.Values.USE_GENDER.toString(), !useGender);
                open();
            }
        });
    }

}