package com.christian34.easyprefix;

import com.christian34.easyprefix.commands.CommandHandler;
import com.christian34.easyprefix.database.Database;
import com.christian34.easyprefix.extensions.ExpansionManager;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.listeners.JoinListener;
import com.christian34.easyprefix.listeners.QuitListener;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Metrics;
import com.christian34.easyprefix.utils.RainbowEffect;
import com.christian34.easyprefix.utils.Updater;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class EasyPrefix extends JavaPlugin {
    private static EasyPrefix instance;
    private ArrayList<User> users;
    private Plugin plugin;
    private Database database;
    private GroupHandler groupHandler;
    private Updater updater;
    private FileManager fileManager;
    private CommandHandler commandHandler;
    private ExpansionManager expansionManager;

    public static EasyPrefix getInstance() {
        return instance;
    }

    public ExpansionManager getExpansionManager() {
        return expansionManager;
    }

    public void onDisable() {
        if (getSqlDatabase() != null) getSqlDatabase().close();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null) player.closeInventory();
        }
    }

    public void onEnable() {
        instance = this;
        this.plugin = this;
        this.users = new ArrayList<>();
        this.fileManager = new FileManager(this);
        ConfigData cfg = this.fileManager.getConfig();
        Messages.load();
        if (cfg.getBoolean(ConfigData.ConfigKeys.USE_SQL)) {
            this.database = new Database(this);
        }
        this.groupHandler = new GroupHandler(this);
        groupHandler.load();
        this.commandHandler = new CommandHandler(this);
        registerEvents();
        if (!ConfigKeys.ENABLED.toBoolean()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.expansionManager = new ExpansionManager(this);
        this.updater = new Updater(this);
        hookMetrics();
        Messages.log("§bPlugin has been enabled! §bVersion: §7" + getDescription().getVersion());
        Messages.log("§bIf you like the plugin or you have suggestions, please write a review on spigotmc.org!");
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (formatChat() && (Bukkit.getPluginManager().isPluginEnabled("EssentialsChat") || Bukkit.getPluginManager().isPluginEnabled("MultiChat"))) {
                Messages.log("§c--------------------------------------");
                Messages.log("§cYou are using a different chat management plugin. To avoid errors, please set 'handle-chat' to false in config.yml");
                Messages.log("§c--------------------------------------");
            }
        }, 20 * 3);
    }

    public boolean formatChat() {
        return ConfigKeys.HANDLE_CHAT.toBoolean();
    }

    public GroupHandler getGroupHandler() {
        return groupHandler;
    }

    public User getUser(Player player) {
        for (User user : users) {
            if (user.getPlayer().getName().equalsIgnoreCase(player.getName())) {
                return user;
            }
        }
        User user = new User(player);
        user.login();
        users.add(user);
        return user;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public Database getSqlDatabase() {
        return database;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Updater getUpdater() {
        return updater;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void unloadUser(final Player player) {
        users.removeIf(crntUser -> crntUser.getPlayer().getName().equals(player.getName()));
    }

    public void reload() {
        this.fileManager = new FileManager(this);
        if (this.fileManager.getConfig().getBoolean(ConfigData.ConfigKeys.USE_SQL) && this.database != null) {
            try {
                getSqlDatabase().getConnection().close();
            } catch (SQLException ignored) {
            }
            this.database = new Database(this);
        } else this.database = null;
        this.commandHandler = new CommandHandler(this);
        Messages.load();
        RainbowEffect.getRainbowColors().clear();
        this.groupHandler = new GroupHandler(this);
        this.groupHandler.load();
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new JoinListener(this), this);
        pluginManager.registerEvents(new QuitListener(this), this);
    }

    private void hookMetrics() {
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("placeholderapi", () -> (expansionManager.isUsingPapi()) ? "installed" : "not installed"));
        metrics.addCustomChart(new Metrics.SimplePie("lang", Messages::getLanguage));
        metrics.addCustomChart(new Metrics.SimplePie("sql", () -> (getSqlDatabase() != null) ? "true" : "false"));
        metrics.addCustomChart(new Metrics.SimplePie("chat", () -> (formatChat()) ? "true" : "false"));
        metrics.addCustomChart(new Metrics.SimplePie("genders", () -> (ConfigKeys.USE_GENDER.toBoolean()) ? "true" : "false"));
    }

}
