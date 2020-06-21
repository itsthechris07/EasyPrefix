package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Command_Alias implements CommandExecutor, TabCompleter {
    private final String prefixAlias;
    private final String suffixAlias;

    public Command_Alias(EasyPrefix instance) {
        ConfigData config = instance.getFileManager().getConfig();
        this.prefixAlias = config.getString(ConfigData.ConfigKeys.PREFIX_ALIAS).replace("/", "");
        this.suffixAlias = config.getString(ConfigData.ConfigKeys.SUFFIX_ALIAS).replace("/", "");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            Bukkit.dispatchCommand(sender, "easyprefix");
            return true;
        }
        Command_Custom commandCustom = new Command_Custom();
        List<String> value = new ArrayList<>(Arrays.asList(args));
        if (cmd.getName().equalsIgnoreCase(this.prefixAlias)) {
            value.add(0, "setprefix");
        } else if (cmd.getName().equalsIgnoreCase(this.suffixAlias)) {
            value.add(0, "setsuffix");
        }
        commandCustom.handleCommand(sender, value);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reset");
        }
        return Collections.emptyList();
    }

}
