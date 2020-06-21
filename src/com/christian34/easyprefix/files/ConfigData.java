package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class ConfigData {
    private final EasyPrefix instance;
    private File file;
    private FileConfiguration data;

    protected ConfigData(EasyPrefix instance) {
        this.instance = instance;
    }

    public ConfigData load() {
        this.file = new File(FileManager.getPluginFolder(), "config.yml");
        if (!file.exists()) {
            this.instance.getPlugin().saveResource("config.yml", true);
        } else {
            try {
                ConfigUpdater.update(this.instance, "config.yml", file, new ArrayList<>());
            } catch (IOException ignored) {
            }
        }
        this.data = YamlConfiguration.loadConfiguration(file);
        return this;
    }

    public void save() {
        try {
            data.options().copyDefaults(true);
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        load();
    }

    public FileConfiguration getData() {
        return data;
    }

    public String getString(ConfigKeys key) {
        return getData().getString(key.toString());
    }

    public boolean getBoolean(ConfigKeys key) {
        return getData().getBoolean(key.toString());
    }

    public void set(String path, Object value) {
        getData().set(path, value);
        save();
    }

    public enum ConfigKeys {
        COLOR_RAINBOW_COLORS("chat.color.rainbow.colors"), CUSTOM_LAYOUT("user.custom-layout.enabled"), ENABLED("enabled"), FORCE_GENDER("gender.force-gender"), GUI_SHOW_ALL_CHATCOLORS("gui.show-all-chatcolors"), HANDLE_CHAT("chat.handle-chat"), HANDLE_COLORS("chat.handle-colors"), HIDE_JOIN_QUIT("join-quit-messages.hide-messages"), JOIN_QUIT_SOUND_RECEIVER("join-quit-messages.sound.receiver"), LANG("lang"), PREFIX_ALIAS("user.custom-layout.alias.prefix"), QUIT_SOUND("join-quit-messages.sound.quit.sound"), SUFFIX_ALIAS("user.custom-layout.alias.suffix"), USE_GENDER("gender.enabled"), USE_JOIN_QUIT("join-quit-messages.enabled"), USE_QUIT_SOUND("join-quit-messages.sound.quit.enabled"), USE_SQL("sql.enabled"), USE_SUBGROUPS("subgroups.enabled");

        private final String KEY;

        ConfigKeys(String key) {
            this.KEY = key;
        }

        @Override
        public String toString() {
            return "config." + KEY;
        }

    }

}
