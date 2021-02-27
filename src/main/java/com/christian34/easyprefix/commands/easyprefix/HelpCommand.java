package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.user.UserPermission;
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
    private final EasyPrefix instance;
    private final EasyPrefixCommand parentCommand;

    public HelpCommand(EasyPrefixCommand parentCommand) {
        this.instance = parentCommand.getInstance();
        this.parentCommand = parentCommand;
    }

    @Override
    @NotNull
    public String getName() {
        return "help";
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
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        sender.sendMessage(" \n§7---------------=== §9§lEasyPrefix §7===---------------\n ");
        String prefix = "§7/§9EasyPrefix ";

        for (Subcommand cmd : parentCommand.getSubcommands()) {
            if (cmd.getDescription() == null) continue;

            if (cmd.getPermission() == null || sender.hasPermission(cmd.getPermission().toString())) {
                sender.sendMessage(prefix + cmd.getCommandUsage());
                sender.sendMessage("  §7" + cmd.getDescription());
            }
        }

        sender.sendMessage(" \n§7------------------------------------------------\n"
                + "§7Version: §9§l" + this.instance.getPlugin().getDescription().getVersion() + "\n"
                + "§7EasyPrefix by §9§lChristian34");
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
