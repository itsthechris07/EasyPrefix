package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.InputReader;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
            return true;
        }
        User user = EasyPrefix.getInstance().getUser((Player) sender);
        String input = InputReader.readInput(args, 1);
        if (args[0].equalsIgnoreCase("setprefix")) {
            if (!user.hasPermission("custom.prefix")) {
                sender.sendMessage(Messages.getMessage(Message.NO_PERMS));
                return true;
            }
            Timestamp next = getNextTimestamp(user.getLastPrefixUpdate());
            if (!next.before(new Timestamp(System.currentTimeMillis())) && !user.hasPermission("custom.bypass")) {
                user.getPlayer().sendMessage(getTimeMessage(next));
                return true;
            }
            if (args[1].equalsIgnoreCase("reset")) {
                if (args.length > 2 && args[2].equalsIgnoreCase("submit")) {
                    user.setPrefix(null);
                    user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_PREFIX.toString()
                            .replace("%prefix%", user.getPrefix().replace("§", "&")));
                } else {
                    user.getPlayer().spigot().sendMessage(buildConfirmComponent(Message.RESET_PLAYER_PREFIX.toString()
                            .replace("%prefix%", input), "/ep setprefix reset submit"));
                }
                return true;
            }
            if (!args[args.length - 1].equalsIgnoreCase("submit")) {
                user.getPlayer().spigot().sendMessage(buildConfirmComponent(Message.SUBMIT_PREFIX.toString()
                        .replace("%prefix%", input), "/ep setprefix " + input + " submit"));
            } else {
                user.setPrefix(input);
                user.saveData("custom-prefix-update", new Timestamp(System.currentTimeMillis()).toString());
                user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_PREFIX.toString()
                        .replace("%prefix%", user.getPrefix().replace("§", "&")));
            }
        } else if (args[0].equalsIgnoreCase("setsuffix")) {
            if (!user.hasPermission("custom.suffix")) {
                sender.sendMessage(Messages.getMessage(Message.NO_PERMS));
                return true;
            }
            Timestamp next = getNextTimestamp(user.getLastSuffixUpdate());
            if (!next.before(new Timestamp(System.currentTimeMillis())) && !user.hasPermission("custom.bypass")) {
                user.getPlayer().sendMessage(getTimeMessage(next));
                return true;
            }
            if (args[1].equalsIgnoreCase("reset")) {
                if (args.length > 2 && args[2].equalsIgnoreCase("submit")) {
                    user.setSuffix(null);
                    user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_SUFFIX.toString()
                            .replace("%suffix%", user.getSuffix().replace("§", "&")));
                } else {
                    user.getPlayer().spigot().sendMessage(buildConfirmComponent(Message.RESET_PLAYER_SUFFIX.toString()
                            .replace("%suffix%", input), "/ep setsuffix reset submit"));
                }
                return true;
            }
            if (!args[args.length - 1].equalsIgnoreCase("submit")) {
                user.getPlayer().spigot().sendMessage(buildConfirmComponent(Message.SUBMIT_SUFFIX.toString()
                        .replace("%suffix%", input), "/ep setsuffix " + input + " submit"));
            } else {
                user.setSuffix(input);
                user.saveData("custom-suffix-update", new Timestamp(System.currentTimeMillis()).toString());
                user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_SUFFIX.toString()
                        .replace("%suffix%", user.getSuffix().replace("§", "&")));
            }
        }
        return true;
    }

    @Override
    public String getPermission() {
        return null;
    }

    private TextComponent buildConfirmComponent(String text, String command) {
        TextComponent msg = new TextComponent(TextComponent.fromLegacyText(text.replace("%newline%", "\n")));
        TextComponent confirm = new TextComponent(TextComponent.fromLegacyText(" " + Message.CHAT_BTN_CONFIRM.toString() + " "));
        confirm.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        msg.addExtra(confirm);
        return msg;
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
        return msg.replace("%h%", hours + "").replace("%m%", (minutes == 0) ? "<1" : minutes + "");
    }

}
