package com.christian34.easyprefix.commands;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class CommandNotFoundException extends RuntimeException {

    public CommandNotFoundException(String commandName) {
        super("Command '" + commandName
                .replace("[", "")
                .replace("]", "")
                .replace(",", "") + "' was not found!");
    }

}
