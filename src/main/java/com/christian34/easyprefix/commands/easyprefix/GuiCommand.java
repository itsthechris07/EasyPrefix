package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.gui.pages.GuiSettings;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GuiCommand implements Subcommand {
    private final EasyPrefix instance;

    public GuiCommand(EasyPrefix instance) {
        this.instance = instance;
    }

    @Override
    @NotNull
    public String getName() {
        return "gui";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (args.size() < 2 || !(sender instanceof Player)) {
            return;
        }
        User user = instance.getUser((Player) sender);

        if (args.get(1).equals("settings")) {
            if (args.size() == 3) {
                if (!instance.getConfigData().getBoolean(ConfigData.Keys.USE_GENDER)) {
                    return;
                }
                if (args.get(2).equals("gender")) {
                    new GuiSettings(user).openGenderSelectPage(() -> user.getPlayer().closeInventory());
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

    @Override
    @Nullable
    public UserPermission getPermission() {
        return null;
    }

    @Override
    @Nullable
    public String getDescription() {
        return null;
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "";
    }

}
