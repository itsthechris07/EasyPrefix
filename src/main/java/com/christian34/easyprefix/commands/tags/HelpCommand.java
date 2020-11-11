package com.christian34.easyprefix.commands.tags;

import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.user.UserPermission;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
class HelpCommand implements Subcommand {
    private final TagsCommand parentCommand;

    public HelpCommand(TagsCommand parentCommand) {
        this.parentCommand = parentCommand;
    }

    @Override
    public UserPermission getPermission() {
        return null;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "shows all commands";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "help";
    }

    @Override
    @NotNull
    public String getName() {
        return "help";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        sender.sendMessage(Message.CHAT_TAGS_HEADER.getText().replace("%newline%", "\n"));
        String prefix = "§7/§5tags ";
        sender.sendMessage(prefix + "§f| §7open your tags gui");
        sender.sendMessage(prefix + "list §7§o<player> §f| §7shows your tags");
        sender.sendMessage(prefix + "set <player> <tag> §f| §7set a players tag");

        for (Subcommand cmd : parentCommand.getSubcommands()) {
            sender.sendMessage(prefix + cmd.getCommandUsage());
            sender.sendMessage("  §7" + cmd.getDescription());
        }
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
