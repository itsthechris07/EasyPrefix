package com.christian34.easyprefix.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public abstract class EasyCommand {

    public abstract boolean handleCommand(CommandSender commandSender, Command command, String[] args);

    public abstract String getPermission();

}