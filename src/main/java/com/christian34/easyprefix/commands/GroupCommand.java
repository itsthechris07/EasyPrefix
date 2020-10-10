package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GroupCommand implements Subcommand {
    private final CommandHandler commandHandler;
    private final GroupHandler groupHandler;

    public GroupCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
        EasyPrefix instance = commandHandler.getInstance();
        this.groupHandler = instance.getGroupHandler();
    }

    @Override
    public String getName() {
        return "group";
    }

    @Override
    public String getPermission() {
        return "admin";
    }

    @Override
    public void handleCommand(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            commandHandler.getSubcommand("help").handleCommand(sender, null);
            return;
        }

        Group group = groupHandler.isGroup(args.get(1)) ? groupHandler.getGroup(args.get(1)) : null;
        if (group == null) {
            sender.sendMessage(Messages.getMessage(Message.GROUP_NOT_FOUND));
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
            String cc = (group.getChatColor() != null) ? group.getChatColor().getCode() : "-";
            if (group.getChatFormatting() != null) cc = cc + group.getChatFormatting().getCode();
            sender.sendMessage("§5Chatcolor§f: §7" + cc.replace("§", "&"));
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
    public List<String> getTabCompletion(CommandSender sender, List<String> args) {
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
