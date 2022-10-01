package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.VersionController;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
class DebugCommand implements Subcommand {
    private final EasyPrefix instance;
    private final GroupHandler groupHandler;

    public DebugCommand(EasyPrefixCommand parentCommand) {
        this.instance = parentCommand.getInstance();
        this.groupHandler = instance.getGroupHandler();
    }

    @Override
    @NotNull
    public String getName() {
        return "debug";
    }

    @Override
    public UserPermission getPermission() {
        return UserPermission.ADMIN;
    }

    @Override
    @Nullable
    public String getDescription() {
        return "shows useful information";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "debug";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        sender.sendMessage(" \n§7------------=== §9§lEasyPrefix DEBUG §7===------------");
        sender.sendMessage("§9Version: §7" + VersionController.getPluginVersion());
        sender.sendMessage("§9Groups/Subgroups: §7" + groupHandler.getGroups().size() + "/" + groupHandler.getSubgroups().size());
        sender.sendMessage("§9Users cached: §7" + this.instance.getUsers().size());
        sender.sendMessage("§9Bukkit Version: §7" + Bukkit.getVersion());
        sender.sendMessage("§9Java Version: §7" + System.getProperty("java.version"));
        sender.sendMessage("§9Version Name: §7" + Bukkit.getBukkitVersion());
        sender.sendMessage("§9Storage: §7" + ((this.instance.getStorageType() == StorageType.SQL) ? "MySQL" : "local"));
        sender.sendMessage("§9active EventHandler: §7" + HandlerList.getRegisteredListeners(this.instance.getPlugin()).size());
        sender.sendMessage("§9Client ID: §7" + instance.getConfigData().getString(ConfigData.Keys.CLIENT_ID));
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
