package com.christian34.easyprefix;

import com.christian34.easyprefix.bungeecord.MessageListener;
import com.christian34.easyprefix.commands.CommandListener;
import com.christian34.easyprefix.commands.TabComplete;
import com.christian34.easyprefix.discordsrv.DiscordSRVHoster;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.listeners.JoinListener;
import com.christian34.easyprefix.listeners.QuitListener;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.placeholderapi.PlaceholderAPI;
import com.christian34.easyprefix.user.Gender;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Metrics;
import com.christian34.easyprefix.utils.RainbowEffect;
import com.christian34.easyprefix.utils.Updater;
import com.christian34.easyprefix.utils.VersionController;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Set;

public class EasyPrefix extends JavaPlugin {
    private static EasyPrefix instance;
    private Plugin plugin;
    private Database database;
    private DiscordSRVHoster discordSRVHoster = null;
    private boolean useBungee = false;

    public static EasyPrefix getInstance() {
        return instance;
    }

    public DiscordSRVHoster getDiscordSRVHoster() {
        return discordSRVHoster;
    }

    public boolean isUseBungee() {
        return useBungee;
    }

    public void onDisable() {
        if (getDatabase() != null) {
            try {
                if (getDatabase().getConnection() != null && !getDatabase().getConnection().isClosed()) {
                    getDatabase().getConnection().close();
                }
            } catch(SQLException ignored) {
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null) player.closeInventory();
        }
        if (isUseBungee()) {
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "easyprefix:chatlistener");
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "easyprefix:joinquit");
        }
    }

    public void onEnable() {
        instance = this;
        this.plugin = this;
        FileManager.load();
        ConfigData cfg = FileManager.getConfig();
        if (cfg.getFileData().getBoolean("config.bungeecord") && isBungeeConfigured()) {
            this.useBungee = true;
            getServer().getMessenger().registerOutgoingPluginChannel(this, "easyprefix:chatlistener");
            getServer().getMessenger().registerOutgoingPluginChannel(this, "easyprefix:chatlayout");
            getServer().getMessenger().registerOutgoingPluginChannel(this, "easyprefix:quitlistener");

            getServer().getMessenger().registerIncomingPluginChannel(this, "easyprefix:joinlistener", new MessageListener());
            getServer().getMessenger().registerIncomingPluginChannel(this, "easyprefix:logger", new MessageListener());
        }
        Messages.load();
        Gender.load();
        if (FileManager.getConfig().getFileData().getBoolean(ConfigData.Values.USE_SQL.toString())) {
            this.database = new Database();
        }
        GroupHandler.load();
        String configVersion = cfg.getFileData().getString("config.version");
        if (configVersion == null) update();
        PluginCommand mainCmd = getCommand("EasyPrefix");
        if (mainCmd != null) {
            mainCmd.setExecutor(new CommandListener());
            mainCmd.setTabCompleter(new TabComplete());
        }
        registerEvents();
        if (!cfg.getFileData().getBoolean("config.enabled")) {
            Bukkit.getServer().getConsoleSender().sendMessage(Messages.getPrefix() + "§cPlugin has been disabled! §7Please enable it in \"config.yml\"");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("placeholderapi", () -> (PlaceholderAPI.isEnabled()) ? "installed" : "not installed"));
        metrics.addCustomChart(new Metrics.SimplePie("lang", () -> cfg.getFileData().getString("config.lang")));
        metrics.addCustomChart(new Metrics.SimplePie("sql", () -> cfg.getFileData().getString("config.sql.enabled")));
        metrics.addCustomChart(new Metrics.SimplePie("bungeecord", () -> (useBungee) ? "true" : "false"));
        PlaceholderAPI.setEnabled(Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"));
        Updater.checkForUpdates();
        Bukkit.getServer().getConsoleSender().sendMessage(Messages.getPrefix() + "§bPlugin has been enabled! §bVersion: §7" + getDescription().getVersion());
        Bukkit.getServer().getConsoleSender().sendMessage(Messages.getPrefix() + "§bIf you like the plugin or you have suggestions, please write a review on spigotmc.org!");
        if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null) {
            this.discordSRVHoster = new DiscordSRVHoster(this);
        }
    }

    private boolean isBungeeConfigured() {
        if (!getServer().getVersion().contains("Spigot") && !getServer().getVersion().contains("Paper")) {
            return false;
        }
        return !getServer().spigot().getConfig().getConfigurationSection("settings").getBoolean("settings.bungeecord");
    }

    public void reload() {
        FileManager.load();
        if (FileManager.getConfig().getFileData().getBoolean(ConfigData.Values.USE_SQL.toString()) && this.database != null) {
            try {
                getDatabase().getConnection().close();
            } catch(SQLException ignored) {
            }
            this.database = new Database();
        } else {
            this.database = null;
        }
        Messages.load();
        Gender.load();
        GroupHandler.load();
        User.getUsers().clear();
        RainbowEffect.getRainbowColors().clear();
    }

    private void update() {
        Bukkit.getServer().getConsoleSender().sendMessage(Messages.getPrefix() + "§cYour configuration is not up to date!");
        Bukkit.getServer().getConsoleSender().sendMessage(Messages.getPrefix() + "§cUpdating files...");
        ConfigData config = FileManager.getConfig();
        GroupsData groups = FileManager.getGroups();
        try {
            Set<String> oldGroups = config.getFileData().getConfigurationSection("config.prefix").getKeys(false);
            for (String name : oldGroups) {
                groups.getFileData().set("groups." + name + ".prefix", config.getFileData().getString("config.prefix." + name + ".prefix"));
                groups.getFileData().set("groups." + name + ".suffix", config.getFileData().getString("config.prefix." + name + ".suffix"));
                groups.getFileData().set("groups." + name + ".chat-color", config.getFileData().getString("config.prefix." + name + ".chatcolor"));
            }
            File file = new File(getDataFolder(), "config.yml");
            file.renameTo(new File(getDataFolder(), "old-config.yml"));
            groups.save();
        } catch(Exception ignored) {
        }
        config.getFileData().set("config.prefix", null);
        config.getFileData().set("config.version", VersionController.getPluginVersion());
        config.getFileData().set("config.enabled", true);
        config.save();
        Bukkit.getServer().getConsoleSender().sendMessage(Messages.getPrefix() + "§bFiles have been updated!");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
    }

    public Database getDatabase() {
        return database;
    }

    public Plugin getPlugin() {
        return plugin;
    }

}