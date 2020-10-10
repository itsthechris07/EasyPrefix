package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class CommandHandler implements CommandExecutor, TabCompleter {
    private final EasyPrefix instance;
    private final List<Subcommand> subcommands;

    public CommandHandler(EasyPrefix instance) {
        this.instance = instance;
        PluginCommand mainCommand = instance.getCommand("easyprefix");
        if (mainCommand != null) {
            mainCommand.setExecutor(this);
            mainCommand.setTabCompleter(this);
        }
        this.subcommands = new ArrayList<>();
        subcommands.add(new UserCommand(this));
        subcommands.add(new HelpCommand(this));
        if (this.instance.getStorageType() == StorageType.SQL) {
            subcommands.add(new DatabaseCommand(this));
        }
        subcommands.add(new GroupCommand(this));
        subcommands.add(new SetupCommand(this));
        subcommands.add(new SettingsCommand(this));
        subcommands.add(new ReloadCommand(this));
        subcommands.add(new DebugCommand(this));
        if (ConfigKeys.CUSTOM_LAYOUT.toBoolean()) {
            subcommands.add(new SetCommand(this));
            new AliasHandler(this);
        }
    }

    protected EasyPrefix getInstance() {
        return instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Messages.getPrefix() + "§7This server uses §5EasyPrefix §7version §b" + this.instance.getDescription().getVersion() + " §7by Christian34.\nType '/" + label + " help' to get a command overview.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("easyprefix")) {
            if (ConfigKeys.CUSTOM_LAYOUT.toBoolean()) {
                if (args[0].equalsIgnoreCase("setprefix") || args[0].equalsIgnoreCase("setsuffix")) {
                    getSubcommand("set").handleCommand(sender, Arrays.asList(args));
                    return true;
                }
            }
            for (Subcommand subCmd : subcommands) {
                if (subCmd.getName().equalsIgnoreCase(args[0]) || subCmd.getName().startsWith(args[0])) {
                    if (subCmd.getPermission() == null || sender.hasPermission("easyprefix." + subCmd.getPermission())) {
                        try {
                            subCmd.handleCommand(sender, Arrays.asList(args));
                        } catch (Exception ex) {
                            Debug.captureException(ex);
                        }
                    } else {
                        sender.sendMessage(Messages.getMessage(Message.NO_PERMS));
                    }
                    return true;
                }
            }
            sender.sendMessage(Messages.getPrefix() + "§cCouldn't find requested command!\nType '/" + label + " help'" + " to get a command overview.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("easyprefix")) return null;
        if (args.length == 1) {
            List<String> matches = new ArrayList<>();
            for (Subcommand subcmd : subcommands) {
                if (subcmd.getName().equalsIgnoreCase(args[0]) || subcmd.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    if (subcmd.getPermission() == null || sender.hasPermission("easyprefix." + subcmd.getPermission())) {
                        matches.add(subcmd.getName());
                    }
                }
            }
            matches.remove("set");
            matches.addAll(Arrays.asList("setprefix", "setsuffix"));
            return matches;
        } else {
            for (Subcommand subcmd : subcommands) {
                if (subcmd.getName().equalsIgnoreCase(args[0]) || subcmd.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    if (subcmd.getPermission() == null || sender.hasPermission("easyprefix." + subcmd.getPermission())) {
                        return subcmd.getTabCompletion(sender, Arrays.asList(args));
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    public Subcommand getSubcommand(String name) {
        for (Subcommand subCmd : subcommands) {
            if (subCmd.getName().equalsIgnoreCase(name)) {
                return subCmd;
            }
        }
        throw new NullPointerException("Couldn't find subcommand with name '" + name + "'");
    }

}
