package com.christian34.easyprefix;

import com.christian34.easyprefix.commands.CommandHandler;
import com.christian34.easyprefix.extensions.ExpansionManager;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.listeners.JoinListener;
import com.christian34.easyprefix.listeners.QuitListener;
import com.christian34.easyprefix.sql.database.LocalDatabase;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.sql.migrate.DataMigration;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.RainbowEffect;
import com.christian34.easyprefix.utils.Updater;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class EasyPrefix extends JavaPlugin {
    private static EasyPrefix instance = null;
    private SQLDatabase sqlDatabase = null;
    private LocalDatabase localDatabase = null;
    private DataMigration dataMigration = null;
    private Set<User> users;
    private Plugin plugin;
    private GroupHandler groupHandler;
    private FileManager fileManager;
    private ExpansionManager expansionManager;
    private StorageType storageType;
    private Updater updater;
    private CommandHandler commandHandler;
    @SuppressWarnings("FieldCanBeLocal")
    private Debug debug;

    public static EasyPrefix getInstance() {
        return instance;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public DataMigration getDataMigration() {
        if (dataMigration == null) {
            this.dataMigration = new DataMigration(this);
        }
        return dataMigration;
    }

    public LocalDatabase getLocalDatabase() {
        return localDatabase;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public SQLDatabase getSqlDatabase() {
        return sqlDatabase;
    }

    public ExpansionManager getExpansionManager() {
        return expansionManager;
    }

    public void onDisable() {
        if (sqlDatabase != null) {
            this.sqlDatabase.close();
        }
        if (localDatabase != null) {
            this.localDatabase.close();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null) player.closeInventory();
        }
    }

    public void onEnable() {
        EasyPrefix.instance = this;
        this.plugin = this;
        this.debug = new Debug(this);
        this.users = Collections.synchronizedSet(new HashSet<>());
        this.fileManager = new FileManager(this);
        if (!debug.getClientID().equals(ConfigKeys.CLIENT_ID.toString("id"))) {
            ConfigKeys.CLIENT_ID.set(debug.getClientID());
        }

        if (ConfigKeys.SQL_ENABLED.toBoolean()) {
            this.sqlDatabase = new SQLDatabase(this);
            this.storageType = StorageType.SQL;
            if (!this.sqlDatabase.connect()) {
                return;
            }
        } else {
            this.localDatabase = new LocalDatabase();
            this.storageType = StorageType.LOCAL;
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
        Debug.log("§bPlugin has been enabled! §bVersion: §7" + getDescription().getVersion());
        Debug.log("§bIf you like the plugin or you have suggestions, please write a review on spigotmc.org!");
        Debug.log("This software uses Sentry for anonymous user statistics. License: https://github.com/getsentry/sentry/blob/master/LICENSE");
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (formatChat() && (Bukkit.getPluginManager().isPluginEnabled("EssentialsChat") || Bukkit.getPluginManager().isPluginEnabled("MultiChat"))) {
                Debug.warn("§c--------------------------------------");
                Debug.warn("§cYou are using a different chat management plugin. To avoid issues, please set 'handle-chat' in config.yml to false");
                Debug.warn("§c--------------------------------------");
            }
        }, 20 * 3);

        if (expansionManager.isUsingPapi()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                @SuppressWarnings("unused") User user = getUser(player);
            }
        }
    }

    public boolean formatChat() {
        return ConfigKeys.HANDLE_CHAT.toBoolean();
    }

    public GroupHandler getGroupHandler() {
        return groupHandler;
    }

    @NotNull
    public User getUser(Player player) {
        User user = getUsers().stream().filter(usr -> usr.getPlayer().getName().equals(player.getName())).findAny().orElse(null);
        if (user == null) {
            user = new User(player);
            try {
                user.login();
                getUsers().add(user);
            } catch (Exception ex) {
                Debug.handleException(ex);
            }
        }
        return user;
    }

    public void reloadUsers() {
        for (User user : getUsers()) {
            try {
                user.login();
            } catch (Exception ex) {
                Debug.handleException(ex);
            }
        }
    }

    public Set<User> getUsers() {
        return users;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void unloadUser(final Player player) {
        getUsers().removeIf(user -> user.getPlayer().getName().equals(player.getName()));
    }

    public void reload() {
        Debug.recordAction("Reloading Plugin");
        this.fileManager = new FileManager(this);
        this.updater.check();
        if (storageType == StorageType.SQL) {
            this.sqlDatabase.close();
            this.sqlDatabase = new SQLDatabase(this);
            this.sqlDatabase.connect();
        } else {
            this.localDatabase.close();
            this.localDatabase.connect();
        }
        RainbowEffect.getRainbowColors().clear();
        this.groupHandler.load();
        reloadUsers();
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new JoinListener(this), this);
        pluginManager.registerEvents(new QuitListener(this), this);
    }

    private void hookMetrics() {
        Metrics metrics = new Metrics(this, 9682);
        metrics.addCustomChart(new Metrics.SimplePie("placeholderapi",
                () -> (expansionManager.isUsingPapi()) ? "installed" : "not installed"));
        metrics.addCustomChart(new Metrics.SimplePie("storage",
                () -> storageType.name().toLowerCase()));
        metrics.addCustomChart(new Metrics.SimplePie("chat",
                () -> (formatChat()) ? "true" : "false"));
        metrics.addCustomChart(new Metrics.SimplePie("genders",
                () -> (ConfigKeys.USE_GENDER.toBoolean()) ? "enabled" : "disabled"));
        metrics.addCustomChart(new Metrics.SimplePie("custom_layout",
                () -> (ConfigKeys.CUSTOM_LAYOUT.toBoolean()) ? "enabled" : "disabled"));
    }

}
