package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class JoinListener implements Listener {
    private final EasyPrefix instance;

    public JoinListener(EasyPrefix instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        if (!instance.getConfigData().getBoolean(ConfigData.Keys.USE_JOIN_QUIT)) return;
        User user = instance.getUser(e.getPlayer());

        if (instance.getConfigData().getBoolean(ConfigData.Keys.HIDE_JOIN_QUIT)) {
            e.setJoinMessage(null);
        } else if (e.getJoinMessage() != null) {
            String joinMsg = instance.setPlaceholders(user, user.getGroup().getJoinMessage());
            e.setJoinMessage(Message.setColors(joinMsg));
        }
    }

}
