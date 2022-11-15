package com.christian34.easyprefix.user;

import org.bukkit.OfflinePlayer;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class OfflineUser extends AbstractUser {
    private OfflinePlayer offlinePlayer;

    public OfflineUser(OfflinePlayer offlinePlayer) {
        super(offlinePlayer.getUniqueId());
    }

    @Override
    public String getDisplayName() {
        return offlinePlayer.getName();
    }

}
