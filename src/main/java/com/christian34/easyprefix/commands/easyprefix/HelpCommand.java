package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
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
    public void handleCommand(CommandSender sender, List<String> args) {
        sender.sendMessage(" \n§7---------------=== §5§lEasyPrefix §7===---------------\n ");
        String prefix = "§7/§5EasyPrefix ";

        for (Subcommand cmd : parentCommand.getSubcommands()) {
            if (cmd.getName().equals("set")) continue;

            sender.sendMessage(prefix + cmd.getCommandUsage());
            sender.sendMessage("  §7" + cmd.getDescription());
        }

        sender.sendMessage(" \n§7------------------------------------------------\n"
                + "§7Version: " + this.instance.getPlugin().getDescription().getVersion() + "\n"
                + "§7EasyPrefix by §5§lChristian34");
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
