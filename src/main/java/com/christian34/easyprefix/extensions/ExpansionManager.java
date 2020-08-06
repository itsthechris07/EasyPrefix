package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class ExpansionManager {
    private final EasyPrefix instance;
    private ChatProvider chatProvider;
    private boolean usingPapi;

    public ExpansionManager(EasyPrefix instance) {
        this.instance = instance;
        if (isEnabled("PlaceholderAPI")) {
            Debug.recordAction("Hooking into PlaceholderAPI...");
            this.usingPapi = true;
            new CustomPlaceholder(this);
        }
        if (isEnabled("Vault")) {
            Debug.recordAction("Hooking into Vault...");
            this.chatProvider = new ChatProvider(this);
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
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(player.getUniqueId()), text);
        } catch (Exception ex) {
            Debug.captureException(ex);
            return text;
        }
    }

    public boolean isEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

}
