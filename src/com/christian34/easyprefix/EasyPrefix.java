package com.christian34.easyprefix;

import com.christian34.easyprefix.commands.CommandListener;
import com.christian34.easyprefix.commands.TabComplete;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
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
import com.christian34.easyprefix.vault.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;

public class EasyPrefix extends JavaPlugin {
    private static EasyPrefix instance;
    public ArrayList<User> users;
    private Plugin plugin;
    private Database database;
    private GroupHandler groupHandler;
    private VaultManager vaultManager = null;

    public boolean formatChat() {
        return !Bukkit.getServer().getPluginManager().isPluginEnabled("MultiChatSpigot");
    }

    public void onDisable() {
        if (getDatabase() != null) getDatabase().close();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null) player.closeInventory();
        }
    }

    public void onEnable() {
        instance = this;
        this.plugin = this;
        this.users = new ArrayList<>();
        FileManager.load();
        ConfigData cfg = FileManager.getConfig();
        Messages.load();
        Gender.load();
        if (cfg.getFileData().getBoolean(ConfigData.Values.USE_SQL.toString())) {
            this.database = new Database();
        }
        this.groupHandler = new GroupHandler(this);
        PluginCommand mainCmd = getCommand("EasyPrefix");
        assert mainCmd != null;
        mainCmd.setExecutor(new CommandListener(this));
        mainCmd.setTabCompleter(new TabComplete(this));
        registerEvents();
        if (!cfg.getFileData().getBoolean("config.enabled")) {
            Messages.log("§cPlugin has been disabled! §7Please enable it in \"config.yml\"");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            this.vaultManager = new VaultManager(this);
            if (vaultManager.hook()) {
                Messages.log("&aConnected to Vault...");
            } else {
                Messages.log("&cCouldn't connect to Vault!");
            }
        }

        PlaceholderAPI.setEnabled(Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"));
        hookMetrics();
        Updater.checkForUpdates();
        Messages.log("§bPlugin has been enabled! §bVersion: §7" + getDescription().getVersion());
        Messages.log("§bIf you like the plugin or you have suggestions, please write a review " + "on spigotmc.org!");
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public GroupHandler getGroupHandler() {
        return groupHandler;
    }

    public User getUser(Player player) {
        for (User crntUser : users) {
            if (crntUser.getPlayer().getName().equals(player.getName())) return crntUser;
        }
        User newUser = new User(player);
        users.add(newUser);
        return newUser;
    }

    public void unloadUser(final Player player) {
        users.removeIf(crntUser -> crntUser.getPlayer().getName().equals(player.getName()));
    }

    public ArrayList<User> getUsers() {
        return users;
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
        this.groupHandler = new GroupHandler(this);
        RainbowEffect.getRainbowColors().clear();
    }

    public Database getDatabase() {
        return database;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
    }

    private void hookMetrics() {
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("placeholderapi", () -> (PlaceholderAPI.isEnabled()) ? "installed" : "not installed"));
        metrics.addCustomChart(new Metrics.SimplePie("lang", Messages::getLanguage));
        metrics.addCustomChart(new Metrics.SimplePie("sql", () -> (getDatabase() != null) ? "true" : "false"));
    }

    public static EasyPrefix getInstance() {
        return instance;
    }

}