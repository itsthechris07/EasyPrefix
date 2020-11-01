package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Messages;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class ReloadCommand implements Subcommand {
    private final EasyPrefix instance;

    public ReloadCommand(CommandHandler commandHandler) {
        this.instance = commandHandler.getInstance();
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "admin";
    }

    @Override
    public void handleCommand(CommandSender sender, List<String> args) {
        this.instance.reload();
        sender.sendMessage(Messages.getPrefix() + "§aPlugin has been reloaded!");
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
