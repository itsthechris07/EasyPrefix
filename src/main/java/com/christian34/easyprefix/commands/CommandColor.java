package com.christian34.easyprefix.commands;

import cloud.commandframework.annotations.*;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TaskManager;
import com.christian34.easyprefix.utils.UserInterface;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class CommandColor {

    private EasyPrefix getInstance() {
        return EasyPrefix.getInstance();
    }

    @CommandMethod("color")
    @CommandDescription("opens the gui")
    public void mainCmd(Player player) {
        User user = getInstance().getUser(player);
        UserInterface gui = new UserInterface(user);
        TaskManager.run(gui::openPageUserColors);
    }

    @CommandMethod("color set <color>")
    @ProxiedBy("setcolor")
    public void setColor(Player player, @Argument("color") Color color) {
        User user = getInstance().getUser(player);
        user.setColor(color);
        user.sendMessage(Message.COLOR_PLAYER_SELECT.get("color", color.getDisplayName()));
    }

    @CommandMethod("color <player> set <color>")
    @CommandPermission("easyprefix.admin")
    public void setColor(CommandSender sender, @Argument("player") Player target, @Argument("color") Color color) {
        User user = getInstance().getUser(target);
        user.setColor(color);
        sender.sendMessage(Message.COLOR_SET_TO_PLAYER.getText()
                .replace("%color%", color.getDisplayName())
                .replace("%player%", target.getName()));
    }

    @CommandMethod("color reset")
    public void resetColor(Player player) {
        User user = getInstance().getUser(player);
        user.setColor(null);
        user.setDecoration(null);
    }

}
