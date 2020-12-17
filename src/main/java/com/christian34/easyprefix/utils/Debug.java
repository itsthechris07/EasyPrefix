package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import io.sentry.HubAdapter;
import io.sentry.IHub;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public final class Debug {
    private static IHub hub = null;

    private static void initSentry() {
        Sentry.init(options -> {
            options.setDsn("https://593815c87f604f2da4620b5031945126@o393387.ingest.sentry.io/5242398");
            options.setEnableExternalConfiguration(false);
        });

        hub = HubAdapter.getInstance();

        String client = ConfigKeys.CLIENT_ID.toString("id");
        String[] components = client.split("-");
        if (client.equals("id") || components.length != 5) {
            ConfigKeys.CLIENT_ID.set(UUID.randomUUID().toString());
        }
        EasyPrefix instance = EasyPrefix.getInstance();

        Sentry.configureScope(scope -> {
            User user = new User();
            user.setId(ConfigKeys.CLIENT_ID.toString());
            scope.setUser(user);
        });
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            hub.setTag("plugin-version", VersionController.getPluginVersion());
            hub.setTag("api", Bukkit.getBukkitVersion());
            hub.setTag("server", Bukkit.getVersion());
            hub.setTag("java", System.getProperty("java.version"));
            hub.setTag("storage", instance.getStorageType().name().toLowerCase());
            hub.setTag("groups", String.valueOf(instance.getGroupHandler().getGroups().size()));
            hub.setTag("subgroups", String.valueOf(instance.getGroupHandler().getSubgroups().size()));
        }, 20);
    }

    public static void recordAction(String message) {
        if (hub == null) {
            initSentry();
        }

        hub.addBreadcrumb(message);
    }

    public static void catchException(Exception exception) {
        if (hub == null) {
            initSentry();
        }
        Sentry.captureException(exception);
    }

    public static void handleException(Exception exception) {
        catchException(exception);
        Debug.log("&cAn error occurred while using EasyPrefix. If you think this is an error, please report following exception to GitHub!");
        Debug.log("&c------ ERROR ------");
        exception.printStackTrace();
        Debug.log("&c------ END OF ERROR ------");
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(Message.PREFIX + Message.setColors(message));
    }

}
