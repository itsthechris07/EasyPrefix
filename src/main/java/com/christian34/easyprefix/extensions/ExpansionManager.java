package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class ExpansionManager {
    private final EasyPrefix instance;
    private boolean usingPapi;

    public ExpansionManager(EasyPrefix instance) {
        this.instance = instance;

        if (isEnabled("PlaceholderAPI")) {
            Debug.recordAction("hooking into PlaceholderAPI");
            this.usingPapi = true;
            new CustomPlaceholder(this);
        }

        if (isEnabled("Vault")) {
            Debug.recordAction("hooking into Vault");
            new ChatProvider(this);
        }
    }

    protected EasyPrefix getInstance() {
        return instance;
    }

    public boolean isUsingPapi() {
        return usingPapi;
    }

    @NotNull
    public String setPapi(@NotNull Player player, @NotNull String text) {
        if (!isUsingPapi()) return text;
        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(offlinePlayer, text);
        } catch (Exception ex) {
            Debug.captureException(ex);
            return text;
        }
    }

    public boolean isEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

}
