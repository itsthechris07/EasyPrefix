package com.christian34.easyprefix;

import com.christian34.easyprefix.commands.CommandHandler;
import com.christian34.easyprefix.extensions.ExpansionManager;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.listeners.JoinListener;
import com.christian34.easyprefix.listeners.QuitListener;
import com.christian34.easyprefix.sql.database.LocalDatabase;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.RainbowEffect;
import com.christian34.easyprefix.utils.Updater;
import org.apache.commons.lang.Validate;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * EasyPrefix 2021.
 *
 * @author Christian34
 */
public class EasyPrefix extends JavaPlugin {
    private static EasyPrefix instance = null;
    private SQLDatabase sqlDatabase = null;
    private LocalDatabase localDatabase = null;
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

    public LocalDatabase getLocalDatabase() {
        return localDatabase;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public SQLDatabase getSqlDatabase() {
        return sqlDatabase;
    }

    public ConfigData getConfigData() {
        return getFileManager().getConfig();
    }

    public void onEnable() {
        EasyPrefix.instance = this;
        this.plugin = this;
        this.debug = new Debug(this);
        this.users = Collections.synchronizedSet(new HashSet<>());
        this.fileManager = new FileManager(this);
        if (!debug.getClientID().equals(getConfigData().getString(ConfigData.Keys.CLIENT_ID))) {
            getConfigData().save(ConfigData.Keys.CLIENT_ID, debug.getClientID());
        }

        if (getConfigData().getBoolean(ConfigData.Keys.SQL_ENABLED)) {
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
        if (!getConfigData().getBoolean(ConfigData.Keys.ENABLED)) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.expansionManager = new ExpansionManager(this);
        this.updater = new Updater(this);
        hookMetrics();
        Debug.log("§bPlugin has been enabled! §bVersion: §7" + getDescription().getVersion());
        Debug.log("§bIf you like the plugin or you have suggestions, please write a review on spigotmc.org!");
        Debug.log("This software uses Sentry for anonymous user statistics. License: https://github.com/getsentry/sentry/blob/master/LICENSE");
        PluginManager pluginManager = Bukkit.getPluginManager();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (formatChat() && (pluginManager.isPluginEnabled("EssentialsChat")
                    || pluginManager.isPluginEnabled("MultiChat"))) {
                Debug.warn("§c--------------------------------------");
                Debug.warn("§cYou are using a different chat management plugin. To avoid issues, " +
                        "please set 'handle-chat' in config.yml to false");
                Debug.warn("§c--------------------------------------");
            }
        }, 20 * 3);
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

    public boolean formatChat() {
        return getConfigData().getBoolean(ConfigData.Keys.HANDLE_CHAT);
    }

    public GroupHandler getGroupHandler() {
        return groupHandler;
    }

    @NotNull
    public synchronized User getUser(Player player) {
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

    @Nullable
    public String setPlaceholders(@NotNull User user, @Nullable String text) {
        if (text == null) return null;
        Validate.notNull(user);
        String subPrefix = "", subSuffix = "";

        Subgroup subgroup = user.getSubgroup();
        if (subgroup != null) {
            subPrefix = Optional.ofNullable(subgroup.getPrefix(user.getGenderType())).orElse("");
            subSuffix = Optional.ofNullable(subgroup.getSuffix(user.getGenderType())).orElse("");
        }

        String prefix = Optional.ofNullable(user.getPrefix()).orElse("");
        String suffix = Optional.ofNullable(user.getSuffix()).orElse("");

        text = text
                .replace("%ep_user_prefix%", prefix)
                .replace("%ep_user_suffix%", suffix)
                .replace("%ep_user_group%", user.getGroup().getName())
                .replace("%ep_user_subgroup_prefix%", subPrefix)
                .replace("%ep_tag_prefix%", subPrefix)
                .replace("%ep_user_subgroup_suffix%", subSuffix)
                .replace("%ep_tag_suffix%", subSuffix)
                .replace("%player%", user.getPlayer().getDisplayName());

        if (expansionManager.isUsingPapi()) {
            text = expansionManager.setPlaceholders(user.getPlayer(), text);
        }
        text = text.replace("%player%", user.getPlayer().getDisplayName());
        return text;
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
        if (getConfigData().getBoolean(ConfigData.Keys.SQL_ENABLED) && this.storageType == StorageType.LOCAL) {
            Debug.warn("************************************************************");
            Debug.warn("* WARNING: You MUST restart the server to enable sql!");
            Debug.warn("* stopping plugin...");
            Debug.warn("************************************************************");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (getConfigData().getBoolean(ConfigData.Keys.SQL_ENABLED)) {
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
        metrics.addCustomChart(new SimplePie("placeholderapi",
                () -> (expansionManager.isUsingPapi()) ? "installed" : "not installed"));
        metrics.addCustomChart(new SimplePie("storage",
                () -> storageType.name().toLowerCase()));
        metrics.addCustomChart(new SimplePie("chat",
                () -> (formatChat()) ? "true" : "false"));
        metrics.addCustomChart(new SimplePie("genders",
                () -> (instance.getConfigData().getBoolean(ConfigData.Keys.USE_GENDER)) ? "enabled" : "disabled"));
        metrics.addCustomChart(new SimplePie("custom_layout",
                () -> (getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT)) ? "enabled" : "disabled"));
    }

}
