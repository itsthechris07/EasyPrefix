package com.christian34.easyprefix.commands.set;

import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.commands.easyprefix.EasyPrefixCommand;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
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
public class SetSuffixCommand implements Subcommand {
    private final EasyPrefixCommand parentCommand;

    public SetSuffixCommand(EasyPrefixCommand parentCommand) {
        this.parentCommand = parentCommand;
    }

    @Override
    @Nullable
    public UserPermission getPermission() {
        return UserPermission.CUSTOM_SUFFIX;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "changes your suffix, reset it to default value with command 'setsuffix reset'";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "setsuffix <suffix> or setsuffix reset";
    }

    @Override
    @NotNull
    public String getName() {
        return "setsuffix";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.PLAYER_ONLY.getMessage());
            return;
        }

        if (args.size() < 2) {
            parentCommand.getSubcommand("help").handleCommand(sender, null);
            return;
        }

        User user = parentCommand.getInstance().getUser((Player) sender);
        String input = CommandUtils.readInput(args);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        Timestamp next = CommandUtils.getNextTimestamp(user.getLastSuffixUpdate());
        if (!next.before(currentTime) && !user.hasPermission(UserPermission.CUSTOM_BYPASS)) {
            user.getPlayer().sendMessage(CommandUtils.getTimeMessage(next));
            return;
        }

        if (args.get(1).equalsIgnoreCase("reset")) {
            if (args.size() > 2 && args.get(2).equalsIgnoreCase("submit")) {
                user.setSuffix(null);
                user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_SUFFIX.getText()
                        .replace("%suffix%", user.getSuffix().replace("§", "&")));
            } else {
                user.getPlayer().spigot().sendMessage(CommandUtils.buildConfirmComponent(Message.RESET_PLAYER_SUFFIX.getText()
                        .replace("%suffix%", input), "/ep setsuffix reset submit"));
            }
            return;
        }

        if (!args.get(args.size() - 1).equalsIgnoreCase("submit")) {
            user.getPlayer().spigot().sendMessage(CommandUtils.buildConfirmComponent(Message.SUBMIT_SUFFIX.getText()
                    .replace("%suffix%", input), "/ep setsuffix " + input + " submit"));
        } else {
            user.setSuffix(input);
            user.saveData("custom_suffix_update", currentTime.toString());
            user.getPlayer().sendMessage(Message.SUCCESS_PLAYER_SUFFIX.getText()
                    .replace("%suffix%", user.getSuffix().replace("§", "&")));
        }
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
