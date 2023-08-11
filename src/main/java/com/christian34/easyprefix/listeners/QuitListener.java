package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * EasyPrefix 2023.
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
        if (!instance.getConfigData().getBoolean(ConfigData.Keys.USE_JOIN_QUIT)) {
            this.instance.unloadUser(e.getPlayer());
            return;
        }

        User user = this.instance.getUser(e.getPlayer());
        if (instance.getConfigData().getBoolean(ConfigData.Keys.HIDE_JOIN_QUIT)) {
            e.setQuitMessage(null);
        } else {
            if (e.getQuitMessage() != null) {
                String quitMsg = instance.setPlaceholders(user, user.getGroup().getQuitMessage());
                e.setQuitMessage(Message.setColors(quitMsg));
            }
        }
        this.instance.unloadUser(e.getPlayer());
    }

}
