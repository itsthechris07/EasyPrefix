package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Decoration;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TaskManager;
import com.christian34.easyprefix.utils.TextUtils;
import com.christian34.easyprefix.utils.UserInterface;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.*;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2026.
 * <p>
 * The /color command: opens the color menu, sets, resets and previews the chat color and formatting of players.
 *
 * @author Christian34
 */
public class CommandColor {

    private EasyPrefix getInstance() {
        return EasyPrefix.getInstance();
    }

    @Command("color")
    @CommandDescription("opens the gui")
    public void mainCmd(Player player) {
        User user = getInstance().getUser(player);
        UserInterface gui = new UserInterface(user);
        TaskManager.run(player, gui::openPageUserColors);
    }

    @Command("color set <color>")
    @ProxiedBy("setcolor")
    public void setColor(Player player, @Argument("color") Color color) {
        User user = getInstance().getUser(player);
        user.setColor(color);
        user.sendMessage(Message.COLOR_PLAYER_SELECT.get("color", color.getDisplayName()));
    }

    @Command("color <player> set <color>")
    @Permission("easyprefix.admin")
    public void setColor(CommandSender sender, @Argument("player") Player target, @Argument("color") Color color) {
        setColor(sender, getInstance().getUser(target), color);
    }

    /**
     * sets the color of another user and tells the sender, also used by /ep user
     */
    static void setColor(CommandSender sender, User user, Color color) {
        user.setColor(color);
        sender.sendMessage(Message.COLOR_SET_TO_PLAYER.getText()
                .replace("%color%", color.getDisplayName())
                .replace("%player%", user.getName()));
    }

    @Command("color format <formatting>")
    @CommandDescription("sets your chat formatting (e.g. bold)")
    public void setFormatting(Player player, @Argument("formatting") Decoration decoration) {
        User user = getInstance().getUser(player);
        user.setDecoration(decoration);
        user.sendMessage(Message.FORMATTING_SELECTED.get("formatting", displayName(decoration)));
    }

    @Command("color format none")
    @CommandDescription("removes your chat formatting")
    public void removeFormatting(Player player) {
        User user = getInstance().getUser(player);
        user.setDecoration(null);
        user.sendMessage(Message.FORMATTING_REMOVED.getText());
    }

    @Command("color <player> format <formatting>")
    @CommandDescription("sets the chat formatting of a player")
    @Permission("easyprefix.admin")
    public void setFormatting(CommandSender sender, @Argument("player") Player target, @Argument("formatting") Decoration decoration) {
        setFormatting(sender, getInstance().getUser(target), decoration);
    }

    @Command("color <player> format none")
    @CommandDescription("removes the chat formatting of a player")
    @Permission("easyprefix.admin")
    public void removeFormatting(CommandSender sender, @Argument("player") Player target) {
        setFormatting(sender, getInstance().getUser(target), null);
    }

    /**
     * sets or removes (null) the formatting of another user and tells the sender, also used by /ep user
     */
    static void setFormatting(CommandSender sender, User user, @Nullable Decoration decoration) {
        user.setDecoration(decoration);
        if (decoration == null) {
            sender.sendMessage(Message.FORMATTING_REMOVED_PLAYER.get("player", user.getName()));
        } else {
            sender.sendMessage(Message.FORMATTING_SET_TO_PLAYER.get("formatting", displayName(decoration))
                    .replace("%player%", user.getName()));
        }
    }

    private static String displayName(Decoration decoration) {
        return TextUtils.colorize(decoration.getDisplayName());
    }

    @Command("color reset")
    @CommandDescription("resets your color and formatting to the default of your group")
    public void resetColor(Player player) {
        User user = getInstance().getUser(player);
        user.setColor(null);
        user.setDecoration(null);
        user.sendMessage(Message.COLOR_RESET.getText());
    }

    @Command("color <player> reset")
    @CommandDescription("resets the color and formatting of a player to the default of their group")
    @Permission("easyprefix.admin")
    public void resetColor(CommandSender sender, @Argument("player") Player target) {
        resetColor(sender, getInstance().getUser(target));
    }

    /**
     * resets color and formatting of another user and tells the sender, also used by /ep user
     */
    static void resetColor(CommandSender sender, User user) {
        user.setColor(null);
        user.setDecoration(null);
        sender.sendMessage(Message.COLOR_RESET_PLAYER.get("player", user.getName()));
    }

    @Command("color show")
    @CommandDescription("shows how your chat messages look")
    public void showColor(Player player) {
        ChatListener.sendPreview(player, getInstance().getUser(player));
    }

    @Command("color <player> show")
    @CommandDescription("shows how the chat messages of a player look")
    @Permission("easyprefix.admin")
    public void showColor(CommandSender sender, @Argument("player") Player target) {
        ChatListener.sendPreview(sender, getInstance().getUser(target));
    }

}
