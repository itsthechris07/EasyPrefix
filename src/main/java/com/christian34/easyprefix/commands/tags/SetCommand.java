package com.christian34.easyprefix.commands.tags;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class SetCommand implements Subcommand {
    private final List<String> TAGS_NAMES;
    private final TagsCommand parentCommand;
    private final EasyPrefix instance;

    public SetCommand(TagsCommand parentCommand, EasyPrefix instance) {
        this.parentCommand = parentCommand;
        this.instance = instance;
        this.TAGS_NAMES = new ArrayList<>();
        for (Subgroup subgroup : instance.getGroupHandler().getSubgroups()) {
            this.TAGS_NAMES.add(subgroup.getName());
        }
    }

    @Override
    @Nullable
    public UserPermission getPermission() {
        return UserPermission.ADMIN;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "sets a players tag";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "set <player> <tag>";
    }

    @Override
    @NotNull
    public String getName() {
        return "set";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (args.size() != 3) {
            parentCommand.getSubcommand("help").handleCommand(sender, null);
            return;
        }

        Player player = Bukkit.getPlayer(args.get(1));
        if (player == null) {
            sender.sendMessage(Message.CHAT_PLAYER_NOT_FOUND.getText());
            return;
        }

        User user = instance.getUser(player);

        String tagName = args.get(2);
        Subgroup subgroup = instance.getGroupHandler().getSubgroup(tagName);
        if (subgroup == null) {
            sender.sendMessage(Message.TAGS_INVALID_NAME.getText()
                    .replace("%tag%", tagName));
            return;
        }

        user.setSubgroup(subgroup);
        sender.sendMessage(Message.TAG_SET_TO_PLAYER.getText()
                .replace("%tag%", subgroup.getName())
                .replace("%player%", user.getPlayer().getName()));
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        if (args.size() > 3) {
            return Collections.emptyList();
        }

        if (args.size() < 3) {
            return null;
        }

        List<String> subgroups = new ArrayList<>();
        if (sender instanceof Player) {
            User user = instance.getUser((Player) sender);
            for (Subgroup subgroup : user.getAvailableSubgroups()) {
                subgroups.add(subgroup.getName());
            }
        } else {
            subgroups = this.TAGS_NAMES;
        }
        return subgroups;
    }

}
