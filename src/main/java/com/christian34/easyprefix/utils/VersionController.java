package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;

/**
 * EasyPrefix 2026.
 * <p>
 * The version of the plugin.
 *
 * @author Christian34
 */
public final class VersionController {
    private static final String pluginVersion = EasyPrefix.getInstance().getPluginMeta().getVersion();

    public static String getPluginVersion() {
        return pluginVersion;
    }

}
