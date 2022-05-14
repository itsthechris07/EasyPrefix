package com.christian34.easyprefix.commands.tags;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.EasyCommand;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.UserInterface;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class TagsCommand implements EasyCommand {
    private final EasyPrefix instance;
    private final List<Subcommand> subcommands;

    public TagsCommand(EasyPrefix instance) {
        this.instance = instance;
        this.subcommands = new ArrayList<>();
        subcommands.add(new HelpCommand(this));
        subcommands.add(new ListCommand(instance));
        subcommands.add(new SelectCommand(this, instance));
        subcommands.add(new SetCommand(this, instance));
        subcommands.add(new ClearCommand(this, instance));
    }

    public List<Subcommand> getSubcommands() {
        return subcommands;
    }

    @Override
    @NotNull
    public String getName() {
        return "tags";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Message.PREFIX + Message.CHAT_PLAYER_ONLY);
                return;
            }

            Player player = (Player) sender;
            User user = instance.getUser(player);
            UserInterface gui = new UserInterface(user);
            gui.openUserSubgroupsListPage();
            return;
        }

        String subcommand = args.get(0);
        for (Subcommand subCmd : subcommands) {
            if (subCmd.getName().equalsIgnoreCase(subcommand) || subCmd.getName().startsWith(subcommand)) {
                if (subCmd.getPermission() == null || sender.hasPermission(subCmd.getPermission().toString())) {
                    try {
                        subCmd.handleCommand(sender, args);
                    } catch (Exception ex) {
                        Debug.handleException(ex);
                    }
                } else {
                    sender.sendMessage(Message.CHAT_NO_PERMS.getText());
                }
                return;
            }
        }
        sender.sendMessage(Message.PREFIX + "§cCouldn't find requested command! Type '/tags help'"
                + " to get a command overview.");
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        String subcommand = args.get(0);
        if (args.size() == 1) {
            List<String> matches = new ArrayList<>();
            for (Subcommand subcmd : subcommands) {
                if (subcmd.getDescription() == null) continue;

                if (subcmd.getName().equalsIgnoreCase(subcommand) || subcmd.getName().toLowerCase().startsWith(subcommand.toLowerCase())) {
                    if (subcmd.getPermission() == null || sender.hasPermission(subcmd.getPermission().toString())) {
                        matches.add(subcmd.getName());
                    }
                }
            }
            return matches;
        } else {
            for (Subcommand subcmd : subcommands) {
                if (subcmd.getDescription() == null) continue;

                if (subcmd.getName().equalsIgnoreCase(subcommand) || subcmd.getName().toLowerCase().startsWith(subcommand.toLowerCase())) {
                    if (subcmd.getPermission() == null || sender.hasPermission(subcmd.getPermission().toString())) {
                        return subcmd.getTabCompletion(sender, args);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    public Subcommand getSubcommand(String name) {
        for (Subcommand subCmd : subcommands) {
            if (subCmd.getName().equalsIgnoreCase(name)) {
                return subCmd;
            }
        }
        throw new NullPointerException("Couldn't find subcommand with name '" + name + "'");
    }

}
