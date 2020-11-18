package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.EasyCommand;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.commands.set.SetPrefixCommand;
import com.christian34.easyprefix.commands.set.SetSuffixCommand;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class EasyPrefixCommand implements EasyCommand {
    private final List<Subcommand> subcommands;
    private final EasyPrefix instance;

    public EasyPrefixCommand(EasyPrefix instance) {
        this.instance = instance;

        this.subcommands = new ArrayList<>();
        subcommands.add(new UserCommand(this));
        subcommands.add(new HelpCommand(this));
        subcommands.add(new GroupCommand(this));
        subcommands.add(new SetupCommand(this));
        subcommands.add(new SettingsCommand(this));
        subcommands.add(new ReloadCommand(this));
        subcommands.add(new DebugCommand(this));

        if (instance.getStorageType() == StorageType.SQL) {
            subcommands.add(new DatabaseCommand(this));
        }

        if (ConfigKeys.CUSTOM_LAYOUT.toBoolean()) {
            subcommands.add(new SetPrefixCommand(this));
            subcommands.add(new SetSuffixCommand(this));
            new AliasHandler(this);
        }
    }

    public List<Subcommand> getSubcommands() {
        return subcommands;
    }

    public EasyPrefix getInstance() {
        return instance;
    }

    public Subcommand getSubcommand(String name) {
        for (Subcommand subCmd : subcommands) {
            if (subCmd.getName().equalsIgnoreCase(name)) {
                return subCmd;
            }
        }
        throw new NullPointerException("Couldn't find subcommand with name '" + name + "'");
    }

    @Override
    @NotNull
    public String getName() {
        return "easyprefix";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (args.size() == 0) {
            sender.sendMessage(Message.getPrefix() + "§7This server uses §5EasyPrefix §7version §b" + this.instance.getDescription().getVersion() + " §7by Christian34.\nType '/easyprefix help' to get a command overview.");
            return;
        }

        String subcommand = args.get(0);
        for (Subcommand subCmd : subcommands) {
            if (subCmd.getName().equalsIgnoreCase(subcommand) || subCmd.getName().startsWith(subcommand)) {
                if (subCmd.getPermission() == null || sender.hasPermission(subCmd.getPermission().toString())) {
                    try {
                        subCmd.handleCommand(sender, args);
                    } catch (Exception ex) {
                        Debug.captureException(ex);
                    }
                } else {
                    sender.sendMessage(Message.CHAT_NO_PERMS.getText());
                }
                return;
            }
        }
        sender.sendMessage(Message.getPrefix() + "§cCouldn't find requested command!\nType '/easyprefix help'"
                + " to get a command overview.");
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        String subcommand = args.get(0);
        if (args.size() == 1) {
            List<String> matches = new ArrayList<>();
            for (Subcommand subcmd : subcommands) {
                if (subcmd.getName().equalsIgnoreCase(subcommand) || subcmd.getName().toLowerCase().startsWith(subcommand.toLowerCase())) {
                    if (subcmd.getPermission() == null || sender.hasPermission(subcmd.getPermission().toString())) {
                        matches.add(subcmd.getName());
                    }
                }
            }
            return matches;
        } else {
            for (Subcommand subcmd : subcommands) {
                if (subcmd.getName().equalsIgnoreCase(subcommand) || subcmd.getName().toLowerCase().startsWith(subcommand.toLowerCase())) {
                    if (subcmd.getPermission() == null || sender.hasPermission(subcmd.getPermission().toString())) {
                        return subcmd.getTabCompletion(sender, args);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

}
