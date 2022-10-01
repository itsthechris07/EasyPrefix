package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.CmdUtils;
import com.christian34.easyprefix.commands.CommandHandler;
import com.christian34.easyprefix.commands.EasyCommand;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.commands.easyprefix.set.SetPrefixCommand;
import com.christian34.easyprefix.commands.easyprefix.set.SetSuffixCommand;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class EasyPrefixCommand implements EasyCommand {
    private final EasyPrefix instance;
    private final List<Subcommand> subcommands;

    public EasyPrefixCommand(EasyPrefix instance, CommandHandler commandHandler) {
        this.instance = instance;
        this.subcommands = new ArrayList<>();
        subcommands.add(new UserCommand(this));
        subcommands.add(new HelpCommand(this));
        subcommands.add(new GroupCommand(this));
        subcommands.add(new SetupCommand(this));
        subcommands.add(new SettingsCommand(this));
        subcommands.add(new ReloadCommand(this));
        subcommands.add(new DebugCommand(this));
        subcommands.add(new ColorAliasCommand(commandHandler));
        subcommands.add(new TagsAliasCommand(commandHandler));

        if (instance.getStorageType() == StorageType.SQL) {
            subcommands.add(new DatabaseCommand(this));
        }

        if (instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT)) {
            subcommands.add(new SetPrefixCommand());
            subcommands.add(new SetSuffixCommand());
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
        if (args.isEmpty()) {
            sender.sendMessage(Message.PREFIX + "§7This server uses §9EasyPrefix §7version §b" + this.instance.getDescription().getVersion() + " §7by Christian34.\nType '/easyprefix help' to get a command overview.");
            return;
        }

        String subcommand = args.get(0);

        final String CMD_NOT_FOUND = Message.PREFIX + "§cCouldn't find requested command!\nType '/easyprefix help'"
                + " to get a command overview.";
        if (!instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT) &&
                (subcommand.equalsIgnoreCase("setprefix") || subcommand.equalsIgnoreCase("setsuffix"))) {
            Debug.log("This is not available because you disabled it in the configuration. Open 'config.yml' and set 'custom-layout.enabled' to 'true'. Please restart the server.");
            sender.sendMessage(CMD_NOT_FOUND);
            return;
        }

        for (Subcommand subCmd : subcommands) {
            if (subCmd.getName().equalsIgnoreCase(subcommand) || subCmd.getName().startsWith(subcommand)) {
                if (subCmd.getPermission() == null || sender.hasPermission(subCmd.getPermission().toString())) {
                    try {
                        subCmd.handleCommand(sender, args);
                    } catch (Exception ex) {
                        Debug.handleException(ex);
                    }
                } else {
                    sender.sendMessage(Message.CHAT_NO_PERMS.getText());
                }
                return;
            }
        }
        sender.sendMessage(CMD_NOT_FOUND);
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        String subcommand = args.get(0);
        if (args.size() == 1) {
            return CmdUtils.matches(subcommands.stream().map(Subcommand::getName).collect(Collectors.toList()), subcommand);

        } else {
            for (Subcommand subcmd : subcommands) {
                if (subcmd.getDescription() == null) continue;

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
