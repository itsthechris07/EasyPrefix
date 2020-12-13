package com.christian34.easyprefix.commands.color;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Message;
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
    private final ColorCommand parentCommand;
    private final EasyPrefix instance;

    public SelectCommand(ColorCommand parentCommand, EasyPrefix instance) {
        this.parentCommand = parentCommand;
        this.instance = instance;
    }

    @Override
    @Nullable
    public UserPermission getPermission() {
        return null;
    }

    @Override
    @Nullable
    public String getDescription() {
        return "sets a players color";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "select <color>";
    }

    @Override
    @NotNull
    public String getName() {
        return "select";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            parentCommand.getSubcommand("help").handleCommand(sender, null);
            return;
        }

        User user = instance.getUser((Player) sender);

        String name = args.get(1);
        if (args.size() == 3) {
            name += " " + args.get(2);
        }

        Color color = null;
        ChatFormatting chatFormatting = null;

        if (name.equalsIgnoreCase("default") || name.equalsIgnoreCase("reset")) {
            user.setChatColor(null);
            if (user.getChatFormatting() != null) {
                user.setChatFormatting(ChatFormatting.UNDEFINED);
            }
        } else {
            for (Color chatcolor : Color.getValues()) {
                if (chatcolor.getName().equalsIgnoreCase(name)) {
                    color = chatcolor;
                }
            }
            for (ChatFormatting formatting : ChatFormatting.getValues()) {
                if (formatting.getName().equalsIgnoreCase(name)) {
                    chatFormatting = formatting;
                }
            }

            if (chatFormatting == null && color == null) {
                sender.sendMessage(Message.COLOR_NOT_FOUND.getText()
                        .replace("%color%", name));
                return;
            }

            if (color != null) {
                user.setChatColor(color);
                name = color.toString();
            } else {
                user.setChatFormatting(chatFormatting);
                name = chatFormatting.toString();
            }
        }
        sender.sendMessage(Message.COLOR_PLAYER_SELECT.getText().replace("%color%", name));
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        if (args.size() > 2) {
            return Collections.emptyList();
        }
        if (args.size() < 2) {
            return null;
        }

        List<String> names = new ArrayList<>();
        User user = instance.getUser((Player) sender);
        for (Color color : user.getColors()) {
            names.add(color.getName());
        }

        for (ChatFormatting formatting : user.getChatFormattings()) {
            names.add(formatting.getName());
        }
        if (!names.isEmpty()) {
            names.add("default");
        }

        String input = args.get(1);
        if (!input.isEmpty()) {
            List<String> list = new ArrayList<>();
            for (String name : names) {
                if (name.toLowerCase().startsWith(input.toLowerCase())) {
                    list.add(name);
                }
            }
            return list;
        }
        return names;
    }

}
