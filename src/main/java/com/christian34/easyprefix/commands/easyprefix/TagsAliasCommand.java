package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.commands.CommandHandler;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.commands.tags.TagsCommand;
import com.christian34.easyprefix.user.UserPermission;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class TagsAliasCommand implements Subcommand {
    private final CommandHandler commandHandler;

    public TagsAliasCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    @NotNull
    public String getName() {
        return "tags";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        TagsCommand tagsCommand = (TagsCommand) commandHandler.getCommand("tags");
        tagsCommand.handleCommand(sender, Collections.singletonList("help"));
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
        return "tags";
    }

}
