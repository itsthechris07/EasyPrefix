package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

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

    public void set(String key, Object value) {
        getFileData().set(key, value);
        save();
    }

    public FileConfiguration getFileData() {
        return fileData;
    }

}