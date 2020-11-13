package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.messages.Messages;
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
    private static final IHub hub;

    static {
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

        hub.setTag("plugin-version", VersionController.getPluginVersion());
        hub.setTag("api", Bukkit.getBukkitVersion());
        hub.setTag("server", Bukkit.getVersion());
        hub.setTag("java", System.getProperty("java.version"));
        hub.setTag("storage", instance.getStorageType().name().toLowerCase());
        hub.setTag("groups", String.valueOf(instance.getGroupHandler().getGroups().size()));
    }

    public static void recordAction(String message) {
        hub.addBreadcrumb(message);
    }

    public static void captureException(Exception exception) {
        Sentry.captureException(exception);
        log("An error occurred while using EasyPrefix:");
        exception.printStackTrace();
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(Messages.getPrefix() + Messages.setColors(message));
    }

}
