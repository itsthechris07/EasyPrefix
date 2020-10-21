package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.extensions.ExpansionManager;
import com.christian34.easyprefix.files.ConfigKeys;
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
    private final EasyPrefix instance;

    public ChatListener(EasyPrefix instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!this.instance.formatChat()) return;
        User user = instance.getUser(e.getPlayer());
        String prefix = user.getPrefix();
        String suffix = user.getSuffix();
        String msg = e.getMessage();
        String chatColor = "";

        if (instance.getExpansionManager().isUsingPapi()) {
            ExpansionManager manager = instance.getExpansionManager();
            prefix = manager.setPapi(user.getPlayer(), prefix);
            suffix = manager.setPapi(user.getPlayer(), suffix);
        }

        if (ConfigKeys.HANDLE_COLORS.toBoolean()) {
            ChatFormatting chatFormatting = user.getChatFormatting();
            chatColor = user.getChatColor().getCode();
            if (chatFormatting != null && !chatFormatting.equals(ChatFormatting.UNDEFINED)) {
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
        String format = prefix + user.getPlayer().getDisplayName() + suffix + " " + chatColor + e.getMessage();
        e.setFormat(format.replace("%", "%%"));
    }

}