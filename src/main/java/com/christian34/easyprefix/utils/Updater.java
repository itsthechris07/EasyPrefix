package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.UserPermission;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
 * Checks the latest release on GitHub for updates and tells admins when they join.
 *
 * @author Christian34
 */
public class Updater implements Listener {
    static final String RELEASES_URL = "https://github.com/itsthechris07/EasyPrefix/releases";
    /**
     * the latest release - drafts and pre-releases are left out, 404 if there is none yet
     */
    private static final URI LATEST_URI = URI.create("https://api.github.com/repos/itsthechris07/EasyPrefix/releases/latest");
    private static final Pattern NUMBERS = Pattern.compile("\\d+");
    private final EasyPrefix instance;
    private volatile String updateMsg;

    public Updater(EasyPrefix instance) {
        this.instance = instance;
        check();
    }

    public void check() {
        if (EasyPrefix.isOffline()) return;
        String current = VersionController.getPluginVersion();
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder(LATEST_URI).timeout(Duration.ofSeconds(10))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "EasyPrefix/" + current)
                .GET().build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((response, error) -> {
            if (current.contains("beta")) {
                Debug.warn("You are using a beta version. Please check regularly for new updates on " + RELEASES_URL);
            }
            if (error != null) {
                Debug.warn("Update checker failed: " + error.getMessage());
                return;
            }
            // no release yet
            if (response.statusCode() == 404) return;
            if (response.statusCode() != 200) {
                Debug.warn("Update checker failed (GitHub answered " + response.statusCode() + ")");
                return;
            }
            Release latest = parse(response.body());
            if (latest != null && isNewer(latest.version(), current)) {
                this.updateMsg = Message.PREFIX + "§7Version §b" + latest.version() + " §7is available at: §b" + latest.url();
                instance.getServer().getConsoleSender().sendMessage(this.updateMsg);
            }
        });
    }

    record Release(@NotNull String version, @NotNull String url) {
    }

    /**
     * @param json the answer of the GitHub api
     * @return the version (tag without a leading "v") and the page of the release, null if it can't be read
     */
    @Nullable
    static Release parse(@NotNull String json) {
        try {
            JsonObject release = JsonParser.parseString(json).getAsJsonObject();
            if (!release.has("tag_name")) return null;
            String version = release.get("tag_name").getAsString().trim().replaceFirst("^[vV]", "");
            String url = release.has("html_url") ? release.get("html_url").getAsString() : RELEASES_URL;
            return new Release(version, url);
        } catch (RuntimeException ex) {
            return null;
        }
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
        return this.updateMsg != null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        String msg = this.updateMsg;
        if (msg != null && e.getPlayer().hasPermission(UserPermission.ADMIN.toString())) {
            e.getPlayer().sendMessage(msg);
        }
    }

}
