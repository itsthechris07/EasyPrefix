package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import org.bukkit.Bukkit;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class VersionController {
    private static final String pluginVersion;
    private static final int minorVersion;

    static {
        String version = Bukkit.getServer().getBukkitVersion();
        String serverVersion = version.substring(0, 6);
        minorVersion = Integer.parseInt(serverVersion.substring(2, 4).replace(".", ""));
        pluginVersion = EasyPrefix.getInstance().getPlugin().getDescription().getVersion();
    }

    public static String getPluginVersion() {
        return pluginVersion;
    }

    public static int getMinorVersion() {
        return minorVersion;
    }

}