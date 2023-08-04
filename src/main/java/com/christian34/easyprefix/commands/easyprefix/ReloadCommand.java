package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2022.
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
    public UserPermission getPermission() {
        return UserPermission.ADMIN;
    }

    @Override
    @Nullable
    public String getDescription() {
        return "reloads the plugin (not recommended, please stop and start the server)";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "reload";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        this.instance.reload();
        sender.sendMessage(Message.PREFIX + "§aPlugin has been reloaded!");
    }

    @Override
    public @NotNull List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
