package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.user.UserPermission;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
class GroupCommand implements Subcommand {
    private final EasyPrefixCommand parentCommand;
    private final GroupHandler groupHandler;

    public GroupCommand(EasyPrefixCommand parentCommand) {
        this.parentCommand = parentCommand;
        EasyPrefix instance = parentCommand.getInstance();
        this.groupHandler = instance.getGroupHandler();
    }

    @Override
    @NotNull
    public String getName() {
        return "group";
    }

    @Override
    public UserPermission getPermission() {
        return UserPermission.ADMIN;
    }

    @Override
    public String getDescription() {
        return "allows to modify groups";
    }

    @Override
    public String getCommandUsage() {
        return "group <group> <argument>";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            parentCommand.getSubcommand("help").handleCommand(sender, null);
            return;
        }

        Group group = groupHandler.isGroup(args.get(1)) ? groupHandler.getGroup(args.get(1)) : null;
        if (group == null) {
            sender.sendMessage(Message.GROUP_NOT_FOUND.toMessage());
            return;
        }

        if (args.size() < 3) {
            showHelp(sender);
            return;
        }

        if (args.get(2).equalsIgnoreCase("info")) {
            sender.sendMessage(" \n§7--------------=== §5§l" + group.getName() + " §7===--------------\n ");
            sender.sendMessage("§5Prefix§f: §8«§7" + group.getPrefix(null, false) + "§8»");
            sender.sendMessage("§5Suffix§f: §8«§7" + group.getSuffix(null, false) + "§8»");
            String cc = group.getChatColor().getCode();
            if (group.getChatFormatting() != null) cc = cc + group.getChatFormatting().getCode();
            sender.sendMessage("§5Chat color§f: §7" + cc.replace("§", "&"));
            sender.sendMessage("§5Join message§f: §7" + group.getJoinMessageText());
            sender.sendMessage("§5Quit message§f: §7" + group.getQuitMessageText());
            sender.sendMessage(" \n§7-----------------------------------------------\n ");
        } else {
            showHelp(sender);
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(" \n§7--------------=== §5§lEasyPrefix Group §7===--------------\n ");
        sender.sendMessage("§7/§5EasyPrefix group <Group> info §f| §7get information about the group");
        sender.sendMessage(" \n§7----------------------------------------------------\n ");
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        List<String> matches;
        if (args.size() == 2) {
            matches = new ArrayList<>();
            for (Group group : groupHandler.getGroups()) {
                matches.add(group.getName());
            }
            return matches;
        } else if (args.size() == 3) {
            matches = Collections.singletonList("info");
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
        }
        return Collections.emptyList();
    }
}
