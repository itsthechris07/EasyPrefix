package com.christian34.easyprefix.messages;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.extensions.ExpansionManager;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.VersionController;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
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
    private static final EasyPrefix instance;
    private static FileConfiguration data;
    private static String language;

    static {
        instance = EasyPrefix.getInstance();
    }

    public static String getLanguage() {
        return language;
    }

    public static void setLanguage(String lang) {
        if (LANGUAGES.contains(lang)) {
            language = lang;
            instance.getFileManager().getConfig().set(ConfigKeys.LANG.getPath(), lang);
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
        Plugin plugin = instance.getPlugin();
        language = ConfigKeys.PLUGIN_LANG.toString();

        if (!LANGUAGES.contains(language)) {
            Messages.log("§cCouldn't load messages for language '" + language + "'! Please use a valid language!");
            setLanguage("en_EN");
            Messages.log("§cYour language has been set to en_EN!");
            return;
        }

        File file = new File(FileManager.getPluginFolder(), language + ".yml");
        if (!file.exists()) {
            plugin.saveResource(language + ".yml", false);
        } else {
            try {
                ConfigUpdater.update(instance, language + ".yml", file, new ArrayList<>());
            } catch (IOException e) {
                Messages.log("§cCouldn't update file. Please report this error on github.com!");
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

    public static void replaceInFile(@NotNull File file) throws IOException {
        File tempFile = new File("plugins/EasyPrefix", "messages.tmp");
        if (!tempFile.createNewFile()) {
            return;
        }

        FileWriter writer = new FileWriter(tempFile);

        Reader reader = new FileReader(file);
        BufferedReader br = new BufferedReader(reader);

        while (br.ready()) {
            writer.write(br.readLine().replaceAll("[^\\x20-\\x7e]", "") + "\n");
        }

        writer.close();
        br.close();
        reader.close();
        if (!file.delete() && !tempFile.renameTo(file)) {
            Messages.log("§cCouldn't update file messages.yml! Please consider an update to newer a newer Minecraft version.");
        }
    }

    public static List<String> getList(@NotNull Message message) {
        List<String> temp = new ArrayList<>();
        for (String msg : data.getStringList(message.getPath())) {
            temp.add(translate(msg));
        }
        return temp;
    }

    public static String getText(@NotNull String path) {
        return translate(data.getString(path));
    }

    public static String getAndSet(@NotNull Message message, @NotNull String value) {
        String text = translate(data.getString(message.getPath()));
        if (text == null) return null;
        return text.replace("%value%", value);
    }

    public static String getMessage(@NotNull Message message) {
        return getPrefix() + translate(data.getString(message.getPath()));
    }

    public static String getMessage(@NotNull Message message, @NotNull User user) {
        String text = translate(data.getString(message.getPath()));

        if (text == null) {
            throw new RuntimeException("§cCould't read message. Please report this error to github! (" + message.name() + ")");
        }

        ExpansionManager expansionManager = EasyPrefix.getInstance().getExpansionManager();
        if (expansionManager.isUsingPapi()) {
            return getPrefix() + expansionManager.setPapi(user.getPlayer(), text);
        } else {
            return getPrefix() + text;
        }
    }

    public static String getPrefix() {
        return "§7[§5EasyPrefix§7] ";
    }

    @Nullable
    private static String translate(String text) {
        return (text != null) ? ChatColor.translateAlternateColorCodes('&', text) : null;
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(getPrefix() + translate(message));
    }

}