package com.christian34.easyprefix.user;

import com.christian34.easyprefix.files.FileManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class UserData {
    private final UUID uniqueId;
    private File file;
    private FileConfiguration fileData;

    public UserData(UUID uniqueId) {
        this.uniqueId = uniqueId;
        load();
    }

    private void load() {
        this.file = new File(FileManager.getPluginFolder() + "/user", this.uniqueId + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        this.fileData = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getFileData() {
        return fileData;
    }

    public void setAndSave(String path, Object value) {
        getFileData().set(path, value);
        save();
    }

    public void set(String path, Object value) {
        getFileData().set(path, value);
    }

    public void save() {
        try {
            fileData.save(file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        load();
    }

}