package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.TextUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * EasyPrefix 2026.
 * <p>
 * Replaces the quit message with the one of the group of the player and unloads the user.
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
            e.quitMessage(null);
        } else if (e.quitMessage() != null) {
            String quitMsg = instance.setPlaceholders(user, user.getGroup().getQuitMessage());
            if (quitMsg != null) e.quitMessage(TextUtils.miniMessage().deserialize(TextUtils.escapeLegacyColors(quitMsg)));
        }
        this.instance.unloadUser(e.getPlayer());
    }

}
