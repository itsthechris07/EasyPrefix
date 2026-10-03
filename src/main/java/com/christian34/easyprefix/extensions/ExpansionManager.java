package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * EasyPrefix 2026.
 * <p>
 * Hooks into PlaceholderAPI and Vault (also the new api of the fork VaultUnlocked), if they are installed.
 *
 * @author Christian34
 */
public class ExpansionManager {
    private final EasyPrefix instance;
    /**
     * resolves placeholders of other plugins, null if PlaceholderAPI is not installed
     */
    private BiFunction<OfflinePlayer, String, String> placeholderResolver;
    private boolean usingVaultUnlocked;

    public ExpansionManager(EasyPrefix instance) {
        this.instance = instance;

        // a broken hook (e.g. an incompatible version of the other plugin) must not stop EasyPrefix
        if (isEnabled("PlaceholderAPI")) {
            try {
                Debug.recordAction("hooking into PlaceholderAPI");
                new CustomPlaceholder(this).register();
                this.placeholderResolver = me.clip.placeholderapi.PlaceholderAPI::setPlaceholders;
            } catch (Exception | LinkageError ex) {
                Debug.warn("Couldn't hook into PlaceholderAPI: " + ex);
            }
        }

        if (isEnabled("Vault")) {
            try {
                Debug.recordAction("hooking into Vault");
                new ChatProvider(this);
            } catch (Exception | LinkageError ex) {
                Debug.warn("Couldn't hook into Vault: " + ex);
            }
            if (ChatUnlockedProvider.isAvailable()) {
                try {
                    Debug.recordAction("hooking into VaultUnlocked");
                    new ChatUnlockedProvider(this);
                    this.usingVaultUnlocked = true;
                } catch (Exception | LinkageError ex) {
                    Debug.warn("Couldn't hook into VaultUnlocked: " + ex);
                }
            }
        }
    }

    protected EasyPrefix getInstance() {
        return instance;
    }

    /**
     * @return true if the chat provider for the new api of VaultUnlocked is registered
     */
    public boolean isUsingVaultUnlocked() {
        return usingVaultUnlocked;
    }

    public boolean isUsingPapi() {
        return placeholderResolver != null;
    }

    /**
     * replaces the placeholders of other plugins (PlaceholderAPI) in the text
     */
    @NotNull
    public String setPlaceholders(@NotNull OfflinePlayer player, @NotNull String text) {
        if (!isUsingPapi()) return text;
        try {
            return placeholderResolver.apply(player, text);
        } catch (Exception ex) {
            Debug.handleException(ex);
            return text;
        }
    }

    /**
     * replaces the way placeholders of other plugins are resolved - PlaceholderAPI can't run in the tests
     */
    void setPlaceholderResolver(@Nullable BiFunction<OfflinePlayer, String, String> placeholderResolver) {
        this.placeholderResolver = placeholderResolver;
    }

    public boolean isEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    /**
     * @return the user of an online or offline player, null if the player is unknown
     */
    @Nullable
    User getUser(@Nullable OfflinePlayer player) {
        if (player == null) return null;
        Player online = player.getPlayer();
        if (online != null) return instance.getUser(online);
        if (!player.hasPlayedBefore()) return null;
        return instance.getUser(player);
    }

    /**
     * @return the user of a player by name (vault still uses names), null if the player is unknown
     */
    @Nullable
    User getUser(@Nullable String name) {
        if (name == null) return null;
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return instance.getUser(online);
        return getUser(Bukkit.getOfflinePlayerIfCached(name));
    }

    /**
     * Prefixes are stored as MiniMessage and may contain placeholders - other plugins expect legacy colors (§).
     *
     * @return the text with all placeholders replaced and legacy colors, "" for null
     */
    @NotNull
    String toLegacy(@NotNull User user, @Nullable String text) {
        if (text == null || text.isEmpty()) return "";
        String resolved = instance.setPlaceholders(user, text);
        if (resolved == null) return "";
        return TextUtils.colorizeOpenEnd(TextUtils.escapeLegacyColors(resolved));
    }

}
