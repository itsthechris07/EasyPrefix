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
    public static final String UPDATE_MSG = "§7A new update is available at: §bhttps://www.spigotmc" + ".org/resources/44580/updates";
    private final EasyPrefix instance;
    private String spigotPluginVersion;
    private boolean available = false;

    public Updater(EasyPrefix instance) {
        this.instance = instance;
    }

    public boolean checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                HttpsURLConnection connection = (HttpsURLConnection)
                        new URL("https://api.spigotmc.org/legacy/update.php?resource=44580").openConnection();
                connection.setRequestMethod("GET");
                spigotPluginVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            } catch (IOException e) {
                Messages.log("§cUpdate checker failed!");
                return;
            }
            if (spigotPluginVersion.split("\\.")[1].equals("7")) {
                available = true;
                Messages.log(UPDATE_MSG);
            }
        });
        return this.available;
    }

}
