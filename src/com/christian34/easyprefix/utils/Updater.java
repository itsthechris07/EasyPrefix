package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Messages;
import org.bukkit.Bukkit;

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
public class Updater {
    public final String UPDATE_MSG = "§7A new update is available at: §bhttps://www.spigotmc" + ".org/resources/44580/updates";
    private final String ERR_MSG = "§cUpdate checker failed!";
    private String spigotPluginVersion;
    private boolean available = false;
    private final EasyPrefix instance;

    public Updater(EasyPrefix instance) {
        this.instance = instance;
    }

    public boolean checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(EasyPrefix.getInstance().getPlugin(), () -> {
            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update" + ".php?resource=44580").openConnection();
                connection.setRequestMethod("GET");
                spigotPluginVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            } catch(IOException e) {
                Bukkit.getServer().getConsoleSender().sendMessage(Messages.getPrefix() + ERR_MSG);
                return;
            }
            if (!VersionController.getPluginVersion().equals(spigotPluginVersion)) {
                available = true;
                Bukkit.getServer().getConsoleSender().sendMessage(Messages.getPrefix() + UPDATE_MSG);
            }
        });
        return this.available;
    }


}