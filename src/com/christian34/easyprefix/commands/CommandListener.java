package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.responder.gui.pages.GuiSettings;
import com.christian34.easyprefix.setup.responder.gui.pages.GuiSetup;
import com.christian34.easyprefix.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class CommandListener implements Listener, CommandExecutor {
    private final EasyPrefix instance;

    public CommandListener(EasyPrefix instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        User user = (sender instanceof Player) ? this.instance.getUser((Player) sender) : null;
        GroupHandler groupHandler = this.instance.getGroupHandler();
        ConfigData config = instance.getFileManager().getConfig();

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("EasyPrefix.admin")) {
                    this.instance.reload();
                    sender.sendMessage(Messages.getMessage(Message.RELOAD_COMPLETE));
                    return true;
                } else {
                    sender.sendMessage(Messages.getMessage(Message.NO_PERMS, user));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("setup")) {
                if (user != null) {
                    if (sender.hasPermission("EasyPrefix.admin")) {
                        new GuiSetup(user).mainPage();
                        return true;
                    } else {
                        sender.sendMessage(Messages.getMessage(Message.NO_PERMS, user));
                        return false;
                    }
                } else {
                    sender.sendMessage(Messages.getMessage(Message.PLAYER_ONLY));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("settings")) {
                if (user != null) {
                    if (sender.hasPermission("EasyPrefix.settings")) {
                        new GuiSettings(user).openWelcomePage();
                        return true;
                    } else {
                        sender.sendMessage(Messages.getMessage(Message.NO_PERMS, user));
                        return false;
                    }
                } else {
                    sender.sendMessage(Messages.getMessage(Message.PLAYER_ONLY));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("debug") && sender.hasPermission("easyprefix.admin.debug")) {
                sender.sendMessage(" \n§7------------=== §5§lEasyPrefix DEBUG §7===------------");
                sender.sendMessage("§5Version: §7" + this.instance.getPlugin().getDescription().getVersion());
                sender.sendMessage("§5Groups: §7" + groupHandler.getGroups().size() + "/" + groupHandler.getSubgroups().size());
                sender.sendMessage("§5Users cached: §7" + this.instance.getUsers().size());
                sender.sendMessage("§5Genders cached: §7" + groupHandler.getGenderTypes().size());
                sender.sendMessage("§5Bukkit Version: §7" + Bukkit.getVersion());
                sender.sendMessage("§5Java Version: §7" + System.getProperty("java.version"));
                sender.sendMessage("§5Version Name: §7" + Bukkit.getBukkitVersion());
                sender.sendMessage("§5Storage: §7" + ((this.instance.getSqlDatabase() != null) ? "MySQL" : "local"));
                sender.sendMessage("§5active EventHandler: §7" + HandlerList.getRegisteredListeners(this.instance.getPlugin()).size());
                return true;
            } else if (args[0].equalsIgnoreCase("database") && this.instance.getSqlDatabase() != null && sender.hasPermission("easyprefix.admin")) {
                sender.sendMessage("§7---------------=== §5§lEasyPrefix §7===---------------\n ");
                sender.sendMessage("§7/§5EasyPrefix database upload §f| §7upload groups and users to database (will override database!)");
                sender.sendMessage("§7/§5EasyPrefix database download §f| §7download groups and users to local " + "storage" + " " + "(will" + " override files and settings!)");
                sender.sendMessage(" \n§7------------------------------------------------\n ");
                return true;
            }
        } else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("group")) {
                if (sender.hasPermission("EasyPrefix.admin")) {
                    Group group;
                    if (groupHandler.isGroup(args[1])) {
                        group = groupHandler.getGroup(args[1]);
                    } else {
                        sender.sendMessage(Messages.getPrefix() + "§cGroup was not found!");
                        return false;
                    }
                    if (args.length >= 3) {
                        if (args[2].equalsIgnoreCase("info")) {
                            sender.sendMessage(" \n§7--------------=== §5§l" + group.getName() + " §7===--------------\n ");
                            sender.sendMessage("§5Prefix§f: §8«§7" + group.getPrefix(null, false) + "§8»");
                            sender.sendMessage("§5Suffix§f: §8«§7" + group.getSuffix(null, false) + "§8»");
                            String cc = (group.getChatColor() != null) ? group.getChatColor().getCode() : "-";
                            if (group.getChatFormatting() != null) cc = cc + group.getChatFormatting().getCode();
                            sender.sendMessage("§5Chatcolor§f: §7" + cc.replace("§", "&"));
                            sender.sendMessage("§5Join message§f: §7" + group.getJoinMessageText());
                            sender.sendMessage("§5Quit message§f: §7" + group.getQuitMessageText());
                            sender.sendMessage(" \n§7-----------------------------------------------\n ");
                            return true;
                        }
                    }
                    sender.sendMessage(" \n§7--------------=== §5§lEasyPrefix Group §7===--------------\n ");
                    sender.sendMessage("§7/§5EasyPrefix group <Group> info §f| §7get information about the group");
                    sender.sendMessage(" \n§7----------------------------------------------------\n ");
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("user")) {
                EasyCommand userCommand = new Command_User();
                if (sender.hasPermission(userCommand.getPermission())) {
                    if (!userCommand.handleCommand(sender, Arrays.asList(args))) {
                        sender.sendMessage(" \n§7--------------=== §5§lEasyPrefix User §7===--------------\n ");
                        sender.sendMessage("§7/§5EasyPrefix user <Player> info §f| §7get information about the player");
                        sender.sendMessage("§7/§5EasyPrefix user <Player> update §f| §7update player data");
                        sender.sendMessage("§7/§5EasyPrefix user <Player> setgroup <Group> §f| §7force group to player");
                        sender.sendMessage("§7/§5EasyPrefix user <Player> setsubgroup <Subgroup> §f| §7set subgroup to player");
                        sender.sendMessage("§7/§5EasyPrefix user <Player> setgender <Gender> §f| §7set gender");
                        sender.sendMessage(" \n§7----------------------------------------------------\n ");
                        return false;
                    }
                    return true;
                } else {
                    sender.sendMessage(Messages.getMessage(Message.NO_PERMS));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("setprefix") || args[0].equalsIgnoreCase("setsuffix")) {
                if (config.getBoolean(ConfigData.ConfigKeys.CUSTOM_LAYOUT)) {
                    EasyCommand layoutCommand = new Command_Custom();
                    layoutCommand.handleCommand(sender, Arrays.asList(args));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("gui")) {
                if (user == null) return false;
                if (args.length > 2) {
                    if (args[1].equalsIgnoreCase("settings")) {
                        GuiSettings gui = new GuiSettings(user);
                        if (args[2].equalsIgnoreCase("gender")) {
                            gui.openGenderSelectPage();
                        } else if (args[2].equalsIgnoreCase("group")) {
                            gui.openGroupsListPage();
                        } else if (args[2].equalsIgnoreCase("subgroups")) {
                            gui.openSubgroupsPage(() -> user.getPlayer().closeInventory());
                        } else if (args[2].equalsIgnoreCase("color")) {
                            gui.openColorsPage();
                        }
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("database") && this.instance.getSqlDatabase() != null) {
                if (sender.hasPermission("easyprefix.admin")) {
                    if (args[1].equalsIgnoreCase("upload")) {
                        sender.sendMessage(Messages.getPrefix() + "§7Uploading data to database. This could take a while.");
                        try {
                            this.instance.getSqlDatabase().uploadData();
                            this.instance.reload();
                            sender.sendMessage(Messages.getPrefix() + "§7Files have been uploaded!");
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    } else if (args[1].equalsIgnoreCase("download")) {
                        sender.sendMessage(Messages.getPrefix() + "§7Downloading data to local storage. This could take a while.");
                        try {
                            this.instance.getSqlDatabase().downloadData();
                            this.instance.reload();
                            sender.sendMessage(Messages.getPrefix() + "§7Files have been downloaded!");
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    }
                    return true;
                } else {
                    sender.sendMessage(Messages.getMessage(Message.NO_PERMS));
                    return false;
                }
            }
        }
        sender.sendMessage(" \n§7---------------=== §5§lEasyPrefix §7===---------------\n ");
        sender.sendMessage("§7/§5EasyPrefix §f| §7main command");
        if (sender.hasPermission("EasyPrefix.settings")) {
            sender.sendMessage("§7/§5EasyPrefix settings §f| §7manage your prefixes");
        }
        if (sender.hasPermission("EasyPrefix.admin")) {
            sender.sendMessage("§7/§5EasyPrefix setup §f| §7opens setup gui");
            sender.sendMessage("§7/§5EasyPrefix reload §f| §7reloads the plugin");
            sender.sendMessage("§7/§5EasyPrefix user <Player> §f| §7player info");
            sender.sendMessage("§7/§5EasyPrefix group <Group> §f| §7group info");
            if (this.instance.getSqlDatabase() != null) {
                sender.sendMessage("§7/§5EasyPrefix database §f| §7sql configuration");
            }
        }
        if (config.getBoolean(ConfigData.ConfigKeys.CUSTOM_LAYOUT)) {
            if (sender.hasPermission("easyprefix.custom.prefix")) {
                sender.sendMessage("§7/§5EasyPrefix setprefix <Prefix> §f| §7set prefix");
            }
            if (sender.hasPermission("easyprefix.custom.suffix")) {
                sender.sendMessage("§7/§5EasyPrefix setsuffix <Suffix> §f| §7set suffix");
            }
        }
        sender.sendMessage(" \n§7------------------------------------------------\n ");
        sender.sendMessage("§7Version: " + this.instance.getPlugin().getDescription().getVersion());
        sender.sendMessage("§7EasyPrefix by §5§lChristian34");
        return false;
    }

}