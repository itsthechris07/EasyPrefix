package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.VersionController;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class MessageData {
    private final EasyPrefix instance;
    private FileConfiguration data;

    public MessageData(EasyPrefix instance) {
        this.instance = instance;
        load();
    }

    public void load() {
        Plugin plugin = instance.getPlugin();
        String langFile = "messages.yml";
        File file = new File(FileManager.getPluginFolder(), langFile);
        if (!file.exists()) {
            plugin.saveResource(langFile, false);
        } else {
            try {
                ConfigUpdater.update(instance, langFile, file, new ArrayList<>());
            } catch (IOException e) {
                Debug.log("§cCouldn't update file. Please report this error on github.com!");
                e.printStackTrace();
            }
        }
        if (VersionController.getMinorVersion() == 8) {
            try {
                replaceInFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        data = YamlConfiguration.loadConfiguration(file);
    }

    private void replaceInFile(@NotNull File file) throws IOException {
        File tempFile = new File(FileManager.getPluginFolder(), "messages.tmp");
        if (!tempFile.createNewFile()) {
            return;
        }

        FileWriter writer = new FileWriter(tempFile);
        Reader reader = new FileReader(file);

        try (BufferedReader br = new BufferedReader(reader)) {
            while (br.ready()) {
                writer.write(br.readLine().replaceAll("[^\\x20-\\x7e]", "") + "\n");
            }
        } catch (IOException ignored) {
        }

        writer.close();
        reader.close();
        if (!file.delete() && !tempFile.renameTo(file)) {
            Debug.log("§cCouldn't update file 'messages.yml'! Please consider an update to newer a newer Minecraft version.");
        }
    }

    @NotNull
    public List<String> getList(@NotNull String path) {
        return data.getStringList(path);
    }

    @NotNull
    public String getText(@NotNull String path) {
        String val = data.getString(path);
        return val == null ? "" : val;
    }

}
