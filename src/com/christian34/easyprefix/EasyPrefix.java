package com.christian34.easyprefix;

import com.christian34.easyprefix.commands.CommandListener;
import com.christian34.easyprefix.commands.Command_Alias;
import com.christian34.easyprefix.commands.TabComplete;
import com.christian34.easyprefix.database.Database;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.listeners.JoinListener;
import com.christian34.easyprefix.listeners.QuitListener;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.placeholderapi.PlaceholderAPI;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Metrics;
import com.christian34.easyprefix.utils.RainbowEffect;
import com.christian34.easyprefix.utils.Updater;
import com.christian34.easyprefix.vault.VaultManager;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    public static EasyPrefix getInstance() {
        return instance;
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
        PlaceholderAPI.setEnabled(Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"));
        this.groupHandler = new GroupHandler(this);
        groupHandler.load();
        PluginCommand mainCmd = getCommand("EasyPrefix");
        assert mainCmd != null;
        mainCmd.setExecutor(new CommandListener(this));
        mainCmd.setTabCompleter(new TabComplete(this));
        registerEvents();
        if (!cfg.getBoolean(ConfigData.ConfigKeys.ENABLED)) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            VaultManager vaultManager = new VaultManager(this);
            vaultManager.hook();
        }

        if (cfg.getBoolean(ConfigData.ConfigKeys.CUSTOM_LAYOUT)) {
            Command_Alias cmd = new Command_Alias(this);

            PluginCommand prefixAlias = createPluginCommand(cfg.getString(ConfigData.ConfigKeys.PREFIX_ALIAS).replace("/", ""));
            assert prefixAlias != null;
            prefixAlias.setExecutor(cmd);
            prefixAlias.setTabCompleter(cmd);

            PluginCommand suffixAlias = createPluginCommand(cfg.getString(ConfigData.ConfigKeys.SUFFIX_ALIAS).replace("/", ""));
            assert suffixAlias != null;
            suffixAlias.setExecutor(cmd);
            suffixAlias.setTabCompleter(cmd);

            CommandMap commandMap = getCommandMapInstance();
            if (commandMap != null) {
                commandMap.register(plugin.getDescription().getName(), prefixAlias);
                commandMap.register(plugin.getDescription().getName(), suffixAlias);
            }
        }

        this.updater = new Updater(this);
        hookMetrics();
        Messages.log("§bPlugin has been enabled! §bVersion: §7" + getDescription().getVersion());
        Messages.log("§bIf you like the plugin or you have suggestions, please write a review on spigotmc.org!");
    }

    public boolean formatChat() {
        return this.fileManager.getConfig().getBoolean(ConfigData.ConfigKeys.HANDLE_CHAT);
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
        metrics.addCustomChart(new Metrics.SimplePie("placeholderapi", () -> (PlaceholderAPI.isEnabled()) ? "installed" : "not installed"));
        metrics.addCustomChart(new Metrics.SimplePie("lang", Messages::getLanguage));
        metrics.addCustomChart(new Metrics.SimplePie("sql", () -> (getSqlDatabase() != null) ? "true" : "false"));
        metrics.addCustomChart(new Metrics.SimplePie("chat", () -> (formatChat()) ? "true" : "false"));
        metrics.addCustomChart(new Metrics.SimplePie("genders", () -> (getFileManager().getConfig().getBoolean(ConfigData.ConfigKeys.USE_GENDER)) ? "true" : "false"));
    }

    private PluginCommand createPluginCommand(String name) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, this);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CommandMap getCommandMapInstance() {
        if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager spm = (SimplePluginManager) Bukkit.getPluginManager();
            try {
                Field field = FieldUtils.getDeclaredField(spm.getClass(), "commandMap", true);
                return (CommandMap) field.get(spm);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
