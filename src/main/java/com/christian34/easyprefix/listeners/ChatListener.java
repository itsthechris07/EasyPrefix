package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class ChatListener implements Listener {
    private final EasyPrefix instance;

    public ChatListener(EasyPrefix instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!this.instance.formatChat()) return;
        User user = instance.getUser(e.getPlayer());

        String prefix = Optional.ofNullable(Message.setColors(instance.setPlaceholders(user, user.getPrefix()))).orElse("");
        String suffix = Optional.ofNullable(Message.setColors(instance.setPlaceholders(user, user.getSuffix()))).orElse("");

        String msg = e.getMessage();
        String chatColor = "";


        if (instance.getConfigData().getBoolean(ConfigData.Keys.HANDLE_COLORS)) {
            ChatFormatting chatFormatting = user.getChatFormatting();
            chatColor = user.getChatColor().getCode();
            if (chatFormatting != null && !chatFormatting.equals(ChatFormatting.UNDEFINED)) {
                chatColor += chatFormatting.getCode();
            }

            if (user.getPlayer().hasPermission("EasyPrefix.Color.all")) {
                msg = ChatColor.translateAlternateColorCodes('&', msg);
            } else {
                for (Color c : user.getColors()) {
                    msg = msg.replace(c.getCode().replace("§", "&"), c.getCode());
                }
                for (ChatFormatting formatting : user.getChatFormattings()) {
                    msg = msg.replace(formatting.getCode().replace("§", "&"), formatting.getCode());
                }
            }
        }

        e.setMessage(msg);
        String format = prefix + user.getPlayer().getDisplayName() + suffix + " " + chatColor + e.getMessage();
        e.setFormat(format.replace("%", "%%"));
    }

}
