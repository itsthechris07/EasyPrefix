package com.christian34.easyprefix.files;

import com.christian34.easyprefix.utils.Debug;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EasyPrefix 2026.
 * <p>
 * Replaces {@code ${NAME}} in config values with the environment variable {@code NAME}, so secrets like the MySQL
 * password don't have to stand in config.yml (which server owners share in support chats, with AI assistants or in
 * git). Values are resolved when they are read, never written back: saving the config keeps {@code ${NAME}}.
 * Thread-safe (values are read async and on Folia from several threads).
 *
 * @author Christian34
 */
public final class EnvironmentVariables {
    private static final Pattern REFERENCE = Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*)}");
    private static final Function<String, String> SYSTEM = System::getenv;
    /**
     * variables that aren't set and were already reported, so the console gets one warning per variable
     */
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
    private static volatile Function<String, String> lookup = SYSTEM;
    // replaced by tests
    static volatile Consumer<String> warning = Debug::warn;

    private EnvironmentVariables() {
    }

    /**
     * @param value a config value, e.g. {@code "${EP_MYSQL_PASSWORD}"} or {@code "${EP_HOST}:3306"}
     * @param key   the config key, only for the warning
     * @return the value with every {@code ${NAME}} replaced by the environment variable (empty if it isn't set, with a
     * warning naming the variable, never a value); values without a reference are returned unchanged
     */
    @Nullable
    public static String resolve(@Nullable String value, @NotNull String key) {
        if (value == null || !value.contains("${")) return value;
        Function<String, String> lookup = EnvironmentVariables.lookup;
        Matcher matcher = REFERENCE.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            String variable = lookup.apply(name);
            if (variable == null) {
                if (WARNED.add(name)) {
                    warning.accept("The environment variable " + name + " (used in " + key + " of config.yml) isn't set");
                }
                variable = "";
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(variable));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * replaces {@link System#getenv(String)} (tests can't set real environment variables)
     */
    public static void setLookup(@NotNull Function<String, String> lookup) {
        EnvironmentVariables.lookup = Objects.requireNonNull(lookup);
        WARNED.clear();
    }

    public static void resetLookup() {
        setLookup(SYSTEM);
    }

}
