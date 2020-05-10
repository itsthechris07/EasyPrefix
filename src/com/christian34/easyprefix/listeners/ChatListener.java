package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.placeholderapi.PlaceholderAPI;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.RainbowEffect;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        User user = EasyPrefix.getInstance().getUser(e.getPlayer());
        String prefix = user.getPrefix();
        String suffix = user.getSuffix();
        String msg = e.getMessage();
        String chatColor = "";

        if (PlaceholderAPI.isEnabled()) {
            prefix = PlaceholderAPI.setPlaceholder(user.getPlayer(), prefix);
            suffix = PlaceholderAPI.setPlaceholder(user.getPlayer(), suffix);
        }

        if (FileManager.getConfig().getBoolean(ConfigData.ConfigKeys.HANDLE_COLORS)) {
            ChatFormatting chatFormatting = user.getChatFormatting();
            chatColor = user.getChatColor().getCode();
            if (chatFormatting != null) {
                if (chatFormatting.equals(ChatFormatting.RAINBOW)) {
                    msg = RainbowEffect.addRainbowEffect(msg);
                    chatColor = "";
                } else {
                    chatColor += chatFormatting.getCode();
                }
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

        if (EasyPrefix.getInstance().formatChat()) {
            String format = prefix + user.getPlayer().getDisplayName() + suffix + " " + chatColor + e.getMessage();
            if (!FileManager.getConfig().getBoolean(ConfigData.ConfigKeys.DUPLICATE_WHITE_SPACES)) {
                format = format.replaceAll("\\s+", " ");
            }
            e.setFormat(format.replace("%", "%%"));
        }
    }

}