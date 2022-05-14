package com.christian34.easyprefix.files;

import com.christian34.easyprefix.utils.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public abstract class PluginFile {
    private final File sourceFile;
    private final String dataPrefix;
    private FileConfiguration data;

    public PluginFile(File sourceFile, String dataPrefix) {
        this.sourceFile = sourceFile;
        if (dataPrefix != null) {
            this.dataPrefix = dataPrefix + ".";
        } else {
            this.dataPrefix = "";
        }
        load();
    }

    @NotNull
    public FileConfiguration getData() {
        return data;
    }

    private void load() {
        if (!getSourceFile().exists()) {
            try {
                createFile();
            } catch (IOException ex) {
                throw new Error(ex.getMessage());
            }
        }
        try {
            update();
        } catch (IOException ex) {
            Debug.warn("Couldn't update file '" + getSourceFile().getName() + "'!");
        }
        try {
            this.data = YamlConfiguration.loadConfiguration(getSourceFile());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void set(String path, Object value) {
        data.set(dataPrefix + path, value);
    }

    public void save(String path, Object value) {
        set(path, value);
        save();
    }

    public synchronized void save() {
        try {
            data.save(getSourceFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        load();
    }

    public File getSourceFile() {
        return sourceFile;
    }

    @Nullable
    public String getString(@NotNull String key) {
        return data.getString(dataPrefix + key);
    }

    public String getString(@NotNull String key, @NotNull String alternative) {
        String val = getString(key);
        if (val == null || val.isEmpty()) {
            return alternative;
        }
        return val;
    }

    public int getInt(@NotNull String key) {
        return data.getInt(dataPrefix + key);
    }

    public double getDouble(@NotNull String key) {
        return data.getDouble(dataPrefix + key);
    }

    public Boolean getBoolean(@NotNull String key) {
        return data.getBoolean(dataPrefix + key);
    }

    @Nullable
    public ConfigurationSection getSection(@Nullable String key) {
        String prefix;
        if (key == null || key.isEmpty()) {
            prefix = dataPrefix.substring(0, dataPrefix.length() - 1);
        } else {
            prefix = dataPrefix;
        }
        return data.getConfigurationSection(prefix + key);
    }

    @NotNull
    public List<String> getList(@NotNull String key) {
        return data.getStringList(dataPrefix + key);
    }

    public abstract void createFile() throws IOException;

    public abstract void update() throws IOException;

}
