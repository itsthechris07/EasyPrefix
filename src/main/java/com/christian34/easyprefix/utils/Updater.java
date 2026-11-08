package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.UserPermission;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
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
 * Checks the latest release on GitHub for updates (on start, on reload and every 6 hours) and tells admins on every
 * join.
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
    /**
     * 6 hours, servers running for days get to know about new releases as well (GitHub allows 60 requests per hour)
     */
    private static final long CHECK_PERIOD_TICKS = 6 * 60 * 60 * 20L;
    /**
     * after the join messages and the motd of other plugins, so the message isn't lost between them
     */
    private static final long JOIN_DELAY_TICKS = 60L;
    private static final Sound SOUND = Sound.sound(Key.key("block.note_block.pling"), Sound.Source.MASTER, 1f, 1.5f);
    private final EasyPrefix instance;
    private volatile Release latest;

    public Updater(EasyPrefix instance) {
        this.instance = instance;
        if (EasyPrefix.isOffline()) return;
        // the first check runs at once (also starting the timer would check twice)
        check();
        TaskManager.asyncTimer(this::check, CHECK_PERIOD_TICKS, CHECK_PERIOD_TICKS);
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

    public void check() {
        if (EasyPrefix.isOffline()) return;
        String current = VersionController.getPluginVersion();
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder(LATEST_URI).timeout(Duration.ofSeconds(10))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "EasyPrefix/" + current)
                .GET().build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((response, error) -> {
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
            Release release = parse(response.body());
            if (release == null) return;
            if (!isNewer(release.version(), current)) {
                if (current.contains("-")) {
                    Debug.warn("You are using a pre-release. Please check regularly for new updates on " + RELEASES_URL);
                }
                return;
            }
            // only announced once per new release in the console, the admins get it on every join
            boolean announce = latest == null || !latest.equals(release);
            this.latest = release;
            if (announce) notify(instance.getServer().getConsoleSender());
        });
    }

    /**
     * @return the newer release, null if this is the latest version (or it wasn't checked yet)
     */
    @Nullable
    public Release getLatest() {
        return latest;
    }

    /**
     * for tests, which don't ask GitHub
     */
    void setLatest(@Nullable Release latest) {
        this.latest = latest;
    }

    /**
     * sends the update message with a clickable link to the release
     */
    void notify(@NotNull Audience audience) {
        Release release = this.latest;
        if (release == null) return;
        audience.sendMessage(Message.UPDATE_AVAILABLE.component("current", VersionController.getPluginVersion(),
                "latest", release.version(), "url", release.url()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (latest == null || !player.hasPermission(UserPermission.ADMIN.toString())) return;
        TaskManager.runLater(player, () -> {
            if (!player.isOnline()) return;
            notify(player);
            player.playSound(SOUND);
        }, JOIN_DELAY_TICKS);
    }

    public record Release(@NotNull String version, @NotNull String url) {
    }

}
