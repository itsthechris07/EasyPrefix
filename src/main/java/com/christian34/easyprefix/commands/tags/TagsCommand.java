package com.christian34.easyprefix.commands.tags;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.EasyCommand;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.responder.gui.pages.GuiSettings;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2020.
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
        subcommands.add(new ListCommand(this, instance));
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
        if (args.size() == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Messages.getPrefix() + Message.PLAYER_ONLY);
                return;
            }

            Player player = (Player) sender;
            User user = instance.getUser(player);
            new GuiSettings(user).openSubgroupsPage(player::closeInventory);
            return;
        }

        String subcommand = args.get(0);
        for (Subcommand subCmd : subcommands) {
            if (subCmd.getName().equalsIgnoreCase(subcommand) || subCmd.getName().startsWith(subcommand)) {
                if (subCmd.getPermission() == null || sender.hasPermission(subCmd.getPermission().toString())) {
                    try {
                        subCmd.handleCommand(sender, args);
                    } catch (Exception ex) {
                        Debug.captureException(ex);
                    }
                } else {
                    sender.sendMessage(Message.NO_PERMS.getMessage());
                }
                return;
            }
        }
        sender.sendMessage(Messages.getPrefix() + "§cCouldn't find requested command! Type '/tags help'"
                + " to get a command overview.");
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return null;
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
