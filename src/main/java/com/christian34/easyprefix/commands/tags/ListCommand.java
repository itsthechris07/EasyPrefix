package com.christian34.easyprefix.commands.tags;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
class ListCommand implements Subcommand {
    private final EasyPrefix instance;

    public ListCommand(EasyPrefix instance) {
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
                sender.sendMessage(Message.PREFIX + Message.CHAT_PLAYER_ONLY);
                return;
            } else {
                player = (Player) sender;
            }
        } else {
            player = Bukkit.getPlayer(args.get(1));
            if (player == null) {
                sender.sendMessage(Message.PREFIX + Message.CHAT_PLAYER_NOT_FOUND);
            }
        }

        User user = instance.getUser(player);
        List<Subgroup> subgroups = user.getAvailableSubgroups();
        sender.sendMessage(Message.CHAT_TAGS_AVAILABLE.getText()
                .replace("%tags%", Integer.toString(subgroups.size())));

        final String itemTitle = Message.TAGS_ITEM_TITLE.getText();
        final String lore = Message.TAGS_ITEM_LORE.getText();

        TextComponent list = new TextComponent("");
        for (int i = 0; i < subgroups.size(); i++) {
            Subgroup subgroup = subgroups.get(i);
            String name = itemTitle.replace("%name%", subgroup.getName());
            String hoverText = lore;
            String tagPrefix = subgroup.getPrefix(null, false);
            if (tagPrefix == null || tagPrefix.isEmpty()) {
                tagPrefix = "-/-";
            }

            String tagSuffix = subgroup.getSuffix(null, false);
            if (tagSuffix == null || tagSuffix.isEmpty()) {
                tagSuffix = "-/-";
            }

            hoverText = hoverText
                    .replace("%tag_prefix%", tagPrefix)
                    .replace("%tag_suffix%", tagSuffix);

            list.addExtra(getText(name, "/tags select " + subgroup.getName(), hoverText));
            if (i != subgroups.size() - 1) {
                list.addExtra("§7, ");
            }
        }

        user.getPlayer().spigot().sendMessage(list);
    }

    @SuppressWarnings("deprecation")
    private TextComponent getText(String text, String command, String hoverText) {
        TextComponent msg = new TextComponent(TextComponent.fromLegacyText(text));
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText)));
        return msg;
    }

    @Override
    public List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
