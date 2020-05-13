package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
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
    private final EasyPrefix instance;

    public QuitListener(EasyPrefix instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        ConfigData configData = this.instance.getFileManager().getConfig();
        if (!configData.getBoolean(ConfigData.ConfigKeys.USE_JOIN_QUIT)) {
            this.instance.unloadUser(e.getPlayer());
            return;
        }

        User user = this.instance.getUser(e.getPlayer());
        if (configData.getBoolean(ConfigData.ConfigKeys.HIDE_JOIN_QUIT)) {
            e.setQuitMessage(null);
        } else {
            if (e.getQuitMessage() != null) {
                Group group = user.getGroup();
                String quitMsg = group.getQuitMessage(user);
                e.setQuitMessage(quitMsg);
            }
        }
        if (configData.getBoolean(ConfigData.ConfigKeys.USE_QUIT_SOUND)) {
            String cfg = configData.getString(ConfigData.ConfigKeys.QUIT_SOUND);
            String[] soundOption = cfg.replace(" ", "").split(";");
            try {
                Sound sound = Sound.valueOf(soundOption[0]);
                float volume = Integer.parseInt(soundOption[1]);
                float pitch = Integer.parseInt(soundOption[2]);
                if (soundOption.length == 3) {
                    String receiver = configData.getString(ConfigData.ConfigKeys.JOIN_QUIT_SOUND_RECEIVER);
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
        this.instance.unloadUser(e.getPlayer());
    }


}