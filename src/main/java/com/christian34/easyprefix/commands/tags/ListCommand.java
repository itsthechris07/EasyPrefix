package com.christian34.easyprefix.commands.tags;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
class ListCommand implements Subcommand {
    private final TagsCommand parentCommand;
    private final EasyPrefix instance;

    public ListCommand(TagsCommand parentCommand, EasyPrefix instance) {
        this.parentCommand = parentCommand;
        this.instance = instance;
    }

    @Override
    public UserPermission getPermission() {
        return null;
    }

    @Override
    @NotNull
    public String getDescription() {
        return "shows the tags of you or the specified player";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "list <player>";
    }

    @Override
    @NotNull
    public String getName() {
        return "list";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        Player player;
        if (args.size() < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Messages.getPrefix() + Message.PLAYER_ONLY);
                return;
            } else {
                player = (Player) sender;
            }
        } else {
            player = Bukkit.getPlayer(args.get(1));
            if (player == null) {
                sender.sendMessage(Messages.getPrefix() + Message.PLAYER_NOT_FOUND);
            }
        }

        User user = instance.getUser(player);
        List<Subgroup> subgroups = user.getAvailableSubgroups();
        sender.sendMessage(Message.CHAT_TAGS_AVAILABLE.toString()
                .replace("%prefix%", Messages.getPrefix())
                .replace("%tags%", subgroups.size() + "")
                .replace("%newline%", "\n"));

        TextComponent list = new TextComponent("");
        for (Subgroup subgroup : subgroups) {
            list.addExtra(getText(subgroup.getName(), "/ep bla", subgroup.getName()));
            list.addExtra(", ");
        }
        user.getPlayer().spigot().sendMessage(getText("text", "/ep test", "hover text"));
    }

    private TextComponent getText(String text, String command, String hoverText) {
        TextComponent msg = new TextComponent(TextComponent.fromLegacyText(text));
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        msg.addExtra(msg);
        return msg;
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return null;
    }

}
