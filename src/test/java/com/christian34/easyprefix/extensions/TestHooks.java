package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.EasyPrefix;
import org.bukkit.OfflinePlayer;

import java.util.function.BiFunction;

/**
 * EasyPrefix 2026.
 * <p>
 * Access to package-private test hooks of the extensions for tests in other packages.
 *
 * @author Christian34
 */
public final class TestHooks {

    private TestHooks() {
    }

    /**
     * resolves placeholders of other plugins like PlaceholderAPI would
     */
    public static void setPlaceholders(EasyPrefix plugin, BiFunction<OfflinePlayer, String, String> resolver) {
        plugin.getExpansionManager().setPlaceholderResolver(resolver);
    }

}
