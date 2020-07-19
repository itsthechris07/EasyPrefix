package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.EasyPrefix;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class ExpansionManager {
    private final EasyPrefix instance;
    private CustomPlaceholder customPlaceholder;
    private ChatProvider chatProvider;
    private boolean usingPapi;

    public ExpansionManager(EasyPrefix instance) {
        this.instance = instance;
        if (isEnabled("PlaceholderAPI")) {
            this.usingPapi = true;
            this.customPlaceholder = new CustomPlaceholder(this);
        }
        if (isEnabled("Vault")) {
            this.chatProvider = new ChatProvider(this);
        }
    }

    protected EasyPrefix getInstance() {
        return instance;
    }

    public boolean isUsingPapi() {
        return usingPapi;
    }

    public String setPapi(Player player, String text) {
        try {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        } catch (Exception ignored) {
            return text;
        }
    }

    public boolean isEnabled(String plugin) {
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }


}
