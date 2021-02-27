package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.commands.CommandHandler;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.commands.color.ColorCommand;
import com.christian34.easyprefix.user.UserPermission;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2021.
 *
 * @author Christian34
 */
public class ColorAliasCommand implements Subcommand {
    private final CommandHandler commandHandler;

    public ColorAliasCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    @NotNull
    public String getName() {
        return "color";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        ColorCommand colorCommand = (ColorCommand) commandHandler.getCommand("color");
        colorCommand.handleCommand(sender, Collections.singletonList("help"));
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

    @Override
    @Nullable
    public UserPermission getPermission() {
        return null;
    }

    @Override
    @Nullable
    public String getDescription() {
        return null;
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "color";
    }

}
