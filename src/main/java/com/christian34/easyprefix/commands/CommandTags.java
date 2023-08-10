package com.christian34.easyprefix.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TaskManager;
import com.christian34.easyprefix.utils.UserInterface;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class CommandTags {

    private EasyPrefix getInstance() {
        return EasyPrefix.getInstance();
    }

    @CommandMethod("tags")
    public void mainCmd(Player player) {
        User user = getInstance().getUser(player);
        UserInterface gui = new UserInterface(user);
        TaskManager.run(gui::openUserSubgroupsListPage);
    }

    @CommandMethod("tags set <user> <subgroup>")
    @CommandPermission("easyprefix.admin")
    public void setTag(CommandSender sender, @Argument("user") User user, @Argument("subgroup") Subgroup subgroup) {
        user.setSubgroup(subgroup);
        sender.sendMessage(Message.TAG_SET_TO_PLAYER.getText()
                .replace("%tag%", subgroup.getName())
                .replace("%player%", user.getPlayer().getName()));
    }

    @CommandMethod("tags clear <user>")
    @CommandPermission("easyprefix.admin")
    public void clearTag(CommandSender sender, @Argument("user") User user) {
        user.setSubgroup(null);
        sender.sendMessage(Message.TAGS_CLEARED_FOR_PLAYER.get("player", user.getPlayer().getName()));
    }

    @CommandMethod("tags select <subgroup>")
    @CommandPermission("easyprefix.tags.switch")
    public void selectTag(Player player, @Argument("subgroup") Subgroup subgroup) {
        User user = this.getInstance().getUser(player);
        if (!user.hasPermission("tag." + subgroup.getName().toLowerCase())) {
            user.getPlayer().sendMessage(Message.CHAT_NO_PERMS.getText());
            return;
        }
        user.setSubgroup(subgroup);
        user.getPlayer().sendMessage(Message.TAGS_PLAYER_SELECT.get("tag", subgroup.getName()));
    }

    @CommandMethod("tags list")
    public void listTags(Player player) {
        User user = this.getInstance().getUser(player);
        List<Subgroup> subgroups = user.getAvailableSubgroups();

        Message text = Message.CHAT_TAGS_AVAILABLE;
        user.getPlayer().sendMessage(text.get("tags", Integer.toString(subgroups.size())).replace("%player%", user.getName()));

        final String itemTitle = Message.TAGS_ITEM_TITLE.getText();
        final String lore = Message.TAGS_ITEM_LORE.getText();

        TextComponent list = new TextComponent("");
        for (int i = 0; i < subgroups.size(); i++) {
            Subgroup subgroup = subgroups.get(i);
            String name = itemTitle.replace("%name%", subgroup.getName());
            String hoverText = lore;
            String tagPrefix = subgroup.getPrefix();
            if (tagPrefix == null || tagPrefix.isEmpty()) {
                tagPrefix = "-/-";
            }

            String tagSuffix = subgroup.getSuffix();
            if (tagSuffix == null || tagSuffix.isEmpty()) {
                tagSuffix = "-/-";
            }
            hoverText = hoverText.replace("%tag_prefix%", tagPrefix).replace("%tag_suffix%", tagSuffix);
            list.addExtra(getText(name, "/tags select " + subgroup.getName(), hoverText));
            if (i != subgroups.size() - 1) {
                list.addExtra("§7, ");
            }
        }

        user.getPlayer().spigot().sendMessage(list);
    }

    //TODO use api for this!
    @SuppressWarnings("deprecation")
    private TextComponent getText(String text, String command, String hoverText) {
        TextComponent msg = new TextComponent(TextComponent.fromLegacyText(text));
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText)));
        return msg;
    }

}
