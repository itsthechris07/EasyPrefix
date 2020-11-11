package com.christian34.easyprefix.messages;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.VersionController;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public final class Messages {
    private static final EasyPrefix instance;
    private static final String PREFIX = "§7[§5EasyPrefix§7] ";
    private static FileConfiguration data;
    private static Language language;

    static {
        instance = EasyPrefix.getInstance();
    }

    @NotNull
    public static Language getLanguage() {
        return language;
    }

    public static void setLanguage(@NotNull Language lang) {
        instance.getFileManager().getConfig().set(ConfigKeys.LANG.getPath(), lang.getId());
        load();
    }

    public static void load() {
        Plugin plugin = instance.getPlugin();

        String langId = ConfigKeys.PLUGIN_LANG.toString();
        language = Language.getByName(langId);
        if (language == null) {
            Debug.log("§cCouldn't load messages for language '" + langId + "'! Please use a valid language!");
            setLanguage(Language.en_EN);
            Debug.log("§cYour language has been set to en_EN!");
            return;
        }

        String langFile = language.getId() + ".yml";
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

    private static void replaceInFile(@NotNull File file) throws IOException {
        File tempFile = new File("plugins/EasyPrefix", "messages.tmp");
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
            Debug.log("§cCouldn't update file messages.yml! Please consider an update to newer a newer Minecraft version.");
        }
    }

    @Nullable
    static List<String> getList(@NotNull String path) {
        return data.getStringList(path);
    }

    @Nullable
    static String getText(@NotNull String path) {
        return data.getString(path);
    }

    @NotNull
    public static String getPrefix() {
        return PREFIX;
    }

    @Nullable
    public static String translate(String text) {
        return (text != null) ? ChatColor.translateAlternateColorCodes('&', text) : null;
    }

    public enum Language {
        en_EN("English"),
        de_DE("Deutsch"),
        it_IT("Italiano");

        private final String NAME;

        Language(String name) {
            this.NAME = name;
        }

        public static Language getByName(String val) {
            for (Language lang : values()) {
                if (lang.name().equalsIgnoreCase(val)) {
                    return lang;
                }
            }
            return null;
        }

        public String getName() {
            return NAME;
        }

        public String getId() {
            return name();
        }

        public Language getNext() {
            if (this == Language.en_EN) {
                return de_DE;
            } else if (this == Language.de_DE) {
                return it_IT;
            } else {
                return en_EN;
            }
        }

    }

}
