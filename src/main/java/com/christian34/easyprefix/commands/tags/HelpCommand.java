package com.christian34.easyprefix.commands.tags;

import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2021.
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
    @Nullable
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
        String prefix = "§7/§9tags ";

        for (Subcommand cmd : parentCommand.getSubcommands()) {
            if (cmd.getDescription() == null) return;

            if (cmd.getPermission() == null || sender.hasPermission(cmd.getPermission().toString())) {
                sender.sendMessage(prefix + cmd.getCommandUsage());
                sender.sendMessage("  §7" + cmd.getDescription());
            }
        }
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
