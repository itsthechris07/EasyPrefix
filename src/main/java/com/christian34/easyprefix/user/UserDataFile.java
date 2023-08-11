package com.christian34.easyprefix.user;

import com.christian34.easyprefix.files.FileManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.util.UUID;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "1.9")
class UserDataFile {
    private final UUID uniqueId;
    private File file;
    private FileConfiguration fileData;

    UserDataFile(UUID uniqueId) {
        this.uniqueId = uniqueId;
        load();
    }

    File getFile() {
        return file;
    }

    FileConfiguration getFileData() {
        return fileData;
    }

    private void load() {
        this.file = new File(FileManager.getPluginFolder() + "/user", this.uniqueId + ".yml");
        if (!file.exists()) {
            this.file = null;
        } else this.fileData = YamlConfiguration.loadConfiguration(file);
    }

}
