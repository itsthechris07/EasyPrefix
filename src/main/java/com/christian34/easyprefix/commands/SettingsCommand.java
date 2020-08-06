package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.responder.gui.pages.GuiSettings;
import com.christian34.easyprefix.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class SettingsCommand implements Subcommand {
    private final EasyPrefix instance;

    public SettingsCommand(CommandHandler commandHandler) {
        this.instance = commandHandler.getInstance();
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public String getPermission() {
        return "settings";
    }

    @Override
    public void handleCommand(CommandSender sender, List<String> args) {
        User user = sender instanceof Player ? instance.getUser((Player) sender) : null;
        if (user != null) {
            new GuiSettings(user).openWelcomePage();
        } else {
            sender.sendMessage(Messages.getMessage(Message.PLAYER_ONLY));
        }
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }
}
