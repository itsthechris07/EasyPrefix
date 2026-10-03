package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.UserPermission;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EasyPrefix 2026.
 * <p>
 * Checks spigotmc.org for updates and tells admins when they join.
 *
 * @author Christian34
 */
public class Updater implements Listener {
    private static final URI VERSION_URI = URI.create("https://api.spigotmc.org/legacy/update.php?resource=44580");
    private static final Pattern NUMBERS = Pattern.compile("\\d+");
    private final String UPDATE_MSG;
    private final EasyPrefix instance;
    private volatile boolean available;

    public Updater(EasyPrefix instance) {
        this.instance = instance;
        this.UPDATE_MSG = Message.PREFIX + "§7A new update is available at: §bhttps://www.spigotmc.org/resources/44580/updates";
        this.available = false;
        check();
    }

    public void check() {
        if (EasyPrefix.isOffline()) return;
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder(VERSION_URI).timeout(Duration.ofSeconds(10)).GET().build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((response, error) -> {
            if (error != null || response.statusCode() != 200) {
                Debug.warn("Update checker failed!");
                return;
            }
            String latest = response.body().trim();
            String current = VersionController.getPluginVersion();
            if (isNewer(latest, current)) {
                this.available = true;
                instance.getServer().getConsoleSender().sendMessage(UPDATE_MSG);
            }
            if (current.contains("beta")) {
                Debug.warn("You are using a beta version. Please check regularly for new updates on https://www.spigotmc.org/resources/44580/updates");
            }
        });
    }

    /**
     * compares the numbers of both versions (e.g. 2.0.1 &gt; 2.0.0 &gt; 1.8.13), a release is newer than its betas
     */
    static boolean isNewer(@NotNull String latest, @NotNull String current) {
        int[] a = numbers(latest.split("-")[0]), b = numbers(current.split("-")[0]);
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? a[i] : 0, y = i < b.length ? b[i] : 0;
            if (x != y) return x > y;
        }
        return !latest.contains("-") && current.contains("-");
    }

    private static int[] numbers(String version) {
        Matcher matcher = NUMBERS.matcher(version);
        return matcher.results().mapToInt(result -> {
            try {
                return Integer.parseInt(result.group());
            } catch (NumberFormatException ex) {
                return 0;
            }
        }).toArray();
    }

    public boolean isAvailable() {
        return this.available;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (isAvailable() && e.getPlayer().hasPermission(UserPermission.ADMIN.toString())) {
            e.getPlayer().sendMessage(UPDATE_MSG);
        }
    }

}
