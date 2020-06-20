package com.christian34.easyprefix.commands;

import org.bukkit.command.CommandSender;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public interface EasyCommand {

    boolean handleCommand(CommandSender sender, String[] args);

    String getPermission();

}
