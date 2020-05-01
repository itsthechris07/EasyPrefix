package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.responder.gui.SettingsGUI;
import com.christian34.easyprefix.setup.responder.gui.WelcomePage;
import com.christian34.easyprefix.user.Gender;
import com.christian34.easyprefix.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class CommandListener implements Listener, CommandExecutor {
    private EasyPrefix instance;

    public CommandListener(EasyPrefix instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        User user = null;
        if (sender instanceof Player) {
            user = this.instance.getUser((Player) sender);
        }
        GroupHandler groupHandler = this.instance.getGroupHandler();
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
                        new WelcomePage(user);
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
                        new SettingsGUI(user);
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
                sender.sendMessage(" ");
                sender.sendMessage("§7------------=== §5§lEasyPrefix DEBUG §7===------------");
                sender.sendMessage("§5Groups: §7" + groupHandler.getGroups().size() + "/" + groupHandler.getSubgroups().size());
                sender.sendMessage("§5Users cached: §7" + EasyPrefix.getInstance().getUsers().size());
                sender.sendMessage("§5Genders cached: §7" + Gender.getTypes().size());
                sender.sendMessage("§5Bukkit Version: §7" + Bukkit.getVersion());
                sender.sendMessage("§5Version Name: §7" + Bukkit.getBukkitVersion());
                sender.sendMessage("§5active EventHandler: §7" + HandlerList.getRegisteredListeners(EasyPrefix.getInstance().getPlugin()).size());
                return true;
            } else if (args[0].equalsIgnoreCase("database") && EasyPrefix.getInstance().getDatabase() != null && sender.hasPermission("easyprefix.admin")) {
                sender.sendMessage("§7---------------=== §5§lEasyPrefix §7===---------------");
                sender.sendMessage(" ");
                sender.sendMessage("§7/§5EasyPrefix database upload §f| §7upload groups and users to database (will override database!)");
                sender.sendMessage("§7/§5EasyPrefix database download §f| §7download groups and users to local " + "storage" + " " + "(will" + " override files and settings!)");
                sender.sendMessage(" ");
                sender.sendMessage("§7------------------------------------------------");
                sender.sendMessage(" ");
                return true;
            }
        } else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("set")) {
                if (sender.hasPermission("EasyPrefix.admin")) {
                    if (!(args.length == 3)) {
                        sender.sendMessage(Messages.getPrefix() + "§7Usage: /easyprefix set <Player> <Prefix>");
                        return false;
                    } else if (!groupHandler.isGroup(args[2])) {
                        sender.sendMessage(Messages.getMessage(Message.GROUP_NOT_FOUND));
                        return false;
                    }
                    Player targetPlayer = Bukkit.getPlayer(args[1]);
                    Group group = groupHandler.getGroup(args[2]);
                    if (targetPlayer != null) {
                        User target = EasyPrefix.getInstance().getUser(targetPlayer);
                        target.setGroup(group, true);
                        sender.sendMessage(Messages.getMessage(Message.SUCCESS));
                        return true;
                    } else {
                        OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
                        File userFile = new File(FileManager.getPluginFolder() + "/user", op.getUniqueId() + ".yml");
                        if (op.hasPlayedBefore() && userFile.exists()) {
                            FileConfiguration userData = YamlConfiguration.loadConfiguration(userFile);
                            userData.set("user.group", group.getName());
                            try {
                                userData.save(userFile);
                            } catch(IOException e) {
                                e.printStackTrace();
                            }
                            sender.sendMessage(Messages.getMessage(Message.SUCCESS));
                            return true;
                        } else {
                            sender.sendMessage(Messages.getMessage(Message.PLAYER_NOT_FOUND));
                            return false;
                        }
                    }
                } else {
                    sender.sendMessage(Messages.getMessage(Message.NO_PERMS));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("group")) {
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
                            sender.sendMessage(" ");
                            sender.sendMessage("§7--------------=== §5§l" + group.getName() + " §7===--------------");
                            sender.sendMessage(" ");
                            sender.sendMessage("§5Prefix§f: §8«§7" + group.getPrefix().replace("§", "&") + "§8»");
                            sender.sendMessage("§5Suffix§f: §8«§7" + group.getSuffix().replace("§", "&") + "§8»");
                            String cc = (group.getChatColor() != null) ? group.getChatColor().getCode() : "-";
                            if (group.getChatFormatting() != null) cc = cc + group.getChatFormatting().getCode();
                            sender.sendMessage("§5Chatcolor§f: §7" + cc.replace("§", "&"));
                            sender.sendMessage("§5Join message§f: §7" + group.getJoinMessage());
                            sender.sendMessage("§5Quit message§f: §7" + group.getQuitMessage());
                            sender.sendMessage(" ");
                            sender.sendMessage("§7-----------------------------------------------");
                            return true;
                        }
                    } else {
                        sender.sendMessage(" ");
                        sender.sendMessage("§7--------------=== §5§lEasyPrefix Group §7===--------------");
                        sender.sendMessage(" ");
                        sender.sendMessage("§7/§5EasyPrefix group <Group> info §f| §7get information about the group");
                        sender.sendMessage(" ");
                        sender.sendMessage("§7----------------------------------------------------");
                    }
                }
            } else if (args[0].equalsIgnoreCase("user")) {
                if (sender.hasPermission("EasyPrefix.admin")) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player != null) {
                        User target = new User(player);
                        if (args.length >= 3) {
                            if (args[2].equalsIgnoreCase("update")) {
                                Bukkit.getScheduler().runTaskLaterAsynchronously(EasyPrefix.getInstance().getPlugin(), () -> {
                                    target.load();
                                    sender.sendMessage(Messages.getMessage(Message.SUCCESS));
                                }, 20L);
                                return true;
                            } else if (args[2].equalsIgnoreCase("info")) {
                                sender.sendMessage(" ");
                                sender.sendMessage("§7--------------=== §5§l" + target.getPlayer().getName() + " §7===--------------");
                                sender.sendMessage(" ");
                                sender.sendMessage("§5Group§f: §7" + target.getGroup().getName());
                                String subgroup = (target.getSubgroup() != null) ? target.getSubgroup().getName() : "-";
                                sender.sendMessage("§5Subgroup§f: §7" + subgroup);
                                sender.sendMessage("§5Prefix§f: §8«§7" + target.getPrefix().replace("§", "&") + "§8»");
                                sender.sendMessage("§5Suffix§f: §8«§7" + target.getSuffix().replace("§", "&") + "§8»");
                                String cc = (target.getChatColor() != null) ? target.getChatColor().getCode() : "-";
                                if (target.getChatFormatting() != null) cc = cc + target.getChatFormatting().getCode();
                                sender.sendMessage("§5Chatcolor§f: §7" + cc.replace("§", "&"));
                                if (target.getGender() != null) {
                                    sender.sendMessage("§5Gender§f: §7" + target.getGender().getName() + "§7/§7" + target.getGender().getId());
                                }
                                sender.sendMessage(" ");
                                sender.sendMessage("§7-----------------------------------------------");
                                return true;
                            } else if (args[2].equalsIgnoreCase("setgroup")) {
                                if (args.length == 4) {
                                    if (groupHandler.isGroup(args[3])) {
                                        Group targetGroup = groupHandler.getGroup(args[3]);
                                        target.setGroup(targetGroup, true);
                                        sender.sendMessage(Messages.getMessage(Message.SUCCESS));
                                        return true;
                                    } else {
                                        sender.sendMessage(Messages.getMessage(Message.GROUP_NOT_FOUND));
                                        return false;
                                    }
                                }
                            } else if (args[2].equalsIgnoreCase("setsubgroup")) {
                                if (args.length == 4) {
                                    if (groupHandler.isSubgroup(args[3])) {
                                        Subgroup targetGroup = groupHandler.getSubgroup(args[3]);
                                        target.setSubgroup(targetGroup);
                                        sender.sendMessage(Messages.getMessage(Message.SUCCESS));
                                        return true;
                                    } else {
                                        sender.sendMessage(Messages.getMessage(Message.GROUP_NOT_FOUND));
                                        return false;
                                    }
                                }
                            } else if (args[2].equalsIgnoreCase("setgender")) {
                                if (args.length == 4) {
                                    Gender gender = Gender.get(args[3]);
                                    if (gender != null) {
                                        target.setGender(gender);
                                        sender.sendMessage(Messages.getMessage(Message.SUCCESS));
                                        return true;
                                    } else {
                                        sender.sendMessage(Messages.getPrefix() + "§cThis gender doesn't exist");
                                        return false;
                                    }
                                }
                            }
                        }
                    } else {
                        sender.sendMessage(Messages.getMessage(Message.PLAYER_NOT_FOUND));
                        return false;
                    }
                    sender.sendMessage(" ");
                    sender.sendMessage("§7--------------=== §5§lEasyPrefix User §7===--------------");
                    sender.sendMessage(" ");
                    sender.sendMessage("§7/§5EasyPrefix user <Player> info §f| §7get information about the player");
                    sender.sendMessage("§7/§5EasyPrefix user <Player> update §f| §7update player data");
                    sender.sendMessage("§7/§5EasyPrefix user <Player> setgroup <Group> §f| §7force group to player");
                    sender.sendMessage("§7/§5EasyPrefix user <Player> setsubgroup <Subgroup> §f| §7set subgroup to player");
                    sender.sendMessage("§7/§5EasyPrefix user <Player> setgender <Gender> §f| §7set gender");
                    sender.sendMessage(" ");
                    sender.sendMessage("§7----------------------------------------------------");
                    return false;
                } else {
                    sender.sendMessage(Messages.getMessage(Message.NO_PERMS));
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("gui")) {
                if (args.length > 2) {
                    if (args[1].equalsIgnoreCase("settings")) {
                        if (args[2].equalsIgnoreCase("gender")) {
                            new SettingsGUI(user).openGenderPage();
                        } else if (args[2].equalsIgnoreCase("group")) {
                            new SettingsGUI(user).openGroupsPage();
                        } else if (args[2].equalsIgnoreCase("subgroups")) {
                            new SettingsGUI(user).openSubgroupsPage();
                        } else if (args[2].equalsIgnoreCase("color")) {
                            new SettingsGUI(user).openColorsPage();
                        }
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("database") && EasyPrefix.getInstance().getDatabase() != null) {
                if (sender.hasPermission("easyprefix.admin")) {
                    if (args[1].equalsIgnoreCase("upload")) {
                        sender.sendMessage(Messages.getPrefix() + "§7Uploading data to database. This could take a while.");
                        try {
                            EasyPrefix.getInstance().getDatabase().uploadData();
                            EasyPrefix.getInstance().reload();
                            sender.sendMessage(Messages.getPrefix() + "§7Files have been uploaded!");
                        } catch(SQLException e) {
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    } else if (args[1].equalsIgnoreCase("download")) {
                        sender.sendMessage(Messages.getPrefix() + "§7Downloading data to local storage This could take a while.");
                        try {
                            EasyPrefix.getInstance().getDatabase().downloadData();
                            EasyPrefix.getInstance().reload();
                            sender.sendMessage(Messages.getPrefix() + "§7Files have been downloaded!");
                        } catch(SQLException e) {
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
        sender.sendMessage(" ");
        sender.sendMessage("§7---------------=== §5§lEasyPrefix §7===---------------");
        sender.sendMessage(" ");
        sender.sendMessage("§7/§5EasyPrefix §f| §7main command");
        sender.sendMessage("§7/§5EasyPrefix settings §f| §7manage your prefixes");
        sender.sendMessage("§7/§5EasyPrefix setup §f| §7opens setup gui");
        sender.sendMessage("§7/§5EasyPrefix reload §f| §7reloads the plugin");
        sender.sendMessage("§7/§5EasyPrefix user <Player> §f| §7player info");
        sender.sendMessage("§7/§5EasyPrefix group <Group> §f| §7group info");
        if (EasyPrefix.getInstance().getDatabase() != null && sender.hasPermission("easyprefix.admin")) {
            sender.sendMessage("§7/§5EasyPrefix database §f| §7sql configuration");
        }
        sender.sendMessage(" ");
        sender.sendMessage("§7------------------------------------------------");
        sender.sendMessage(" ");
        sender.sendMessage("§7Version: " + EasyPrefix.getInstance().getPlugin().getDescription().getVersion());
        sender.sendMessage("§7EasyPrefix by §5§lChristian34");
        return false;
    }

}