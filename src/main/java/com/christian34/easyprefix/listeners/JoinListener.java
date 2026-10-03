package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.TextUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * EasyPrefix 2026.
 * <p>
 * Loads the user while logging in and replaces the join message with the one of the group of the player.
 *
 * @author Christian34
 */
public class JoinListener implements Listener {
    private final EasyPrefix instance;

    public JoinListener(EasyPrefix instance) {
        this.instance = instance;
    }

    /**
     * loads the user while logging in (not on the main thread), a slow or unreachable database would freeze the
     * server on the join otherwise
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        try {
            instance.preloadUser(e.getUniqueId(), e.getName());
        } catch (Exception ex) {
            // the user is loaded on the join then
            Debug.handleException(ex);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        if (!instance.getConfigData().getBoolean(ConfigData.Keys.USE_JOIN_QUIT)) return;
        User user = instance.getUser(e.getPlayer());

        if (instance.getConfigData().getBoolean(ConfigData.Keys.HIDE_JOIN_QUIT)) {
            e.joinMessage(null);
        } else if (e.joinMessage() != null) {
            String joinMsg = instance.setPlaceholders(user, user.getGroup().getJoinMessage());
            if (joinMsg != null) e.joinMessage(TextUtils.miniMessage().deserialize(TextUtils.escapeLegacyColors(joinMsg)));
        }
    }

}
