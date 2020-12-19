package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.groups.GroupHandler;
import io.sentry.HubAdapter;
import io.sentry.IHub;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Debug {
    private static Debug debug = null;
    private final EasyPrefix instance;
    private final Logger logger = EasyPrefix.getInstance().getLogger();
    private String clientID;
    private IHub hub;

    public Debug(EasyPrefix instance) {
        debug = this;
        this.instance = instance;

        //load config additionally to the file manager, which is loaded later
        File file = new File(FileManager.getPluginFolder(), "config.yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        this.clientID = data.getString("config.client");
        if (clientID == null || clientID.equals("id") || clientID.split("-").length != 5) {
            this.clientID = UUID.randomUUID().toString();
        }
        initSentry();
    }

    /**
     * registers an action without sending the message to the console
     *
     * @param message the text that will be sent to SentryIO
     */
    public static void recordAction(String message) {
        debug.getHub().addBreadcrumb(ChatColor.translateAlternateColorCodes('&', message));
    }

    /**
     * this function registers an action and sends the message to the console
     *
     * @param message the text that will be sent to the console and to SentryIO
     */
    public static void record(String message) {
        recordAction(message);
        log(message);
    }

    public static void catchException(Exception exception) {
        Sentry.captureException(exception);
    }

    public static void handleException(Exception exception) {
        catchException(exception);
        warn("&cAn error occurred while using EasyPrefix. If you think this is an error, please report following exception to GitHub!");
        warn("&c------ ERROR ------");
        exception.printStackTrace();
        warn("&c------ END OF ERROR ------");
    }

    public static void log(String message) {
        log(Level.INFO, message);
    }

    public static void warn(String message) {
        log(Level.WARNING, message);
    }

    private static void log(Level level, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        if (debug.getLogger() == null) {
            System.out.println(message);
            return;
        }
        debug.getLogger().log(level, message);
    }

    public Logger getLogger() {
        return logger;
    }

    public String getClientID() {
        return clientID;
    }

    private IHub getHub() {
        return hub;
    }

    private void initSentry() {
        this.hub = HubAdapter.getInstance();
        Sentry.init(options -> {
            options.setDsn("https://593815c87f604f2da4620b5031945126@o393387.ingest.sentry.io/5242398");
            options.setEnableExternalConfiguration(false);
        });
        Sentry.configureScope(scope -> {
            User user = new User();
            user.setId(clientID);
            scope.setUser(user);
        });
        hub.setTag("plugin-version", VersionController.getPluginVersion());
        hub.setTag("api", Bukkit.getBukkitVersion());
        hub.setTag("server", Bukkit.getVersion());
        hub.setTag("java", System.getProperty("java.version"));
        // set tags after plugin was loaded
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            hub.setTag("storage", instance.getStorageType().name().toLowerCase());
            GroupHandler groupHandler = instance.getGroupHandler();
            hub.setTag("groups", String.valueOf(groupHandler.getGroups().size()));
            hub.setTag("subgroups", String.valueOf(groupHandler.getSubgroups().size()));
        }, 20 * 10);
    }

}
