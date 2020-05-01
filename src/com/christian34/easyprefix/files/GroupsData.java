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
    private File file;
    private FileConfiguration fileData;

    public GroupsData load() {
        this.file = new File(FileManager.getPluginFolder(), "groups.yml");
        if (!file.exists()) {
            EasyPrefix.getInstance().getPlugin().saveResource("groups.yml", true);
        }
        this.fileData = YamlConfiguration.loadConfiguration(file);
        if (fileData.getConfigurationSection("groups") == null) {
            File old = new File(FileManager.getPluginFolder(), "groups.yml");
            File backup = new File(FileManager.getPluginFolder(), "backup-groups.yml");
            if (old.renameTo(backup) && old.delete()) {
                load();
            }
        }
        return this;
    }

    public void save() {
        try {
            fileData.save(file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        load();
    }

    public Set<String> getSection(String key) {
        ConfigurationSection section = getFileData().getConfigurationSection(key);
        if (section != null) {
            return section.getKeys(false);
        }
        return Collections.emptySet();
    }

    public void setAndSave(String key, Object value) {
        getFileData().set(key, value);
        save();
    }

    public void set(String key, Object value) {
        getFileData().set(key, value);
    }

    public FileConfiguration getFileData() {
        return fileData;
    }

}