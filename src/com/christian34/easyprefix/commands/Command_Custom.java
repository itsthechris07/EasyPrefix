package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.InputReader;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Command_Custom implements EasyCommand {

    @Override
    public boolean handleCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.getMessage(Message.PLAYER_ONLY));
            return false;
        }
        User user = EasyPrefix.getInstance().getUser((Player) sender);

        if (args[0].equalsIgnoreCase("setprefix")) {
            if (!user.hasPermission("custom.prefix")) {
                sender.sendMessage(Messages.getMessage(Message.NO_PERMS));
                return false;
            }
            Timestamp next = getNextTimestamp(user.getLastPrefixUpdate());

            if (next.before(new Timestamp(System.currentTimeMillis())) || user.hasPermission("custom.bypass")) {
                user.setPrefix(InputReader.readInput(args, 1));
                user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_PREFIX.toString().replace("%prefix%", user.getPrefix()));
            } else {
                user.getPlayer().sendMessage(getTimeMessage(next));
            }
        } else if (args[0].equalsIgnoreCase("setsuffix")) {
            if (!user.hasPermission("custom.suffix")) {
                sender.sendMessage(Messages.getMessage(Message.NO_PERMS));
                return false;
            }
            Timestamp next = getNextTimestamp(user.getLastPrefixUpdate());

            if (next.before(new Timestamp(System.currentTimeMillis())) || user.hasPermission("custom.bypass")) {
                user.setSuffix(InputReader.readInput(args, 1));
                user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_SUFFIX.toString().replace("%suffix%", user.getSuffix()));
            } else {
                user.getPlayer().sendMessage(getTimeMessage(next));
            }
        }
        return true;
    }

    @Override
    public String getPermission() {
        return null;
    }

    private Timestamp getNextTimestamp(long last) {
        double delay = EasyPrefix.getInstance().getFileManager().getConfig().getData().getDouble("config.user.custom-layout.cooldown");
        long newTime = (long) (last + (delay * 60 * 60 * 1000));
        return new Timestamp(newTime);
    }

    private String getTimeMessage(Timestamp timestamp) {
        long min = (timestamp.getTime() - System.currentTimeMillis()) / 1000 / 60;
        int minutes = (int) (min % 60);
        int hours = (int) ((min / 60) % 24);
        String msg = Message.LAYOUT_ERROR.toString();
        return msg.replace("%h%", hours + "").replace("%m%", minutes + "");
    }

}
