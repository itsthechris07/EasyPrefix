package com.christian34.easyprefix.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * EasyPrefix 2026.
 * <p>
 * Logs messages and errors of EasyPrefix to the console. Messages may contain legacy colors (&amp;c or §c), which are
 * shown as colors in the console.
 *
 * @author Christian34
 */
public final class Debug {
    private static final Pattern LEGACY_AMPERSAND = Pattern.compile("&([0-9a-fk-orA-FK-OR])");
    private static ComponentLogger logger;

    private Debug() {
    }

    /**
     * called once when the plugin is enabled
     */
    public static void init(@NotNull Plugin plugin) {
        logger = plugin.getComponentLogger();
    }

    /**
     * logs an action on the debug level, which is hidden unless it is enabled for EasyPrefix in the logging config
     */
    public static void recordAction(String message) {
        if (logger != null) logger.debug(format(message));
    }

    public static void log(String message) {
        if (logger == null) fallback(message, null);
        else logger.info(format(message));
    }

    public static void warn(String message) {
        if (logger == null) fallback(message, null);
        else logger.warn(format(message));
    }

    /**
     * reports an error that has been handled, without the stack trace
     */
    public static void catchException(Exception exception) {
        warn("An error occurred: " + exception);
    }

    /**
     * reports an unexpected error with its stack trace
     */
    public static void handleException(Exception exception) {
        String message = "An error occurred while using EasyPrefix. If you think this is an error, please report the following exception in the discussion tab of EasyPrefix on spigotmc.org!";
        if (logger == null) fallback(message, exception);
        else logger.error(Component.text(message), exception);
    }

    static Component format(String message) {
        return LegacyComponentSerializer.legacySection().deserialize(LEGACY_AMPERSAND.matcher(message).replaceAll("§$1"));
    }

    /**
     * before the plugin is enabled (e.g. in tests without a server)
     */
    private static void fallback(String message, @Nullable Exception exception) {
        System.out.println("[EasyPrefix] " + PlainTextComponentSerializer.plainText().serialize(format(message)));
        if (exception != null) exception.printStackTrace(System.out);
    }

}
