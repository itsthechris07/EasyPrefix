package com.christian34.easyprefix.placeholderapi;

import org.bukkit.entity.Player;

public class PlaceholderAPI {
    private static boolean enabled;

    public static String setPlaceholder(Player player, String string) {
        if (isEnabled()) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, string);
        } else return string;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enable) {
        enabled = enable;
        if (enable) CustomPlaceholder.enable();
    }

}