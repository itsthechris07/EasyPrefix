package com.christian34.easyprefix;

import com.christian34.easyprefix.commands.CommandManager;
import com.christian34.easyprefix.extensions.ExpansionManager;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.listeners.DisplayManager;
import com.christian34.easyprefix.listeners.JoinListener;
import com.christian34.easyprefix.listeners.QuitListener;
import com.christian34.easyprefix.sql.database.LocalDatabase;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.SharedConfig;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.user.CustomLayout;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserData;
import com.christian34.easyprefix.utils.*;
import com.christian34.easyprefix.utils.textinput.FoliaChatInput;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EasyPrefix 2026.
 * <p>
 * Main class of the plugin: loads files, storage, groups, colors and hooks, and gives access to the users.
 *
 * @author Christian34
 */
public class EasyPrefix extends JavaPlugin {
    private static final Set<String> DEFAULT_COLORS = Set.of("black", "dark_blue", "dark_green", "dark_aqua", "dark_red",
            "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white", "rainbow", "sunset", "ocean", "toxic", "candy", "fire", "ice", "pride");
    private static final List<String> RELATED_PLUGINS = List.of("LuckPerms", "Vault", "PlaceholderAPI", "EssentialsChat",
            "MultiChat", "TAB", "DiscordSRV", "ChatControl", "VentureChat", "CMI", "NametagEdit", "Geyser-Spigot", "ViaVersion");
    private static EasyPrefix instance = null;
    private volatile Collection<Color> colors;
    private volatile Collection<Decoration> decorations;
    private SQLDatabase sqlDatabase = null;
    private LocalDatabase localDatabase = null;
    private Map<UUID, User> users;
    /**
     * the data of players who are logging in, loaded on the login thread and taken over on the join
     */
    private final Map<UUID, PreloadedData> preloaded = new ConcurrentHashMap<>();
    private Plugin plugin;
    private GroupHandler groupHandler;
    private FileManager fileManager;
    private ExpansionManager expansionManager;
    private DisplayManager displayManager;
    private StorageType storageType;
    private Updater updater;
    private CommandManager commandManager;
    private volatile MiniMessage miniMessage;

    public static EasyPrefix getInstance() {
        return instance;
    }

    /**
     * -Deasyprefix.offline=true disables everything that sends data to the internet (bStats, update check),
     * used by the tests so they don't report to the live statistics
     */
    public static boolean isOffline() {
        return Boolean.getBoolean("easyprefix.offline");
    }

    private static String bucket(int amount) {
        if (amount <= 5) return Integer.toString(amount);
        if (amount <= 10) return "6-10";
        if (amount <= 25) return "11-25";
        if (amount <= 50) return "26-50";
        return "50+";
    }

