package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.messages.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
class ReloadCommand implements Subcommand {
    private final EasyPrefix instance;

    public ReloadCommand(EasyPrefixCommand parentCommand) {
        this.instance = parentCommand.getInstance();
    }

    @Override
    @NotNull
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "admin";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        this.instance.reload();
        sender.sendMessage(Messages.getPrefix() + "§aPlugin has been reloaded!");
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
