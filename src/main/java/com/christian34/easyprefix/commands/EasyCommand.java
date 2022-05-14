package com.christian34.easyprefix.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public interface EasyCommand {

    @NotNull
    String getName();

    void handleCommand(@NotNull CommandSender sender, List<String> args);

    List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args);

}
