package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
@Deprecated
class SetCommand implements Subcommand {
    private final EasyPrefixCommand parentCommand;
    private final EasyPrefix instance;

    public SetCommand(EasyPrefixCommand parentCommand) {
        this.parentCommand = parentCommand;
        this.instance = parentCommand.getInstance();
    }

    @Override
    @NotNull
    public String getName() {
        return "set";
    }

    @Override
    public UserPermission getPermission() {
        return null;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "changes your prefix or suffix";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "setprefix/setsuffix <value>";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        User user = sender instanceof Player ? instance.getUser((Player) sender) : null;
        if (user == null) {
            sender.sendMessage(Message.PLAYER_ONLY.getMessage());
            return;
        }

        if (args.size() < 2) {
            parentCommand.getSubcommand("help").handleCommand(sender, null);
            return;
        }

        String input = readInput(args);

        if (args.get(0).equalsIgnoreCase("setprefix")) {
            if (!user.hasPermission("custom.prefix")) {
                sender.sendMessage(Message.NO_PERMS.getMessage());
                return;
            }

            Timestamp next = getNextTimestamp(user.getLastPrefixUpdate());
            if (!next.before(new Timestamp(System.currentTimeMillis())) && !user.hasPermission("custom.bypass")) {
                user.getPlayer().sendMessage(getTimeMessage(next));
                return;
            }
            if (args.get(1).equalsIgnoreCase("reset")) {
                if (args.size() > 2 && args.get(2).equalsIgnoreCase("submit")) {
                    user.setPrefix(null);
                    user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_PREFIX.getText()
                            .replace("%prefix%", user.getPrefix().replace("§", "&")));
                } else {
                    user.getPlayer().spigot().sendMessage(buildConfirmComponent(Message.RESET_PLAYER_PREFIX.getText()
                            .replace("%prefix%", input), "/ep setprefix reset submit"));
                }
                return;
            }
            if (!args.get(args.size() - 1).equalsIgnoreCase("submit")) {
                user.getPlayer().spigot().sendMessage(buildConfirmComponent(Message.SUBMIT_PREFIX.getText()
                        .replace("%prefix%", input), "/ep setprefix " + input + " submit"));
            } else {
                user.setPrefix(input);
                user.saveData("custom_prefix_update", new Timestamp(System.currentTimeMillis()).toString());
                user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_PREFIX.getText()
                        .replace("%prefix%", user.getPrefix().replace("§", "&")));
            }
        } else if (args.get(0).equalsIgnoreCase("setsuffix")) {
            if (!user.hasPermission("custom.suffix")) {
                sender.sendMessage(Message.NO_PERMS.getMessage());
                return;
            }
            Timestamp next = getNextTimestamp(user.getLastSuffixUpdate());
            if (!next.before(new Timestamp(System.currentTimeMillis())) && !user.hasPermission("custom.bypass")) {
                user.getPlayer().sendMessage(getTimeMessage(next));
                return;
            }
            if (args.get(1).equalsIgnoreCase("reset")) {
                if (args.size() > 2 && args.get(2).equalsIgnoreCase("submit")) {
                    user.setSuffix(null);
                    user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_SUFFIX.getText()
                            .replace("%suffix%", user.getSuffix().replace("§", "&")));
                } else {
                    user.getPlayer().spigot().sendMessage(buildConfirmComponent(Message.RESET_PLAYER_SUFFIX.getText()
                            .replace("%suffix%", input), "/ep setsuffix reset submit"));
                }
                return;
            }
            if (!args.get(args.size() - 1).equalsIgnoreCase("submit")) {
                user.getPlayer().spigot().sendMessage(buildConfirmComponent(Message.SUBMIT_SUFFIX.getText()
                        .replace("%suffix%", input), "/ep setsuffix " + input + " submit"));
            } else {
                user.setSuffix(input);
                user.saveData("custom_suffix_update", new Timestamp(System.currentTimeMillis()).toString());
                user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_SUFFIX.getText()
                        .replace("%suffix%", user.getSuffix().replace("§", "&")));
            }
        }
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

    private Timestamp getNextTimestamp(long last) {
        double delay = ConfigKeys.CUSTOM_LAYOUT_COOLDOWN.toDouble();
        long newTime = (long) (last + (delay * 60 * 60 * 1000));
        return new Timestamp(newTime);
    }

    private String getTimeMessage(Timestamp timestamp) {
        long min = (timestamp.getTime() - System.currentTimeMillis()) / 1000 / 60;
        int minutes = (int) (min % 60);
        int hours = (int) ((min / 60) % 24);
        String msg = Message.LAYOUT_ERROR.getText();
        return msg.replace("%h%", Integer.toString(hours)).replace("%m%", (minutes == 0) ? "<1" : Integer.toString(minutes));
    }

    /**
     * @param args input by user
     * @return String translated input
     */
    private String readInput(List<String> args) {
        StringBuilder stringBuilder = new StringBuilder();
        int counter = 1;
        while (args.size() > counter) {
            String arg = args.get(counter);
            if (arg.equals("submit")) break;
            if (counter != 1) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(arg);
            counter++;
        }
        return stringBuilder.toString();
    }

    private TextComponent buildConfirmComponent(String text, String command) {
        TextComponent msg = new TextComponent(TextComponent.fromLegacyText(text.replace("%newline%", "\n")));
        TextComponent confirm = new TextComponent(TextComponent.fromLegacyText(" " + Message.CHAT_BTN_CONFIRM.getText() + " "));
        confirm.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        msg.addExtra(confirm);
        return msg;
    }

}
