package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.UserInterface;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
class SettingsCommand implements Subcommand {
    private final EasyPrefix instance;

    public SettingsCommand(EasyPrefixCommand parentCommand) {
        this.instance = parentCommand.getInstance();
    }

    @Override
    @NotNull
    public String getName() {
        return "settings";
    }

    @Override
    public UserPermission getPermission() {
        return UserPermission.SETTINGS;
    }

    @Override
    @Nullable
    public String getDescription() {
        return "opens the graphical user interface which allows you to make settings";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "settings";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        User user = sender instanceof Player ? instance.getUser((Player) sender) : null;
        if (user != null) {
            UserInterface gui = new UserInterface(user);
            gui.openUserSettings();
        } else {
            sender.sendMessage(Message.PREFIX + Message.CHAT_PLAYER_ONLY);
        }
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }
}
