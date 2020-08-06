package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GroupsData {
    private final EasyPrefix instance;
    private File file;
    private FileConfiguration data;

    protected GroupsData(EasyPrefix instance) {
        this.instance = instance;
    }

    public GroupsData load() {
        File pluginFolder = FileManager.getPluginFolder();
        this.file = new File(pluginFolder, "groups.yml");
        if (!file.exists()) {
            this.instance.getPlugin().saveResource("groups.yml", true);
        }
        this.data = YamlConfiguration.loadConfiguration(file);
        return this;
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        load();
    }

    public String getOrDefault(String key, String defaultValue) {
        String val = data.getString(key);
        return val == null ? defaultValue : val;
    }

    public Set<String> getSection(String key) {
        ConfigurationSection section = getData().getConfigurationSection(key);
        if (section != null) {
            return section.getKeys(false);
        }
        return Collections.emptySet();
    }

    public void setAndSave(String key, Object value) {
        getData().set(key, value);
        save();
    }

    public void set(String key, Object value) {
        getData().set(key, value);
    }

    public FileConfiguration getData() {
        return data;
    }

}
