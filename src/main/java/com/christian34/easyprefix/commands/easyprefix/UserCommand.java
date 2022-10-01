package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.*;

/**
 * EasyPrefix 2022.
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

        User target;

        Player player = Bukkit.getPlayer(args.get(1));
        if (player != null) {
            target = instance.getUser(player);
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args.get(1));
            target = instance.getUser(offlinePlayer);
            if (!offlinePlayer.hasPlayedBefore() || target == null) {
                sender.sendMessage(Message.CHAT_PLAYER_NOT_FOUND.getText());
                return;
            }
        }


        if (args.size() < 3 || args.get(2).equalsIgnoreCase("info")) {
            showInfo(sender, target);
        } else if (args.get(2).equalsIgnoreCase("reload")) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(EasyPrefix.getInstance().getPlugin(), () -> {
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
        }
    }

    private void showInfo(CommandSender sender, User user) {
        String subgroup = (user.getSubgroup() != null) ? user.getSubgroup().getName() : "-";
        String chatColor = user.getChatColor().getCode();
        ChatFormatting chatFormatting = user.getChatFormatting();
        String formatting = "";
        if (chatFormatting != null) {
            if (chatFormatting.equals(ChatFormatting.RAINBOW)) {
                formatting = "rainbow";
            } else {
                formatting = chatFormatting.getCode();
            }
        }

        sender.sendMessage(" \n§7--------------=== §9§l" + user.getName() + " §7===--------------");
        sender.sendMessage("§9Group§f: §7" + user.getGroup().getName());
        sender.sendMessage("§9Tag§f: §7" + subgroup);
        sender.sendMessage("§9Prefix§f: §8«§7" + Optional.ofNullable(user.getPrefix()).orElse("-") + "§8»"
                + (user.hasCustomPrefix() ? " §7(§9customized§7)"
                + "\n  §7↳ §9last update§f: §7" + new Timestamp(user.getLastPrefixUpdate()) : ""));
        sender.sendMessage("§9Suffix§f: §8«§7" + Optional.ofNullable(user.getSuffix()).orElse("-") + "§8»"
                + (user.hasCustomSuffix() ? " §7(§9customized§7)"
                + "\n  §7↳ §9last update§f: §7" + new Timestamp(user.getLastSuffixUpdate()) : ""));
        if (chatFormatting == null || !chatFormatting.equals(ChatFormatting.RAINBOW)) {
            sender.sendMessage(" §9chat color§f: §7" + chatColor.replace("§", "&"));
        }
        if (chatFormatting != null) {
            sender.sendMessage(" §9chat formatting§f: §7" + formatting.replace("§", "&"));
        }
        sender.sendMessage(" \n§7-----------------------------------------------\n ");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(" \n§7--------------=== §9§lEasyPrefix User §7===--------------\n ");
        String prefix = "§7/§9EasyPrefix user <Player> ";
        sender.sendMessage(prefix + "info §f| §7get information about the player");
        sender.sendMessage(prefix + "setgroup <Group> §f| §7force group to player");
        if (instance.getConfigData().getBoolean(ConfigData.Keys.USE_TAGS)) {
            sender.sendMessage(prefix + "settag <Tag> §f| §7set tag to player");
        }
        sender.sendMessage(" \n§7----------------------------------------------------\n ");
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        if (args.size() == 2) {
            List<String> names = new ArrayList<>();
            for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                names.add(op.getName());
            }
            return names;
        } else if (args.size() == 3) {
            List<String> matches = new ArrayList<>(Arrays.asList("reload", "info", "setgroup"));
            if (instance.getConfigData().getBoolean(ConfigData.Keys.USE_TAGS)) {
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
            }
            return matches;
        }
        return Collections.emptyList();
    }

}
