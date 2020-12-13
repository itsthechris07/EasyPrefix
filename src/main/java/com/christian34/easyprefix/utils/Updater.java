package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Updater implements Listener {
    private final String UPDATE_MSG;
    private final EasyPrefix instance;
    private String spigotPluginVersion;
    private boolean available;

    public Updater(EasyPrefix instance) {
        this.instance = instance;
        this.UPDATE_MSG = Message.PREFIX + "§7A new update is available at: §bhttps://www.spigotmc"
                + ".org/resources/44580/updates";
        this.available = false;
        Bukkit.getPluginManager().registerEvents(this, instance);
        check();
    }

    public void check() {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                HttpsURLConnection connection = (HttpsURLConnection)
                        new URL("https://api.spigotmc.org/legacy/update.php?resource=44580").openConnection();
                connection.setRequestMethod("GET");
                this.spigotPluginVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                if (!VersionController.getPluginVersion().equals(spigotPluginVersion)) {
                    this.available = true;
                    instance.getServer().getConsoleSender().sendMessage(UPDATE_MSG);
                }
            } catch (IOException ignored) {
                Debug.log("§cUpdate checker failed!");
            }
        });
    }

    public boolean isAvailable() {
        return this.available;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        User user = this.instance.getUser(e.getPlayer());
        if (user.hasPermission(UserPermission.ADMIN)) {
            if (isAvailable()) {
                user.sendMessage(UPDATE_MSG);
            }
        }
    }

}
