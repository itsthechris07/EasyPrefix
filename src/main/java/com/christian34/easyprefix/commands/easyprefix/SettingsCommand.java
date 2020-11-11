package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.responder.gui.pages.GuiSettings;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
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
    @NotNull
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
            new GuiSettings(user).openWelcomePage();
        } else {
            sender.sendMessage(Message.PLAYER_ONLY.getMessage());
        }
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }
}
