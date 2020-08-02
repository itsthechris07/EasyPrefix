package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class HelpCommand implements Subcommand {
    private final EasyPrefix instance;

    public HelpCommand(CommandHandler commandHandler) {
        this.instance = commandHandler.getInstance();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public void handleCommand(CommandSender sender, List<String> args) {
        sender.sendMessage(" \n§7---------------=== §5§lEasyPrefix §7===---------------\n ");
        sender.sendMessage("§7/§5EasyPrefix §f| §7main command");
        if (sender.hasPermission("EasyPrefix.settings")) {
            sender.sendMessage("§7/§5EasyPrefix settings §f| §7manage your prefixes");
        }
        if (sender.hasPermission("EasyPrefix.admin")) {
            sender.sendMessage("§7/§5EasyPrefix setup §f| §7opens setup gui");
            sender.sendMessage("§7/§5EasyPrefix reload §f| §7reloads the plugin");
            sender.sendMessage("§7/§5EasyPrefix user <Player> §f| §7player info");
            sender.sendMessage("§7/§5EasyPrefix group <Group> §f| §7group info");
            if (this.instance.getSqlDatabase() != null) {
                sender.sendMessage("§7/§5EasyPrefix database §f| §7sql configuration");
            }
        }
        if (ConfigKeys.CUSTOM_LAYOUT.toBoolean()) {
            if (sender.hasPermission("easyprefix.custom.prefix")) {
                sender.sendMessage("§7/§5EasyPrefix setprefix <Prefix> §f| §7set prefix");
            }
            if (sender.hasPermission("easyprefix.custom.suffix")) {
                sender.sendMessage("§7/§5EasyPrefix setsuffix <Suffix> §f| §7set suffix");
            }
        }
        sender.sendMessage(" \n§7------------------------------------------------\n ");
        sender.sendMessage("§7Version: " + this.instance.getPlugin().getDescription().getVersion());
        sender.sendMessage("§7EasyPrefix by §5§lChristian34");
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }
}
