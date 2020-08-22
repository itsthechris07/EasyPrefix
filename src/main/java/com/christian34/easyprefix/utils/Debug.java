package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import io.sentry.Sentry;
import io.sentry.context.Context;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Debug {

    static {
        Sentry.init("https://593815c87f604f2da4620b5031945126@o393387.ingest.sentry.io/5242398");
        String client = ConfigKeys.CLIENT_ID.toString("id");
        String[] components = client.split("-");
        if (client.equals("id") || components.length != 5) {
            ConfigKeys.CLIENT_ID.set(UUID.randomUUID().toString());
        }
        EasyPrefix instance = EasyPrefix.getInstance();
        Context context = Sentry.getContext();
        context.setUser(new UserBuilder().setId(ConfigKeys.CLIENT_ID.toString()).build());
        context.addTag("plugin-version", VersionController.getPluginVersion());
        context.addTag("api", Bukkit.getBukkitVersion());
        context.addTag("server", Bukkit.getVersion());
        context.addTag("java", System.getProperty("java.version"));
        context.addTag("storage", instance.getStorageType().name().toLowerCase());
        context.addTag("groups", instance.getGroupHandler().getGroups().size() + "");
    }

    public static void recordAction(String message) {
        Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setMessage(message).build());
    }

    public static void captureException(Exception exception) {
        Sentry.capture(exception);
    }

}
