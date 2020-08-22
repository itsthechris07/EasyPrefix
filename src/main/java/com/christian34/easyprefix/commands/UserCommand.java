package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.GenderType;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class UserCommand implements Subcommand {
    private final CommandHandler commandHandler;
    private final EasyPrefix instance;
    private final GroupHandler groupHandler;

    public UserCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
        this.instance = commandHandler.getInstance();
        this.groupHandler = instance.getGroupHandler();
    }

    @Override
    public String getName() {
        return "user";
    }

    @Override
    public String getPermission() {
        return "admin";
    }

    @Override
    public void handleCommand(CommandSender sender, List<String> args) {
        if (!(args.size() > 1)) {
            showHelp(sender);
            return;
        }
        Player player = Bukkit.getPlayer(args.get(1));
        if (player == null) {
            sender.sendMessage(Messages.getMessage(Message.PLAYER_NOT_FOUND));
            return;
        }
        GroupHandler groupHandler = commandHandler.getInstance().getGroupHandler();
        User target = new User(player);
        target.login();
        if (args.size() < 3) {
            showHelp(sender);
            return;
        }
        if (args.get(2).equalsIgnoreCase("reload")) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(EasyPrefix.getInstance().getPlugin(), () -> {
                instance.unloadUser(target.getPlayer());
                instance.getUser(target.getPlayer()).login();
                sender.sendMessage(Messages.getMessage(Message.SUCCESS));
            }, 20L);
        } else if (args.get(2).equalsIgnoreCase("info")) {
            showInfo(sender, target);
        } else if (args.get(2).equalsIgnoreCase("setgroup")) {
            if (args.size() != 4) {
                showHelp(sender);
            } else {
                if (groupHandler.isGroup(args.get(3))) {
                    Group targetGroup = groupHandler.getGroup(args.get(3));
                    target.setGroup(targetGroup, true);
                    sender.sendMessage(Messages.getMessage(Message.SUCCESS));
                } else {
                    sender.sendMessage(Messages.getMessage(Message.GROUP_NOT_FOUND));
                }
            }
        } else if (args.get(2).equalsIgnoreCase("setsubgroup")) {
            if (args.size() != 4) {
                showHelp(sender);
            } else {
                if (groupHandler.isSubgroup(args.get(3))) {
                    Subgroup targetGroup = groupHandler.getSubgroup(args.get(3));
                    target.setSubgroup(targetGroup);
                    sender.sendMessage(Messages.getMessage(Message.SUCCESS));
                } else if (args.get(3).equalsIgnoreCase("none")) {
                    target.setSubgroup(null);
                    sender.sendMessage(Messages.getMessage(Message.SUCCESS));
                } else {
                    sender.sendMessage(Messages.getMessage(Message.GROUP_NOT_FOUND));
                }
            }
        } else if (args.get(2).equalsIgnoreCase("setgender")) {
            if (args.size() != 4) {
                showHelp(sender);
            } else {
                setGender(sender, target, args.get(3));
            }
        }
    }

    private void showInfo(CommandSender sender, User targetUser) {
        String subgroup = (targetUser.getSubgroup() != null) ? targetUser.getSubgroup().getName() : "-";
        String cc = (targetUser.getChatColor() != null) ? targetUser.getChatColor().getCode() : "-";

        sender.sendMessage(" \n§7--------------=== §5§l" + targetUser.getPlayer().getName() + " §7===--------------\n ");
        sender.sendMessage("§5Group§f: §7" + targetUser.getGroup().getName());
        sender.sendMessage("§5Subgroup§f: §7" + subgroup);
        sender.sendMessage("§5Prefix§f: §8«§7" + targetUser.getPrefix().replace("§", "&") + "§8»" + (targetUser.hasCustomPrefix() ? " §7(§5customized§7)" : ""));
        if (targetUser.hasCustomPrefix()) {
            sender.sendMessage(" §7↳ §5last update§f: §7" + new Timestamp(targetUser.getLastPrefixUpdate()).toString());
        }
        sender.sendMessage("§5Suffix§f: §8«§7" + targetUser.getSuffix().replace("§", "&") + "§8»" + (targetUser.hasCustomSuffix() ? " §7(§5customized§7)" : ""));
        if (targetUser.hasCustomSuffix()) {
            sender.sendMessage(" §7↳ §5last update§f: §7" + new Timestamp(targetUser.getLastSuffixUpdate()).toString());
        }
        if (targetUser.getChatFormatting() != null) cc = cc + targetUser.getChatFormatting().getCode();
        sender.sendMessage("§5Chatcolor§f: §7" + cc.replace("§", "&"));
        if (targetUser.getGenderType() != null) {
            sender.sendMessage("§5Gender§f: §7" + targetUser.getGenderType().getDisplayName() + "§7/§7" + targetUser.getGenderType().getName());
        }
        sender.sendMessage(" \n§7-----------------------------------------------\n ");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(" \n§7--------------=== §5§lEasyPrefix User §7===--------------\n ");
        sender.sendMessage("§7/§5EasyPrefix user <Player> info §f| §7get information about the player");
        sender.sendMessage("§7/§5EasyPrefix user <Player> setgroup <Group> §f| §7force group to player");
        sender.sendMessage("§7/§5EasyPrefix user <Player> setsubgroup <Subgroup> §f| §7set subgroup to player");
        sender.sendMessage("§7/§5EasyPrefix user <Player> setgender <Gender> §f| §7set gender");
        sender.sendMessage(" \n§7----------------------------------------------------\n ");
    }

    private void setGender(CommandSender sender, User targetUser, String gender) {
        GenderType genderType = groupHandler.getGender(gender);
        if (genderType != null) {
            targetUser.setGenderType(genderType);
            sender.sendMessage(Messages.getMessage(Message.SUCCESS));
        } else {
            sender.sendMessage(Messages.getPrefix() + "§cThis gender doesn't exist");
        }
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, List<String> args) {
        if (args.size() == 2) {
            return null;
        } else if (args.size() == 3) {
            List<String> matches = Arrays.asList("reload", "info", "setgroup", "setgender", "setsubgroup");
            if (args.get(2).isEmpty()) {
                return matches;
            } else {
                List<String> complete = new ArrayList<>();
                for (String sub : matches) {
                    if (sub.startsWith(args.get(2))) {
                        complete.add(sub);
                    }
                }
                return complete;
            }
        } else if (args.size() == 4) {
            List<String> matches = new ArrayList<>();
            if (args.get(2).equalsIgnoreCase("setgroup")) {
                for (Group group : this.instance.getGroupHandler().getGroups()) {
                    matches.add(group.getName());
                }
            } else if (args.get(2).equalsIgnoreCase("setsubgroup")) {
                for (Subgroup group : this.instance.getGroupHandler().getSubgroups()) {
                    matches.add(group.getName());
                }
            } else if (args.get(2).equalsIgnoreCase("setgender")) {
                for (GenderType gender : this.instance.getGroupHandler().getGenderTypes()) {
                    matches.add(gender.getName());
                }
            }
            return matches;
        }
        return Collections.emptyList();
    }

}