package com.christian34.easyprefix.commands.tags;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
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
public class SelectCommand implements Subcommand {
    private final List<String> TAGS_NAMES;
    private final TagsCommand parentCommand;
    private final EasyPrefix instance;

    public SelectCommand(TagsCommand parentCommand, EasyPrefix instance) {
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
        return UserPermission.TAGS_SWITCH;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "changes your tag";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "select <tag>";
    }

    @Override
    @NotNull
    public String getName() {
        return "select";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.getPrefix() + Message.CHAT_PLAYER_ONLY);
            return;
        }

        if (args.size() != 2) {
            parentCommand.getSubcommand("help").handleCommand(sender, null);
            return;
        }

        User user = instance.getUser((Player) sender);

        String tagName = args.get(1);
        Subgroup subgroup = instance.getGroupHandler().getSubgroup(tagName);
        if (subgroup == null) {
            user.getPlayer().sendMessage(Message.TAGS_INVALID_NAME.getText()
                    .replace("%prefix%", Message.getPrefix())
                    .replace("%tag%", tagName));
            return;
        }

        if (!user.hasPermission("tag." + tagName.toLowerCase())) {
            user.getPlayer().sendMessage(Message.CHAT_NO_PERMS.getText());
            return;
        }
        user.setSubgroup(subgroup);
        user.getPlayer().sendMessage(Message.TAGS_PLAYER_SELECT.getText()
                .replace("%prefix%", Message.getPrefix())
                .replace("%tag%", subgroup.getName()));
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        if (args.size() > 2) {
            return Collections.emptyList();
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
