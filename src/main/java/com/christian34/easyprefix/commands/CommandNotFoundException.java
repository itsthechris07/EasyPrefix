package com.christian34.easyprefix.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class CommandNotFoundException extends RuntimeException {
    private final String command;
    private final String subcommand;

    public CommandNotFoundException(@NotNull String commandName, @Nullable String subcommand) {
        super("Command '" + commandName + " " + subcommand + "' was not found!");
        this.command = commandName;
        this.subcommand = subcommand;
    }

    @NotNull
    public String getCommand() {
        return command;
    }

    @Nullable
    public String getSubcommand() {
        return subcommand;
    }

}
