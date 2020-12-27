package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
class UserCommand implements Subcommand {
    private final EasyPrefix instance;
    private final GroupHandler groupHandler;

    public UserCommand(EasyPrefixCommand parentCommand) {
        this.instance = parentCommand.getInstance();
        this.groupHandler = instance.getGroupHandler();
    }

    @Override
    @NotNull
    public String getName() {
        return "user";
    }

    @Override
    public UserPermission getPermission() {
        return UserPermission.ADMIN;
    }

    @Override
    @Nullable
    public String getDescription() {
        return "shows some important information about the given player and allows a configuration";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "user <player> (<argument>)";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (args.size() <= 1) {
            showHelp(sender);
            return;
        }

        Player player = Bukkit.getPlayer(args.get(1));
        if (player == null) {
            sender.sendMessage(Message.CHAT_PLAYER_NOT_FOUND.getText());
            return;
        }
        User target = instance.getUser(player);

        if (args.size() < 3 || args.get(2).equalsIgnoreCase("info")) {
            showInfo(sender, target);
        } else if (args.get(2).equalsIgnoreCase("reload")) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(EasyPrefix.getInstance().getPlugin(), () -> {
                instance.unloadUser(target.getPlayer());
                instance.getUser(target.getPlayer()).login();
                sender.sendMessage(Message.PREFIX + "User has been reloaded!");
            }, 20L);
        } else if (args.get(2).equalsIgnoreCase("setgroup")) {
            if (args.size() != 4) {
                showHelp(sender);
            } else {
                if (groupHandler.isGroup(args.get(3))) {
                    Group targetGroup = groupHandler.getGroup(args.get(3));
                    target.setGroup(targetGroup, true);
                    sender.sendMessage(Message.PREFIX + "User has been updated!");
                } else {
                    sender.sendMessage(Message.CHAT_GROUP_NOT_FOUND.getText());
                }
            }
        } else if (args.get(2).equalsIgnoreCase("setsubgroup") || args.get(2).equalsIgnoreCase("settag")) {
            if (args.size() != 4) {
                showHelp(sender);
            } else {
                Subgroup targetGroup = groupHandler.getSubgroup(args.get(3));
                if (targetGroup != null) {
                    target.setSubgroup(targetGroup);
                    sender.sendMessage(Message.PREFIX + "User has been updated!");
                } else if (args.get(3).equalsIgnoreCase("none")) {
                    target.setSubgroup(null);
                    sender.sendMessage(Message.PREFIX + "User has been updated!");
                } else {
                    sender.sendMessage(Message.CHAT_GROUP_NOT_FOUND.getText());
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
        String cc = targetUser.getChatColor().getCode();

        sender.sendMessage(" \n§7--------------=== §9§l" + targetUser.getPlayer().getName() + " §7===--------------\n ");
        sender.sendMessage("§9Group§f: §7" + targetUser.getGroup().getName());
        sender.sendMessage("§9Tag§f: §7" + subgroup);
        sender.sendMessage("§9Prefix§f: §8«§7" + targetUser.getPrefix().replace("§", "&") + "§8»" + (targetUser.hasCustomPrefix() ? " §7(§9customized§7)" : ""));
        if (targetUser.hasCustomPrefix()) {
            sender.sendMessage(" §7↳ §9last update§f: §7" + new Timestamp(targetUser.getLastPrefixUpdate()).toString());
        }
        sender.sendMessage("§9Suffix§f: §8«§7" + targetUser.getSuffix().replace("§", "&") + "§8»" + (targetUser.hasCustomSuffix() ? " §7(§9customized§7)" : ""));
        if (targetUser.hasCustomSuffix()) {
            sender.sendMessage(" §7↳ §9last update§f: §7" + new Timestamp(targetUser.getLastSuffixUpdate()).toString());
        }
        if (targetUser.getChatFormatting() != null) cc += targetUser.getChatFormatting().getCode();
        sender.sendMessage("§9Chat color§f: §7" + cc.replace("§", "&"));
        if (targetUser.getGenderType() != null) {
            sender.sendMessage("§9Gender§f: §7" + targetUser.getGenderType().getDisplayName() + "§7/§7" + targetUser.getGenderType().getName());
        }
        sender.sendMessage(" \n§7-----------------------------------------------\n ");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(" \n§7--------------=== §9§lEasyPrefix User §7===--------------\n ");
        String prefix = "§7/§9EasyPrefix user <Player> ";
        sender.sendMessage(prefix + "info §f| §7get information about the player");
        sender.sendMessage(prefix + "setgroup <Group> §f| §7force group to player");
        if (ConfigKeys.USE_TAGS.toBoolean()) {
            sender.sendMessage(prefix + "settag <Tag> §f| §7set tag to player");
        }
        if (ConfigKeys.USE_GENDER.toBoolean()) {
            sender.sendMessage(prefix + "setgender <Gender> §f| §7set gender");
        }
        sender.sendMessage(" \n§7----------------------------------------------------\n ");
    }

    private void setGender(CommandSender sender, User targetUser, String gender) {
        Gender genderType = groupHandler.getGender(gender);
        if (genderType != null) {
            targetUser.setGenderType(genderType);
            sender.sendMessage(Message.PREFIX + "User has been updated!");
        } else {
            sender.sendMessage(Message.PREFIX + "§cThis gender doesn't exist");
        }
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        if (args.size() == 2) {
            return null;
        } else if (args.size() == 3) {
            List<String> matches = Arrays.asList("reload", "info", "setgroup");
            if (ConfigKeys.USE_GENDER.toBoolean()) {
                matches.add("setgender");
            }
            if (ConfigKeys.USE_TAGS.toBoolean()) {
                matches.add("settag");
            }
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
            } else if (args.get(2).equalsIgnoreCase("settag")) {
                for (Subgroup group : this.instance.getGroupHandler().getSubgroups()) {
                    matches.add(group.getName());
                }
            } else if (args.get(2).equalsIgnoreCase("setgender")) {
                for (Gender gender : this.instance.getGroupHandler().getGenderTypes()) {
                    matches.add(gender.getName());
                }
            }
            return matches;
        }
        return Collections.emptyList();
    }

}
