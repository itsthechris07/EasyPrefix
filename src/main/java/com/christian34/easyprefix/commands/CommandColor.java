package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TaskManager;
import com.christian34.easyprefix.utils.UserInterface;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.*;

/**
 * EasyPrefix 2026.
 * <p>
 * The /color command: opens the color menu, sets, resets and previews the chat color of players.
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
        User user = getInstance().getUser(target);
        user.setColor(color);
        sender.sendMessage(Message.COLOR_SET_TO_PLAYER.getText()
                .replace("%color%", color.getDisplayName())
                .replace("%player%", target.getName()));
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
        User user = getInstance().getUser(target);
        user.setColor(null);
        user.setDecoration(null);
        sender.sendMessage(Message.COLOR_RESET_PLAYER.get("player", target.getName()));
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