    public Collection<Decoration> getDecorations() {
        return decorations;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public Collection<Color> getColors() {
        return colors;
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

    public DisplayManager getDisplayManager() {
        return displayManager;
    }

    public ExpansionManager getExpansionManager() {
        return expansionManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * only used by the tests, which register the commands at a cloud manager without a server behind it
     */
    void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void onEnable() {
        EasyPrefix.instance = this;

        this.plugin = this;
        Debug.init(this);
        this.users = new ConcurrentHashMap<>();
        this.fileManager = new FileManager(this);
        this.displayManager = new DisplayManager(this);

        if (!getConfigData().getBoolean(ConfigData.Keys.ENABLED)) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (getConfigData().getBoolean(ConfigData.Keys.SQL_ENABLED)) {
            this.sqlDatabase = new SQLDatabase(this);
            this.storageType = StorageType.SQL;
            if (!this.sqlDatabase.connect()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            this.sqlDatabase.startSynchronizer(this);
            SharedConfig.load(this);
        } else {
            this.localDatabase = new LocalDatabase();
            this.storageType = StorageType.LOCAL;
            if (!this.localDatabase.connect()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }

        loadFormats();
        this.updater = new Updater(this);
        registerEvents();
        this.expansionManager = new ExpansionManager(this);
        hookMetrics();
        Debug.log("If you like the plugin or you have suggestions, please write a review on spigotmc.org!");
        PluginManager pluginManager = Bukkit.getPluginManager();
        TaskManager.globalLater(() -> {
            if (formatChat() && (pluginManager.isPluginEnabled("EssentialsChat") || pluginManager.isPluginEnabled("MultiChat"))) {
                Debug.warn("§c--------------------------------------");
                Debug.warn("§cYou are using a different chat management plugin. To avoid issues, please set 'handle-chat' in config.yml to false");
                Debug.warn("§c--------------------------------------");
            }
        }, 20 * 3);

        try {
            this.commandManager = new CommandManager(this);
        } catch (Exception | LinkageError ex) {
            // cloud reflects into server internals, which may break on server updates - the rest of the plugin keeps working
            Debug.warn("Couldn't register the commands of EasyPrefix: " + ex);
        }

        this.groupHandler = new GroupHandler(this);
        try {
            groupHandler.load();
        } catch (Exception ex) {
            Debug.handleException(ex);
        }
        displayManager.start();
    }

    /**
     * loads the chat colors and formattings from config.yml (again, e.g. after another server changed them)
     */
    public void loadFormats() {
        TagResolver.Builder tagResolverBuilder = TagResolver.builder();

        List<Color> colors = new ArrayList<>();
        ConfigurationSection colorsSection = this.getConfigData().getSection("chat.colors");
        if (colorsSection != null) {
            for (String name : colorsSection.getKeys(false)) {
                try {
                    Color color = new Color(name);
                    colors.add(color);
                    tagResolverBuilder.resolver(color.tagResolver());
                } catch (RuntimeException ex) {
                    Debug.warn(String.format("Couldn't load color '%s' from config.yml: %s", name, ex.getMessage()));
                }
            }
        }

        List<Decoration> decorations = new ArrayList<>();
        ConfigurationSection decorationsSection = this.getConfigData().getSection("chat.decorations");
        if (decorationsSection != null) {
            for (String name : decorationsSection.getKeys(false)) {
                try {
                    Decoration decoration = new Decoration(name);
                    decorations.add(decoration);
                    tagResolverBuilder.resolver(decoration.tagResolver());
                } catch (RuntimeException ex) {
                    Debug.warn(String.format("Couldn't load decoration '%s' from config.yml: %s", name, ex.getMessage()));
                }
            }
        }
        tagResolverBuilder.resolver(CustomLayout.PERCENT);
        tagResolverBuilder.resolver(StandardTags.defaults());

        this.colors = colors;
        this.decorations = decorations;
        this.miniMessage = MiniMessage.builder().tags(tagResolverBuilder.build()).build();
    }

    public void onDisable() {
        if (displayManager != null) displayManager.stop();
        if (sqlDatabase != null) {
            this.sqlDatabase.close();
        }
        if (localDatabase != null) {
            this.localDatabase.close();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            // on Folia other regions own the players
            if (player != null && Bukkit.isOwnedByCurrentRegion(player)) player.closeInventory();
        }
    }

    public boolean formatChat() {
        return getConfigData().getBoolean(ConfigData.Keys.HANDLE_CHAT);
    }

    public GroupHandler getGroupHandler() {
        return groupHandler;
    }

    /**
     * loads the user of an offline player - it is not cached, as it is only needed once (e.g. by a command)
     */
    @Nullable
    public User getUser(OfflinePlayer player) {
        User user = new User(player);
        try {
            user.login();
            return user;
        } catch (Exception ex) {
            Debug.handleException(ex);
        }
        return null;
    }

    @NotNull
    public User getUser(Player player) {
        User user = users.get(player.getUniqueId());
        if (user != null) return user;
        PreloadedData data = preloaded.remove(player.getUniqueId());
        user = data != null ? new User(player, data.userData()) : new User(player);
        try {
            user.login();
        } catch (Exception ex) {
            Debug.handleException(ex);
            return user;
        }
        // the chat thread and the main thread may load the same user at the same time
        User cached = users.putIfAbsent(player.getUniqueId(), user);
        return cached != null ? cached : user;
    }

    @Nullable
    public String setPlaceholders(@NotNull User user, @Nullable String text) {
        if (text == null) return null;
        String subPrefix = "", subSuffix = "";

        Subgroup subgroup = user.getSubgroup();
        if (subgroup != null) {
            subPrefix = Optional.ofNullable(subgroup.getPrefix()).orElse("");
            subSuffix = Optional.ofNullable(subgroup.getSuffix()).orElse("");
        }

        String prefix = Optional.ofNullable(user.getPrefix()).orElse("");
        String suffix = Optional.ofNullable(user.getSuffix()).orElse("");

        // offline players (e.g. requested by placeholderapi or vault) have no display name
        String name = user.getPlayer() != null
                ? LegacyComponentSerializer.legacySection().serialize(user.getPlayer().displayName()) : user.getName();

        text = text.replace("%ep_user_prefix%", prefix).replace("%ep_user_suffix%", suffix).replace("%ep_user_group%", user.getGroup().getName()).replace("%ep_user_subgroup_prefix%", subPrefix).replace("%ep_tag_prefix%", subPrefix).replace("%ep_user_subgroup_suffix%", subSuffix).replace("%ep_tag_suffix%", subSuffix).replace("%ep_user_tag%", subgroup != null ? subgroup.getName() : "-").replace("%player%", name);

        if (expansionManager.isUsingPapi()) {
            text = expansionManager.setPlaceholders(user.getOfflinePlayer(), text);
        }
        text = text.replace("%player%", name);
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

    /**
     * loads the data of a user again, if they are online (e.g. changed by another server)
     */
    public void reloadUser(UUID uniqueId) {
        User user = users.get(uniqueId);
        if (user == null) return;
        try {
            user.login();
        } catch (Exception ex) {
            Debug.handleException(ex);
        }
    }

    public Collection<User> getUsers() {
        return users.values();
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void unloadUser(final Player player) {
        users.remove(player.getUniqueId());
        preloaded.remove(player.getUniqueId());
    }

    /**
     * Loads the data of a player who is logging in, so the join does not wait for the database on the main thread.
     * Must not be called on the main thread.
     */
    public void preloadUser(UUID uniqueId, String name) {
        // players whose login was denied after this never join, their data is dropped with the next login
        long now = System.currentTimeMillis();
        preloaded.values().removeIf(data -> now - data.time() > 60_000);
        UserData userData = new UserData(uniqueId);
        userData.loadData(name);
        preloaded.put(uniqueId, new PreloadedData(userData, now));
    }

    private record PreloadedData(UserData userData, long time) {
    }

    public void reload() {
        Debug.recordAction("Reloading Plugin");
        this.fileManager = new FileManager(this);
        this.updater.check();
        boolean sqlEnabled = getConfigData().getBoolean(ConfigData.Keys.SQL_ENABLED);
        if (sqlEnabled != (this.storageType == StorageType.SQL)) {
            Debug.warn("************************************************************");
            Debug.warn("* WARNING: You MUST restart the server to " + (sqlEnabled ? "enable" : "disable") + " sql!");
            Debug.warn("* stopping plugin...");
            Debug.warn("************************************************************");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (sqlEnabled) {
            this.sqlDatabase.close();
            this.sqlDatabase = new SQLDatabase(this);
            if (!this.sqlDatabase.connect()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            this.sqlDatabase.startSynchronizer(this);
            // config.yml was edited by hand: its shared settings go to the other servers
            SharedConfig.upload(this);
        } else {
            this.localDatabase.close();
            this.localDatabase.connect();
        }
        loadFormats();
        // groups and users keep the files they were loaded from, so both are loaded again
        this.groupHandler = new GroupHandler(this);
        this.groupHandler.load();
        this.users.clear();
        // the listeners stay registered: they read the config on every event, and HandlerList.unregisterAll(this) would
        // also remove the listeners of cloud and InventoryGui, which don't register themselves again
        displayManager.start();
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(updater, this);
        pluginManager.registerEvents(new JoinListener(this), this);
        pluginManager.registerEvents(new QuitListener(this), this);
        pluginManager.registerEvents(displayManager, this);
        if (TaskManager.isFolia()) pluginManager.registerEvents(new FoliaChatInput.Listener(), this);
    }

    /**
     * anonymous statistics on bstats.org - custom charts only show up after they have been added to the plugin page
     * there with the same idJah
     */
    private void hookMetrics() {
        if (isOffline()) return;
        Metrics metrics = new Metrics(this, 9682);
        // existing charts
        metrics.addCustomChart(new SimplePie("placeholderapi", () -> (expansionManager.isUsingPapi()) ? "installed" : "not installed"));
        metrics.addCustomChart(new SimplePie("storage", () -> storageType.name().toLowerCase()));
        metrics.addCustomChart(new SimplePie("chat", () -> Boolean.toString(formatChat())));
        metrics.addCustomChart(new SimplePie("custom_layout", () -> enabled(ConfigData.Keys.CUSTOM_LAYOUT)));

        // features
        metrics.addCustomChart(new SimplePie("vault", () -> !getServer().getPluginManager().isPluginEnabled("Vault") ? "not installed"
                : expansionManager.isUsingVaultUnlocked() ? "VaultUnlocked" : "installed"));
        metrics.addCustomChart(new SimplePie("tags", () -> enabled(ConfigData.Keys.USE_TAGS)));
        metrics.addCustomChart(new SimplePie("join_quit_messages", () -> !getConfigData().getBoolean(ConfigData.Keys.USE_JOIN_QUIT)
                ? "disabled" : getConfigData().getBoolean(ConfigData.Keys.HIDE_JOIN_QUIT) ? "hidden" : "enabled"));
        metrics.addCustomChart(new SimplePie("tab_list", () -> enabled(ConfigData.Keys.DISPLAY_TAB_LIST)));
        metrics.addCustomChart(new SimplePie("sort_tab_list", () -> enabled(ConfigData.Keys.DISPLAY_SORT_TAB_LIST)));
        metrics.addCustomChart(new SimplePie("name_tags", () -> enabled(ConfigData.Keys.DISPLAY_NAME_TAGS)));
        metrics.addCustomChart(new SimplePie("color_icon", () -> ColorIcon.fromConfig().getConfigValue()));

        // setup size
        metrics.addCustomChart(new SimplePie("groups_amount", () -> bucket(groupHandler == null ? 0 : groupHandler.getGroups().size())));
        metrics.addCustomChart(new SimplePie("tags_amount", () -> bucket(groupHandler == null ? 0 : groupHandler.getSubgroups().size())));
        metrics.addCustomChart(new SimplePie("colors_amount", () -> bucket(colors.size())));
        metrics.addCustomChart(new SimplePie("decorations_amount", () -> bucket(decorations.size())));
        metrics.addCustomChart(new SimplePie("custom_colors", () -> colors.stream().anyMatch(color -> !DEFAULT_COLORS.contains(color.getName().toLowerCase(Locale.ROOT))) ? "yes" : "no"));

        // usage by the players that are online
        metrics.addCustomChart(new AdvancedPie("chat_colors_used", () -> countUsers(user -> user.getColor() == null ? null : user.getColor().getName())));
        metrics.addCustomChart(new AdvancedPie("decorations_used", () -> countUsers(user -> user.getDecoration() == null ? null : user.getDecoration().getName())));
        metrics.addCustomChart(new SimplePie("players_with_tag", () -> percentage(user -> user.getSubgroup() != null)));
        metrics.addCustomChart(new SimplePie("players_with_custom_layout", () -> percentage(user -> user.hasCustomPrefix() || user.hasCustomSuffix())));

        // other plugins that matter for the compatibility
        metrics.addCustomChart(new AdvancedPie("related_plugins", () -> {
            Map<String, Integer> plugins = new HashMap<>();
            for (String name : RELATED_PLUGINS) {
                if (getServer().getPluginManager().isPluginEnabled(name)) plugins.put(name, 1);
            }
            return plugins;
        }));
    }

    private String enabled(String key) {
        return getConfigData().getBoolean(key) ? "enabled" : "disabled";
    }

    private Map<String, Integer> countUsers(java.util.function.Function<User, String> value) {
        Map<String, Integer> counts = new HashMap<>();
        for (User user : getUsers()) {
            String key = value.apply(user);
            if (key != null) counts.merge(key, 1, Integer::sum);
        }
        return counts;
    }

    /**
     * @return the share of online players, rounded to steps of 10 percent ("none" if nobody is online)
     */
    private String percentage(java.util.function.Predicate<User> condition) {
        Collection<User> users = getUsers();
        if (users.isEmpty()) return "none";
        long matching = users.stream().filter(condition).count();
        return (Math.round(matching * 10.0 / users.size()) * 10) + "%";
    }

}
