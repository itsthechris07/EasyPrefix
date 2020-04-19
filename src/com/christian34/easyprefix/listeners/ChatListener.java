package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.bungeecord.MessageSender;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.placeholderapi.PlaceholderAPI;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.RainbowEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) return;
        User user = User.getUser(e.getPlayer());
        String prefix = user.getPrefix();

        String suffix = user.getSuffix();
        String msg = e.getMessage();
        String chatColor = "";

        if (PlaceholderAPI.isEnabled()) {
            prefix = PlaceholderAPI.setPlaceholder(user.getPlayer(), prefix);
            suffix = PlaceholderAPI.setPlaceholder(user.getPlayer(), suffix);
        }

        if (user.getChatColor() != null || user.getChatFormatting() != null) {
            if (user.getChatFormatting() != null && user.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                msg = RainbowEffect.addRainbowEffect(msg);
            } else {
                chatColor = user.getChatColor().getCode();
                if (user.getChatFormatting() != null) chatColor += user.getChatFormatting().getCode();
            }
        } else {
            if (user.getGroup().getChatFormatting() != null && user.getGroup().getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                msg = RainbowEffect.addRainbowEffect(msg);
            } else {
                chatColor = user.getGroup().getChatColor().getCode();
                if (user.getGroup().getChatFormatting() != null)
                    chatColor += user.getGroup().getChatFormatting().getCode();
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

        e.setMessage(msg);
        String format = prefix + user.getPlayer().getDisplayName() + suffix + " " + chatColor + e.getMessage();
        if (!FileManager.getConfig().getFileData().getBoolean(ConfigData.Values.DUPLICATE_WHITE_SPACES.toString())) {
            format = format.replaceAll("\\s+", " ");
        }
        e.setFormat(format.replace("%", "%%"));
        if (EasyPrefix.getInstance().getDiscordSRVHoster() != null) {
            EasyPrefix.getInstance().getDiscordSRVHoster().sendChat(e.getPlayer(), msg);
        }
        if (EasyPrefix.getInstance().isUseBungee()) {
            ArrayList<Player> blockedPlayers = new ArrayList<>();
            if (e.getRecipients().size() != Bukkit.getOnlinePlayers().size()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!e.getRecipients().contains(player)) {
                        blockedPlayers.add(player);
                    }
                }
            }
            MessageSender messageSender = new MessageSender();
            messageSender.sendChat(user.getPlayer(), blockedPlayers, e.getFormat());
            e.setCancelled(true);
        }
    }

}