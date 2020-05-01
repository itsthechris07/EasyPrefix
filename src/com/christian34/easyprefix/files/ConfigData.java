package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
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
    private File file;
    private FileConfiguration fileData;

    public ConfigData load() {
        EasyPrefix instance = EasyPrefix.getInstance();
        this.file = new File(FileManager.getPluginFolder(), "config.yml");
        if (!file.exists()) {
            instance.getPlugin().saveResource("config.yml", true);
        } else {
            try {
                ConfigUpdater.update(instance, "config.yml", file, new ArrayList<>());
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        this.fileData = YamlConfiguration.loadConfiguration(file);
        return this;
    }

    public void save() {
        try {
            fileData.options().copyDefaults(true);
            fileData.save(file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        load();
    }

    public FileConfiguration getFileData() {
        return fileData;
    }

    public void set(String path, Object value) {
        getFileData().set(path, value);
        save();
    }

    public enum Values {
        ENABLED("config.enabled"), LANG("config.lang"), CUSTOM_PREFIX("config.user.custom-prefix.enabled"), COLOR_RAINBOW_COLORS("config.chat.color.rainbow.colors"), GUI_SHOW_ALL_CHATCOLORS("config.gui.show-all-chatcolors"), USE_GENDER("config.gender.enabled"), GENDER_TYPES("config.gender.types"), FORCE_GENDER("config.gender.force-gender"), USE_SUBGROUPS("config.subgroups.enabled"), USE_JOIN_QUIT("config.join-quit-messages.enabled"), HIDE_JOIN_QUIT("config.join-quit-messages.hide-messages"), JOIN_QUIT_SOUND_RECEIVER("config.join-quit-messages.sound.receiver"), USE_JOIN_SOUND("config.join-quit-messages.sound.join.enabled"), JOIN_SOUND("config.join-quit-messages.sound.join.sound"), USE_QUIT_SOUND("config.join-quit-messages.sound.quit.enabled"), QUIT_SOUND("config.join-quit-messages.sound.quit.sound"), USE_SQL("config.sql.enabled"), SQL_HOST("config.sql.host"), SQL_PORT("config.sql.port"), SQL_DATABASE("config.sql.database"), SQL_USERNAME("config.sql.username"), SQL_PASSWORD("config.sql.password"), SQL_TABLE_PREFIX("config.sql.table-prefix"), DUPLICATE_WHITE_SPACES("config.chat.duplicate-white-spaces");

        private final String KEY;

        Values(String key) {
            this.KEY = key;
        }

        @Override
        public String toString() {
            return KEY;
        }

    }

}