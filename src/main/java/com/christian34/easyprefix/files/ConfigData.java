package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.utils.Debug;
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

    public void load() {
        this.file = new File(FileManager.getPluginFolder(), "config.yml");
        if (!file.exists()) {
            this.instance.getPlugin().saveResource("config.yml", true);
        } else {
            try {
                ConfigUpdater.update(this.instance, "config.yml", file, new ArrayList<>());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            data.options().copyDefaults(true);
            data.save(file);
        } catch (IOException e) {
            Debug.handleException(e);
        }
        load();
    }

    public FileConfiguration getData() {
        return data;
    }

    public void set(String path, Object value) {
        getData().set(path, value);
        save();
    }

}
