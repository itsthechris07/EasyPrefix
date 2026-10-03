package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.utils.Debug;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2026.
 * <p>
 * The messages.yml with all texts, missing messages are added on start.
 *
 * @author Christian34
 */
public class MessageData {
    /**
     * 'chat_name_hover' of an older messages.yml, taken over by the default group once
     */
    @Nullable
    private static List<String> legacyNameHover;
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
            // the hover text moved to the default group (groups.yml) - ConfigUpdater removes the old key
            List<String> hover = YamlConfiguration.loadConfiguration(file).getStringList("chat_name_hover");
            if (!hover.isEmpty()) legacyNameHover = hover;
            try {
                ConfigUpdater.update(instance, langFile, file, new ArrayList<>());
            } catch (IOException e) {
                Debug.log("§cCouldn't update file. Please report this error on spigotmc.org (discussion tab of EasyPrefix)!");
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * @return the hover text of an older messages.yml once, null if there was none
     */
    @Nullable
    public static List<String> takeLegacyNameHover() {
        List<String> hover = legacyNameHover;
        legacyNameHover = null;
        return hover;
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
