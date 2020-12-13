package com.christian34.easyprefix.commands.tags;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class ClearCommand implements Subcommand {
    private final TagsCommand parentCommand;
    private final EasyPrefix instance;

    public ClearCommand(TagsCommand parentCommand, EasyPrefix instance) {
        this.parentCommand = parentCommand;
        this.instance = instance;
    }

    @Override
    @Nullable
    public UserPermission getPermission() {
        return UserPermission.ADMIN;
    }

    @Override
    @Nullable
    public String getDescription() {
        return "clears a player's tag";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "clear <player>";
    }

    @Override
    @NotNull
    public String getName() {
        return "clear";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (args.size() != 2) {
            parentCommand.getSubcommand("help").handleCommand(sender, null);
            return;
        }

        Player player = Bukkit.getPlayer(args.get(1));
        if (player == null) {
            sender.sendMessage(Message.CHAT_PLAYER_NOT_FOUND.getText());
            return;
        }

        User user = instance.getUser(player);
        user.setSubgroup(null);

        sender.sendMessage(Message.TAGS_CLEARED_FOR_PLAYER.getText()
                .replace("%player%", user.getPlayer().getName()));
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        if (args.size() >= 3) {
            return Collections.emptyList();
        }
        return null;
    }

}
