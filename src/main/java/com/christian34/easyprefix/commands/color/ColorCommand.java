package com.christian34.easyprefix.commands.color;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.EasyCommand;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.gui.pages.GuiSettings;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class ColorCommand implements EasyCommand {
    private final EasyPrefix instance;
    private final List<Subcommand> subcommands;

    public ColorCommand(EasyPrefix instance) {
        this.instance = instance;
        this.subcommands = new ArrayList<>();
        if (ConfigKeys.HANDLE_COLORS.toBoolean()) {
            this.subcommands.add(new SetCommand(this, instance));
            this.subcommands.add(new SelectCommand(this, instance));
            this.subcommands.add(new HelpCommand(this));
        }
    }

    @Override
    @NotNull
    public String getName() {
        return "color";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (!ConfigKeys.HANDLE_COLORS.toBoolean()) {
            Debug.warn("&cYou can't use the command 'color' as it has been disabled in config.yml! " +
                    "Set 'handle-colors' to 'true' and restart the server.");
            EasyCommand command = instance.getCommandHandler().getCommand("easyprefix");
            command.handleCommand(sender, Collections.singletonList("help"));
            return;
        }

        if (args.isEmpty()) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Message.PREFIX + Message.CHAT_PLAYER_ONLY);
                return;
            }

            Player player = (Player) sender;
            User user = instance.getUser(player);
            new GuiSettings(user).openColorsPage(player::closeInventory);
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

        if (sender instanceof Player) {
            String name = args.get(0);
            if (args.size() == 2) {
                name += " " + args.get(1);
            }
            getSubcommand("select").handleCommand(sender, Arrays.asList("select", name));
        } else {
            getSubcommand("help").handleCommand(sender, Collections.emptyList());
        }
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
            ArrayList<String> list = new ArrayList<>();
            list.add("select");
            list.addAll(args);
            matches.addAll(getSubcommand("select").getTabCompletion(sender, list));
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
            if (subCmd.getName().equals(name)) {
                return subCmd;
            }
        }
        throw new NullPointerException("Couldn't find subcommand with name '" + name + "'");
    }

    public List<Subcommand> getSubcommands() {
        return subcommands;
    }

}
