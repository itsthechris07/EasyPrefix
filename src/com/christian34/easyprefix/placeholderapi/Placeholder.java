package com.christian34.easyprefix.placeholderapi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Placeholder {
    private static final boolean enabled;

    static {
        enabled = Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (enabled) {
            me.clip.placeholderapi.PlaceholderAPI.registerExpansion(new CustomPlaceholder());
        }
    }

    public static String setPlaceholder(Player player, String string) {
        if (isEnabled()) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, string);
        } else return string;
    }

    public static boolean isEnabled() {
        return enabled;
    }

}
