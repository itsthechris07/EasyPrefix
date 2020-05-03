package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class QuitListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        User user = EasyPrefix.getInstance().getUser(e.getPlayer());
        ConfigData configData = FileManager.getConfig();
        if (configData.getBoolean(ConfigData.Values.HIDE_JOIN_QUIT)) {
            e.setQuitMessage(null);
        } else {
            if (e.getQuitMessage() != null) {
                Group group = user.getGroup();
                String quitMsg = group.getQuitMessage(user);
                e.setQuitMessage(quitMsg);
            }
        }
        if (configData.getBoolean(ConfigData.Values.USE_QUIT_SOUND)) {
            String cfg = configData.getString(ConfigData.Values.QUIT_SOUND);
            String[] soundOption = cfg.replace(" ", "").split(";");
            try {
                Sound sound = Sound.valueOf(soundOption[0]);
                float volume = Integer.parseInt(soundOption[1]);
                float pitch = Integer.parseInt(soundOption[2]);
                if (soundOption.length == 3) {
                    String receiver = configData.getString(ConfigData.Values.JOIN_QUIT_SOUND_RECEIVER);
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
        EasyPrefix.getInstance().unloadUser(e.getPlayer());
    }

}