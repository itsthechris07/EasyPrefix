package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.sql.InsertStatement;
import com.christian34.easyprefix.sql.SelectQuery;
import com.christian34.easyprefix.sql.UpdateStatement;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EasyPrefix 2026.
 * <p>
 * The settings of config.yml that all servers with the same database share, stored as yaml in the options table.
 * Shared is what players see or what has to match the shared data (e.g. the colors, users store their names).
 * Switches of single features (e.g. handle-chat, tab list, join messages) and settings that are only read on the
 * start (tags and the command aliases register commands) stay local.
 * <p>
 * On the start the database wins, a new database gets the local settings. Changes in the setup GUI and /ep reload
 * (after editing config.yml) are uploaded and the other servers take them over with the next sync.
 *
 * @author Christian34
 */
public final class SharedConfig {
    static final String OPTION = "shared_config";
    public static final List<String> KEYS = List.of(
            ConfigData.Keys.CUSTOM_LAYOUT,
            ConfigData.Keys.CUSTOM_LAYOUT_COOLDOWN,
            ConfigData.Keys.CUSTOM_LAYOUT_BLACKLIST,
            ConfigData.Keys.NAME_HOVER,
            ConfigData.Keys.NAME_CLICK,
            ConfigData.Keys.DATE_FORMAT,
            ConfigData.Keys.COLOR_ICON,
            "chat.colors",
            "chat.decorations",
            ConfigData.Keys.DISPLAY_TAB_LIST_LAYOUT,
            ConfigData.Keys.DISPLAY_NAME_TAG_PREFIX,
            ConfigData.Keys.DISPLAY_NAME_TAG_SUFFIX);

    private SharedConfig() {
    }

    /**
     * @param path a path of config.yml without 'config.', e.g. chat.colors.red.display-name
     */
    public static boolean isShared(String path) {
        return KEYS.stream().anyMatch(key -> path.equals(key) || path.startsWith(key + "."));
    }

    /**
     * on the start: takes over the shared settings of the database, a new database gets the local ones
     */
    public static void load(EasyPrefix instance) {
        String yaml = read();
        if (yaml == null) {
            upload(instance);
        } else {
            apply(instance.getConfigData(), yaml);
        }
    }

    /**
     * takes over the shared settings of the database, e.g. after another server changed them
     *
     * @return true if a setting has changed
     */
    public static boolean download(EasyPrefix instance) {
        String yaml = read();
        return yaml != null && apply(instance.getConfigData(), yaml);
    }

    /**
     * writes the shared settings of this server into the database and tells the other servers to take them over
     */
    public static synchronized void upload(EasyPrefix instance) {
        String yaml = toYaml(instance.getConfigData());
        if (read() == null) {
            new InsertStatement("options").setValue("option_name", OPTION).setValue("option_value", yaml).execute();
        } else {
            new UpdateStatement("options").setValue("option_value", yaml).addCondition("option_name", OPTION).execute();
        }
        instance.getSqlDatabase().announce(SQLSynchronizer.Type.CONFIG, null);
    }

    @Nullable
    private static String read() {
        return new SelectQuery("options", "option_value").addCondition("option_name", OPTION).getData().getString("option_value");
    }

    static String toYaml(ConfigData config) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (String key : KEYS) {
            Object value = config.getData().get("config." + key);
            if (value instanceof ConfigurationSection section) {
                yaml.createSection(key, toMap(section));
            } else {
                yaml.set(key, value);
            }
        }
        return yaml.saveToString();
    }

    /**
     * @return true if a setting has changed
     */
    private static boolean apply(ConfigData config, String text) {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(text);
        } catch (InvalidConfigurationException ex) {
            Debug.warn("The shared settings in the database are broken, the local ones are used: " + ex.getMessage());
            return false;
        }
        String before = toYaml(config);
        for (String key : KEYS) {
            // settings added by newer versions are not in the database yet
            if (!yaml.contains(key)) continue;
            Object value = yaml.get(key);
            if (value instanceof ConfigurationSection section) {
                config.set(key, null);
                config.getData().createSection("config." + key, toMap(section));
            } else {
                config.set(key, value);
            }
        }
        if (before.equals(toYaml(config))) return false;
        // the file shows the settings that are used
        config.save();
        return true;
    }

    private static Map<String, Object> toMap(ConfigurationSection section) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            map.put(key, value instanceof ConfigurationSection child ? toMap(child) : value);
        }
        return map;
    }

}
