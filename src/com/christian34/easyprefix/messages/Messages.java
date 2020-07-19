package com.christian34.easyprefix.messages;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.placeholderapi.Placeholder;
import com.christian34.easyprefix.user.User;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Messages {
    private final static List<String> LANGUAGES = Arrays.asList("en_EN", "de_DE", "it_IT");
    private static FileConfiguration data;
    private static String language;

    public static String getLanguage() {
        return language;
    }

    public static void setLanguage(String lang) {
        if (LANGUAGES.contains(lang)) {
            language = lang;
            EasyPrefix.getInstance().getFileManager().getConfig().set(ConfigData.ConfigKeys.LANG.toString(), lang);
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
        try {
            Plugin plugin = EasyPrefix.getInstance().getPlugin();
            ConfigData config = EasyPrefix.getInstance().getFileManager().getConfig();
            language = config.getData().getString("config.lang");
            String path = "plugins/EasyPrefix";
            if (!LANGUAGES.contains(language)) {
                setLanguage("en_EN");
            }
            File file = new File(path, language + ".yml");
            if (!file.exists()) {
                plugin.saveResource(language + ".yml", false);
            } else {
                ConfigUpdater.update(EasyPrefix.getInstance(), language + ".yml", file, new ArrayList<>());
            }
            data = YamlConfiguration.loadConfiguration(file);
            data.save(file);
        } catch (Exception ignored) {
            setLanguage("en_EN");
        }
    }

    public static List<String> getList(Message message) {
        List<String> temp = new ArrayList<>();
        for (String msg : data.getStringList(message.getPath())) {
            temp.add(translate(msg));
        }
        return temp;
    }

    public static String getText(String path) {
        return translate(data.getString(path));
    }

    public static String getAndSet(Message message, String value) {
        if (message != null) {
            String text = translate(data.getString(message.getPath()));
            if (text == null) return null;
            return text.replace("%value%", value);
        }
        return null;
    }

    public static String getMessage(Message message) {
        return getPrefix() + translate(data.getString(message.getPath()));
    }

    public static String getMessage(Message message, User user) {
        String text = translate(data.getString(message.getPath()));
        if (Placeholder.isEnabled()) {
            return getPrefix() + Placeholder.setPlaceholder(user.getPlayer(), text);
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