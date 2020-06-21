package com.christian34.easyprefix.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public interface EasyCommand {

    boolean handleCommand(CommandSender sender, List<String> args);

    String getPermission();

}
