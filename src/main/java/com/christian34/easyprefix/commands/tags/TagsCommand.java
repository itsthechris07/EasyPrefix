package com.christian34.easyprefix.commands.tags;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.EasyCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class TagsCommand implements EasyCommand {
    private final EasyPrefix instance;

    public TagsCommand(EasyPrefix instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull String getName() {
        return "tags";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {

    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return null;
    }

}
