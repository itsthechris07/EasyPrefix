package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.utils.VersionController;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class DebugCommand implements Subcommand {
    private final CommandHandler commandHandler;
    private final EasyPrefix instance;
    private final GroupHandler groupHandler;

    public DebugCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
        this.instance = commandHandler.getInstance();
        this.groupHandler = instance.getGroupHandler();
    }

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public String getPermission() {
        return "admin";
    }

    @Override
    public void handleCommand(CommandSender sender, List<String> args) {
        sender.sendMessage(" \n§7------------=== §5§lEasyPrefix DEBUG §7===------------");
        sender.sendMessage("§5Version: §7" + VersionController.getPluginVersion());
        sender.sendMessage("§5Groups: §7" + groupHandler.getGroups().size() + "/" + groupHandler.getSubgroups().size());
        sender.sendMessage("§5Users cached: §7" + this.instance.getUsers().size());
        sender.sendMessage("§5Genders cached: §7" + groupHandler.getGenderTypes().size());
        sender.sendMessage("§5Bukkit Version: §7" + Bukkit.getVersion());
        sender.sendMessage("§5Java Version: §7" + System.getProperty("java.version"));
        sender.sendMessage("§5Version Name: §7" + Bukkit.getBukkitVersion());
        sender.sendMessage("§5Storage: §7" + ((this.instance.getSqlDatabase() != null) ? "MySQL" : "local"));
        sender.sendMessage("§5active EventHandler: §7" + HandlerList.getRegisteredListeners(this.instance.getPlugin()).size());
        sender.sendMessage("§5Client ID: §7" + ConfigKeys.CLIENT_ID.toString());
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
