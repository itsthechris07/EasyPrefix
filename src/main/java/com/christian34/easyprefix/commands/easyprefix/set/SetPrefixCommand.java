package com.christian34.easyprefix.commands.easyprefix.set;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.UserInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2021.
 *
 * @author Christian34
 */
public class SetPrefixCommand implements Subcommand, CommandExecutor {

    @Override
    @Nullable
    public UserPermission getPermission() {
        return UserPermission.CUSTOM_PREFIX;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "modify your prefix";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "setprefix";
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

        User user = EasyPrefix.getInstance().getUser((Player) sender);
        UserInterface gui = new UserInterface(user);
        gui.showCustomPrefixGui();
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        handleCommand(commandSender, Arrays.asList(strings));
        return true;
    }
}
