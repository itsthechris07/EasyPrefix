package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TaskManager;
import com.christian34.easyprefix.utils.TextUtils;
import com.christian34.easyprefix.utils.UserInterface;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.List;

/**
 * EasyPrefix 2026.
 * <p>
 * The /tags command: lets players select their tag and admins set or clear the tags of players.
 *
 * @author Christian34
 */
public class CommandTags {

    private EasyPrefix getInstance() {
        return EasyPrefix.getInstance();
    }

    @Command("tags")
    public void mainCmd(Player player) {
        User user = getInstance().getUser(player);
        UserInterface gui = new UserInterface(user);
        TaskManager.run(player, gui::openUserSubgroupsListPage);
    }

    @Command("tags set <user> <subgroup>")
    @Permission("easyprefix.admin")
    public void setTag(CommandSender sender, @Argument("user") User user, @Argument("subgroup") Subgroup subgroup) {
        user.setSubgroup(subgroup);
        sender.sendMessage(Message.TAG_SET_TO_PLAYER.getText()
                .replace("%tag%", subgroup.getName())
                .replace("%player%", user.getName()));
    }

    @Command("tags clear <user>")
    @Permission("easyprefix.admin")
    public void clearTag(CommandSender sender, @Argument("user") User user) {
        user.setSubgroup(null);
        sender.sendMessage(Message.TAGS_CLEARED_FOR_PLAYER.get("player", user.getName()));
    }

    @Command("tags select <subgroup>")
    @Permission("easyprefix.tags.switch")
    public void selectTag(Player player, @Argument("subgroup") Subgroup subgroup) {
        User user = this.getInstance().getUser(player);
        if (!user.hasPermission("tag." + subgroup.getName().toLowerCase())) {
            user.getPlayer().sendMessage(Message.CHAT_NO_PERMS.getText());
            return;
        }
        user.setSubgroup(subgroup);
        user.getPlayer().sendMessage(Message.TAGS_PLAYER_SELECT.get("tag", subgroup.getName()));
    }

    @Command("tags list")
    public void listTags(Player player) {
        User user = this.getInstance().getUser(player);
        List<Subgroup> subgroups = user.getAvailableSubgroups();

        Message text = Message.CHAT_TAGS_AVAILABLE;
        user.getPlayer().sendMessage(text.get("tags", Integer.toString(subgroups.size())).replace("%player%", user.getName()));

        final String itemTitle = Message.TAGS_ITEM_TITLE.getText();
        final String lore = Message.TAGS_ITEM_LORE.getText();

        TextComponent.Builder list = Component.text();
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
            hoverText = hoverText.replace("%tag_prefix%", colored(tagPrefix)).replace("%tag_suffix%", colored(tagSuffix));
            list.append(getText(name, "/tags select " + subgroup.getName(), hoverText));
            if (i != subgroups.size() - 1) {
                list.append(Component.text(", ", NamedTextColor.GRAY));
            }
        }

        user.getPlayer().sendMessage(list.build());
    }

    /**
     * tags are stored as MiniMessage, the texts of messages.yml use legacy colors
     */
    private static String colored(String text) {
        return TextUtils.colorize(TextUtils.escapeLegacyColors(text));
    }

    private Component getText(String text, String command, String hoverText) {
        LegacyComponentSerializer legacy = TextUtils.getLegacySerializer();
        return legacy.deserialize(text)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(legacy.deserialize(hoverText)));
    }

}
