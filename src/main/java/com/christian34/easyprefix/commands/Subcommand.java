package com.christian34.easyprefix.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public interface Subcommand {

    String getName();

    String getPermission();

    void handleCommand(CommandSender sender, List<String> args);

    List<String> getTabCompletion(CommandSender sender, List<String> args);

}
