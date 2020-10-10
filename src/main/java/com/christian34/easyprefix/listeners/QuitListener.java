package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.user.User;
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
        if (!ConfigKeys.USE_JOIN_QUIT.toBoolean()) {
            this.instance.unloadUser(e.getPlayer());
            return;
        }

        User user = this.instance.getUser(e.getPlayer());
        if (ConfigKeys.HIDE_JOIN_QUIT.toBoolean()) {
            e.setQuitMessage(null);
        } else {
            if (e.getQuitMessage() != null) {
                Group group = user.getGroup();
                String quitMsg = group.getQuitMessage(user);
                e.setQuitMessage(quitMsg);
            }
        }
        this.instance.unloadUser(e.getPlayer());
    }

}
