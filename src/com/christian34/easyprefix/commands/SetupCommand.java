package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.responder.gui.pages.GuiSetup;
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
public class SetupCommand implements Subcommand {
    private final EasyPrefix instance;

    public SetupCommand(CommandHandler commandHandler) {
        this.instance = commandHandler.getInstance();
    }

    @Override
    public String getName() {
        return "setup";
    }

    @Override
    public String getPermission() {
        return "admin";
    }

    @Override
    public void handleCommand(CommandSender sender, List<String> args) {
        User user = sender instanceof Player ? instance.getUser((Player) sender) : null;
        if (user != null) {
            new GuiSetup(user).mainPage();
        } else {
            sender.sendMessage(Messages.getMessage(Message.PLAYER_ONLY));
        }
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
