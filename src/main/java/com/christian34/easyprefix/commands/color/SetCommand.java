package com.christian34.easyprefix.commands.color;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Message;
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
    private final ColorCommand parentCommand;
    private final EasyPrefix instance;
    private final List<String> colorNames;

    public SetCommand(ColorCommand parentCommand, EasyPrefix instance) {
        this.parentCommand = parentCommand;
        this.instance = instance;
        this.colorNames = new ArrayList<>();
        colorNames.add("default");
        for (Color color : Color.getValues()) {
            colorNames.add(color.getName());
        }
        for (ChatFormatting formatting : ChatFormatting.getValues()) {
            colorNames.add(formatting.getName());
        }
    }

    @Override
    @Nullable
    public UserPermission getPermission() {
        return UserPermission.ADMIN;
    }

    @Override
    @Nullable
    public String getDescription() {
        return "sets a players color";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "set <player> <color>";
    }

    @Override
    @NotNull
    public String getName() {
        return "set";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (args.size() < 3) {
            parentCommand.getSubcommand("help").handleCommand(sender, null);
            return;
        }

        Player player = Bukkit.getPlayer(args.get(1));
        if (player == null) {
            sender.sendMessage(Message.CHAT_PLAYER_NOT_FOUND.getText());
            return;
        }

        User user = instance.getUser(player);

        String name = args.get(2);
        if (args.size() == 4) {
            name += " " + args.get(3);
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
        sender.sendMessage(Message.COLOR_SET_TO_PLAYER.getText()
                .replace("%color%", name)
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
        String input = args.get(2);
        if (!input.isEmpty()) {
            List<String> list = new ArrayList<>();
            for (String name : colorNames) {
                if (name.toLowerCase().startsWith(input.toLowerCase())) {
                    list.add(name);
                }
            }
            return list;
        }
        return this.colorNames;
    }

}
