package com.christian34.easyprefix.messages;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.ConfigUpdater;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.placeholderapi.PlaceholderAPI;
import com.christian34.easyprefix.user.User;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Messages {
    private static FileConfiguration data;
    private static List<String> languages = Arrays.asList("en_EN", "de_DE", "it_IT");
    private static String language;

    public static String getLanguage() {
        return language;
    }

    public static void setLanguage(String lang) {
        if (languages.contains(lang)) {
            language = lang;
            FileManager.getConfig().set(ConfigData.ConfigKeys.LANG.toString(), lang);
            load();
        }
    }

    public static String langToName() {
        switch (language) {
            case "de_DE":
                return "Deutsch";
            case "it_IT":
                return "Italiano";
            default:
                return "English";
        }
    }

    public static void load() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("EasyPrefix");
        ConfigData config = FileManager.getConfig();
        language = config.getData().getString("config.lang");
        String path = "plugins/EasyPrefix";
        if (language == null || !languages.contains(language)) {
            language = "en_EN";
            config.set("config.lang", "en_EN");
        }
        File file = new File(path, language + ".yml");
        if (!file.exists()) {
            plugin.saveResource(language + ".yml", false);
        } else {
            try {
                ConfigUpdater.update(EasyPrefix.getInstance(), language + ".yml", file, new ArrayList<>());
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        try {
            data.save(file);
        } catch(IOException ignored) {
        }
    }

    public static List<String> getList(Message message) {
        List<String> temp = new ArrayList<>();
        for (String msg : data.getStringList(message.toString())) {
            temp.add(translate(msg));
        }
        return temp;
    }

    @Nullable
    public static String getText(@NotNull String path) {
        return translate(data.getString(path));
    }

    public static String getText(Message message) {
        if (message != null) {
            return translate(data.getString(message.toString()));
        }
        return null;
    }

    public static String getText(Message message, User user) {
        String text = "";
        if (message != null) {
            text = translate(data.getString(message.toString()));
        }
        if (PlaceholderAPI.isEnabled()) {
            return PlaceholderAPI.setPlaceholder(user.getPlayer(), text);
        } else {
            return text;
        }
    }

    public static String getMessage(Message message) {
        return getPrefix() + translate(data.getString(message.toString()));
    }

    public static String getMessage(Message message, User user) {
        String text = translate(data.getString(message.toString()));
        if (PlaceholderAPI.isEnabled()) {
            return getPrefix() + PlaceholderAPI.setPlaceholder(user.getPlayer(), text);
        } else {
            return getPrefix() + text;
        }
    }

    public static String getPrefix() {
        return "§7[§5EasyPrefix§7] ";
    }

    private static String translate(String text) {
        return (text != null) ? ChatColor.translateAlternateColorCodes('&', text) : null;
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(getPrefix() + translate(message));
    }

}