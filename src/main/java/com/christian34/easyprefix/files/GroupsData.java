package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private FileConfiguration data = null;

    protected GroupsData(EasyPrefix instance) {
        this.instance = instance;
    }

    public void load() {
        File pluginFolder = FileManager.getPluginFolder();
        this.file = new File(pluginFolder, "groups.yml");
        if (!file.exists()) {
            this.instance.getPlugin().saveResource("groups.yml", true);
        }
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            Debug.captureException(e);
            e.printStackTrace();
        }
        load();
    }

    @Nullable
    public String getString(String key) {
        return data.getString(key);
    }

    @NotNull
    public Set<String> getSection(String key) {
        ConfigurationSection section = getData().getConfigurationSection(key);
        if (section != null) {
            return section.getKeys(false);
        }
        return Collections.emptySet();
    }

    public void setAndSave(@NotNull String key, @Nullable Object value) {
        getData().set(key, value);
        save();
    }

    public void set(@NotNull String key, @Nullable Object value) {
        getData().set(key, value);
    }

    public FileConfiguration getData() {
        return data;
    }

}
