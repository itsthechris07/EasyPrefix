package com.christian34.easyprefix.commands.set;

import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.commands.easyprefix.EasyPrefixCommand;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class SetPrefixCommand implements Subcommand {
    private final EasyPrefixCommand parentCommand;

    public SetPrefixCommand(EasyPrefixCommand parentCommand) {
        this.parentCommand = parentCommand;
    }

    @Override
    @Nullable
    public UserPermission getPermission() {
        return UserPermission.CUSTOM_PREFIX;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "changes your prefix, reset it to default value with command 'setprefix reset'";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "setprefix <prefix> or setprefix reset";
    }

    @Override
    @NotNull
    public String getName() {
        return "setprefix";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.PREFIX + Message.CHAT_PLAYER_ONLY);
            return;
        }

        if (args.size() < 2) {
            parentCommand.getSubcommand("help").handleCommand(sender, null);
            return;
        }

        User user = parentCommand.getInstance().getUser((Player) sender);
        String input = CommandUtils.readInput(args);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        Timestamp next = CommandUtils.getNextTimestamp(user.getLastPrefixUpdate());
        if (!next.before(currentTime) && !user.hasPermission(UserPermission.CUSTOM_BYPASS)) {
            user.getPlayer().sendMessage(CommandUtils.getTimeMessage(next));
            return;
        }

        if (args.get(1).equalsIgnoreCase("reset")) {
            if (args.size() > 2 && args.get(2).equalsIgnoreCase("submit")) {
                user.setPrefix(null);
                user.getPlayer().sendMessage(Message.CHAT_INPUT_PREFIX_SAVED.getText()
                        .replace("%content%", user.getPrefix().replace("§", "&")));
            } else {
                user.getPlayer().spigot().sendMessage(CommandUtils.buildConfirmComponent(Message.CHAT_INPUT_PREFIX_RESET.getText()
                        .replace("%content%", input), "/ep setprefix reset submit"));
            }
            return;
        }

        if (!args.get(args.size() - 1).equalsIgnoreCase("submit")) {
            user.getPlayer().spigot().sendMessage(CommandUtils.buildConfirmComponent(Message.CHAT_INPUT_PREFIX_CONFIRM.getText()
                    .replace("%content%", input), "/ep setprefix " + input + " submit"));
        } else {
            if (input.equals("null")) {
                user.setPrefix(null);
            } else {
                user.setPrefix(input);
            }
            user.saveData("custom_prefix_update", currentTime.toString());
            user.getPlayer().sendMessage(Message.CHAT_INPUT_PREFIX_SAVED.getText()
                    .replace("%content%", user.getPrefix().replace("§", "&")));
        }
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
