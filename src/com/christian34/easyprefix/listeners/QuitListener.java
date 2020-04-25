package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.placeholderapi.PlaceholderAPI;
import com.christian34.easyprefix.user.User;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        User user = User.getUser(e.getPlayer());
        FileConfiguration config = FileManager.getConfig().getFileData();
        if (config.getBoolean(ConfigData.Values.HIDE_JOIN_QUIT.toString()) && !EasyPrefix.getInstance().isUseBungee()) {
            e.setQuitMessage(null);
        } else {
            if (e.getQuitMessage() != null) {
                Group group = user.getGroup();
                String quitMsg = group.getQuitMessage();
                if (PlaceholderAPI.isEnabled()) {
                    quitMsg = PlaceholderAPI.setPlaceholder(user.getPlayer(), quitMsg);
                }
                quitMsg = quitMsg.replace("%player%", user.getPlayer().getDisplayName()).replace("  ", " ");
                e.setQuitMessage(user.setPlaceholder(quitMsg));
            }
        }
        if (config.getBoolean(ConfigData.Values.USE_QUIT_SOUND.toString())) {
            String cfg = config.getString(ConfigData.Values.QUIT_SOUND.toString());
            String[] soundOption = cfg.replace(" ", "").split(";");
            try {
                Sound sound = Sound.valueOf(soundOption[0]);
                float volume = Integer.parseInt(soundOption[1]);
                float pitch = Integer.parseInt(soundOption[2]);
                if (soundOption.length == 3) {
                    String receiver = config.getString(ConfigData.Values.JOIN_QUIT_SOUND_RECEIVER.toString());
                    if (receiver.equals("all")) {
                        for (Player target : Bukkit.getOnlinePlayers()) {
                            target.playSound(target.getLocation(), sound, volume, pitch);
                        }
                    } else {
                        user.getPlayer().playSound(user.getPlayer().getLocation(), sound, volume, pitch);
                    }
                } else {
                    Messages.log("&cCouldn't play sound on player quit. Please check up the sound configuration.");
                }
            } catch(IllegalArgumentException ignored) {
                Messages.log("&cCouldn't play sound '" + soundOption[0] + "'. Please use valid sounds!");
            }
        }
        User.getUsers().remove(e.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit2(PlayerQuitEvent e) {
        if (EasyPrefix.getInstance().isUseBungee()) {
            final String playerName = e.getPlayer().getName();
            final String quitMessage = e.getQuitMessage();
            Bukkit.getScheduler().runTask(EasyPrefix.getInstance(), () -> {
                ByteArrayDataOutput quitListenerOut = ByteStreams.newDataOutput();
                quitListenerOut.writeUTF(playerName);
                quitListenerOut.writeUTF((quitMessage == null) ? "silent" : "all");
                quitListenerOut.writeUTF(quitMessage);
                e.getPlayer().sendPluginMessage(EasyPrefix.getInstance(), "easyprefix:quitlistener", quitListenerOut.toByteArray());
            });
            e.setQuitMessage(null);
        }
    }

}