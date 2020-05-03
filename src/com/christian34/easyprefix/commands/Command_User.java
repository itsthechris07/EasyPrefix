package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Gender;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Command_User extends EasyCommand {
    private EasyPrefix instance;

    public Command_User(EasyPrefix instance) {
        this.instance = instance;
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String[] args) {
        Player player = Bukkit.getPlayer(args[1]);
        GroupHandler groupHandler = instance.getGroupHandler();
        if (player == null) {
            sender.sendMessage(Messages.getMessage(Message.PLAYER_NOT_FOUND));
            return true;
        }
        User target = new User(player);
        if (args.length >= 3) {
            if (args[2].equalsIgnoreCase("reload")) {
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
        return false;
    }

    @Override
    public String getPermission() {
        return "EasyPrefix.admin";
    }

}