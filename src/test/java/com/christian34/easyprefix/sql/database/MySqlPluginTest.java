package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static com.christian34.easyprefix.sql.database.TestMySql.queryString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * The whole plugin on MySQL. Only runs with a test database, see {@link TestMySql}.
 *
 * @author Christian34
 */
@EnabledIfSystemProperty(named = TestMySql.PROPERTY, matches = ".+")
class MySqlPluginTest extends PluginTestBase {

    @Override
    protected void beforePluginLoad() {
        try {
            TestMySql.clearTables();
            YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration(resource("/plugin.yml"));
            File folder = server.getPluginManager().createTemporaryDirectory("EasyPrefix-" + pluginYml.getString("version"));
            YamlConfiguration config = YamlConfiguration.loadConfiguration(resource("/config.yml"));
            TestMySql.Settings settings = TestMySql.settings();
            config.set("config.sql.enabled", true);
            config.set("config.sql.host", settings.host());
            config.set("config.sql.port", settings.port());
            config.set("config.sql.database", settings.database());
            config.set("config.sql.username", settings.username());
            config.set("config.sql.password", settings.password());
            config.set("config.sql.table-prefix", TestMySql.PREFIX);
            config.save(new File(folder, "config.yml"));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private InputStreamReader resource(String name) {
        return new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(name)), StandardCharsets.UTF_8);
    }

    /**
     * the schema is migrated by the first test only, the others just clear the rows
     */
    @BeforeAll
    @AfterAll
    static void dropTables() throws SQLException {
        TestMySql.dropTables();
    }

    private GroupHandler groups() {
        return plugin.getGroupHandler();
    }

    /**
     * the async tasks of the plugin run on other threads
     */
    private static void await(BooleanSupplier condition) throws InterruptedException {
        long end = System.currentTimeMillis() + 10_000;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > end) fail("timed out");
            Thread.sleep(20);
        }
    }

    /**
     * a message of another server with the same database
     */
    private static void otherServer(SQLSynchronizer.Type type, @Nullable String target) throws SQLException {
        TestMySql.execute("INSERT INTO `%p%messages` (`server`, `type`, `target`) VALUES ('" + UUID.randomUUID() + "', '"
                + type + "', " + (target == null ? "NULL" : "'" + target + "'") + ")");
    }

    private static int countMessages(String condition) {
        try {
            return Integer.parseInt(queryString("SELECT COUNT(*) FROM `%p%messages` WHERE " + condition));
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void poll() {
        server.getScheduler().performTicks(SQLSynchronizer.POLL_TICKS);
    }

    @Test
    void createsTheDefaultGroup() throws SQLException {
        assertEquals(StorageType.SQL, plugin.getStorageType());
        assertEquals("0", queryString("SELECT `priority` FROM `%p%groups` WHERE `group` = 'default'"));
        assertEquals(String.join("\n", Group.DEFAULT_HOVER), groups().getGroup("default").getOwnHover() == null
                ? null : String.join("\n", groups().getGroup("default").getOwnHover()));
    }

    /**
     * a new database gets the example groups and tags of groups.yml
     */
    @Test
    void loadsTheExamples() throws SQLException {
        assertEquals(100, groups().getGroup("Owner").getPriority());
        assertEquals("rainbow", groups().getGroup("Vip").getColor().getName());
        assertEquals("&6VIP", queryString("SELECT `prefix` FROM `%p%subgroups` WHERE `group` = 'Vip'"));
    }

    @Test
    void storesGroups() {
        assertTrue(groups().createGroup("Tester"));
        Group admin = groups().getGroup("Tester");
        // MiniMessage prefixes get long, and emojis need utf8mb4
        String prefix = "<gradient:#ff5555:#ffaa00><bold>Administrator</bold></gradient> <dark_gray>|</dark_gray> 🔥 ".repeat(3);
        admin.setPrefix(prefix);
        admin.setPriority(50);
        admin.setHover(List.of("<red>Admin", "", "🔥 Team"));

        groups().load();
        Group loaded = groups().getGroup("Tester");
        assertEquals(prefix, loaded.getPrefix());
        assertEquals(50, loaded.getPriority());
        assertEquals(List.of("<red>Admin", "", "🔥 Team"), loaded.getOwnHover());

        loaded.delete();
        groups().load();
        assertFalse(groups().isGroup("Tester"));
    }

    @Test
    void storesTags() {
        assertTrue(groups().createSubgroup("Builder"));
        Objects.requireNonNull(groups().getSubgroup("Builder")).setPrefix("<gold>Builder 🔨 ");
        groups().load();
        assertEquals("<gold>Builder 🔨 ", Objects.requireNonNull(groups().getSubgroup("Builder")).getPrefix());
    }

    @Test
    void storesUserData() throws SQLException {
        groups().createGroup("Tester");
        PlayerMock player = addPlayer("Steve");
        // a new group resets the own prefix
        user(player).setGroup(groups().getGroup("Tester"), true);
        user(player).setPrefix("<red>Steve 🔥 ");

        String uuid = player.getUniqueId().toString();
        assertEquals("Steve", queryString("SELECT `username` FROM `%p%users` WHERE `uuid` = '" + uuid + "'"));
        assertEquals("<red>Steve 🔥 ", queryString("SELECT `custom_prefix` FROM `%p%users` WHERE `uuid` = '" + uuid + "'"));

        plugin.getUsers().clear();
        assertEquals("Tester", user(player).getGroup().getName());
    }

    /**
     * a change on another server with the same database
     */
    @Test
    void syncsChangesOfOtherServers() throws Exception {
        TestMySql.execute("INSERT INTO `%p%groups` (`group`, `prefix`) VALUES ('Remote', '<blue>Remote ')");
        otherServer(SQLSynchronizer.Type.GROUPS, null);
        // a change of this server at the same time does not hide the one of the other server
        groups().createGroup("Local");
        poll();
        await(() -> groups().isGroup("Remote"));
        assertEquals("<blue>Remote ", groups().getGroup("Remote").getPrefix());
    }

    @Test
    void reloadsOnlyTheChangedUser() throws Exception {
        PlayerMock steve = addPlayer("Steve", "EasyPrefix.custom.prefix");
        PlayerMock alex = addPlayer("Alex", "EasyPrefix.custom.prefix");
        TestMySql.execute("UPDATE `%p%users` SET `custom_prefix` = '<red>Remote '");
        otherServer(SQLSynchronizer.Type.USER, steve.getUniqueId().toString());
        poll();
        await(() -> "<red>Remote ".equals(user(steve).getPrefix()));
        assertNotEquals("<red>Remote ", user(alex).getPrefix());
    }

    /**
     * the ids are counted up when an insert starts, so a message can be committed after one with a higher id
     */
    @Test
    void readsMessagesThatAreCommittedLate() throws Exception {
        TestMySql.execute("INSERT INTO `%p%messages` (`id`, `server`, `type`) VALUES (1000, 'other', 'USER')");
        poll();
        TestMySql.execute("INSERT INTO `%p%groups` (`group`) VALUES ('Late')");
        TestMySql.execute("INSERT INTO `%p%messages` (`id`, `server`, `type`) VALUES (999, 'other', 'GROUPS')");
        poll();
        await(() -> groups().isGroup("Late"));
    }

    @Test
    void announcesChangesOfUsers() throws Exception {
        // e.g. an admin on this server changes the group of a player on another one
        PlayerMock player = addPlayer("Steve");
        user(player).setPrefix("<red>Steve ");
        user(player).setSuffix(" <gray>»");
        String uuid = player.getUniqueId().toString();
        server.getScheduler().performTicks(SQLSynchronizer.SEND_DELAY_TICKS + 1);
        await(() -> countMessages("`type` = 'USER' AND `target` = '" + uuid + "'") > 0);
        // changes at once are sent as one message
        assertEquals(1, countMessages("`type` = 'USER' AND `target` = '" + uuid + "'"));
    }

    @Test
    void uploadsTheLocalStorage() throws Exception {
        // groups.yml contains the example groups (Owner with priority 100 and an own hover) and tags
        LocalDatabase local = new LocalDatabase(new File(plugin.getDataFolder(), "storage.db"));
        assertTrue(local.connect());
        try (Connection connection = local.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO `users` (`uuid`, `username`, `group`, `subgroup`, `custom_prefix`, `chat_color`) VALUES ('00000000-0000-0000-0000-000000000001', 'Alex', 'owner', 'unknown', '<gold>Alex 🔥 ', 'red')");
        } finally {
            local.close();
        }

        execute(server.getConsoleSender(), "ep database upload");
        server.getScheduler().waitAsyncTasksFinished();
        assertTrue(messages(server.getConsoleSender()).stream().anyMatch(message -> message.matches(".*Uploaded \\d+ groups, \\d+ tags and 1 users!")));

        assertEquals("100", queryString("SELECT `priority` FROM `%p%groups` WHERE `group` = 'Owner'"));
        assertTrue(groups().isGroup("Owner"));
        assertEquals(List.of("&4&l%ep_user_display_name%", "&7Server owner", "", "&8» Click to write a message"),
                groups().getGroup("Owner").getOwnHover());
        String user = "WHERE `uuid` = '00000000-0000-0000-0000-000000000001'";
        // the group is written like in the database, the unknown tag is left out
        assertEquals("Owner", queryString("SELECT `group` FROM `%p%users` " + user));
        assertNull(queryString("SELECT `subgroup` FROM `%p%users` " + user));
        assertEquals("<gold>Alex 🔥 ", queryString("SELECT `custom_prefix` FROM `%p%users` " + user));

        // a second upload overwrites instead of failing on the existing rows
        execute(server.getConsoleSender(), "ep database upload");
        server.getScheduler().waitAsyncTasksFinished();
        assertContains(messages(server.getConsoleSender()), "Uploaded");
    }

    private static String sharedConfig() {
        try {
            return queryString("SELECT `option_value` FROM `%p%options` WHERE `option_name` = '" + SharedConfig.OPTION + "'");
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void setSharedConfig(String path, Object value) throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(sharedConfig());
        yaml.set(path, value);
        TestMySql.execute("UPDATE `%p%options` SET `option_value` = '" + yaml.saveToString().replace("'", "''")
                + "' WHERE `option_name` = '" + SharedConfig.OPTION + "'");
    }

    @Test
    void uploadsTheSharedSettingsToANewDatabase() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(sharedConfig());
        assertEquals("dd.MM.yyyy", yaml.getString(ConfigData.Keys.DATE_FORMAT));
        assertEquals("#000000", yaml.getString("chat.colors.black.hex"));
        // switches of single features and the connection stay local
        assertFalse(yaml.contains("sql"));
        assertFalse(yaml.contains(ConfigData.Keys.HANDLE_CHAT));
    }

    /**
     * the start of a server with an existing database
     */
    @Test
    void takesOverTheSharedSettings() throws Exception {
        setSharedConfig(ConfigData.Keys.DATE_FORMAT, "yyyy-MM-dd");
        setSharedConfig(ConfigData.Keys.HANDLE_CHAT, false);
        SharedConfig.load(plugin);

        assertEquals("yyyy-MM-dd", plugin.getConfigData().getString(ConfigData.Keys.DATE_FORMAT));
        assertTrue(plugin.getConfigData().getBoolean(ConfigData.Keys.HANDLE_CHAT), "local settings are not shared");
        // config.yml shows the settings that are used
        YamlConfiguration file = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        assertEquals("yyyy-MM-dd", file.getString("config." + ConfigData.Keys.DATE_FORMAT));
    }

    @Test
    void uploadsChangesOfTheSetup() throws Exception {
        plugin.getConfigData().save(ConfigData.Keys.COLOR_ICON, "dye");
        server.getScheduler().waitAsyncTasksFinished();
        await(() -> sharedConfig().contains("color-icon: dye"));
    }

    @Test
    void syncsSettingsOfOtherServers() throws Exception {
        setSharedConfig("chat.colors.salmon", Map.of("display-name", "Salmon", "hex", "#fa8072", "default", true));
        otherServer(SQLSynchronizer.Type.CONFIG, null);
        poll();
        await(() -> plugin.getColors().stream().anyMatch(color -> color.getName().equals("salmon")));
        assertTrue(plugin.getConfigData().getData().isConfigurationSection("config.chat.colors.salmon"));
    }

    @Test
    void downloadsIntoTheLocalStorage() throws Exception {
        groups().createGroup("Tester");
        groups().getGroup("Tester").setPriority(40);
        groups().getGroup("Tester").setHover(List.of("<green>Mod", "🔥"));
        // an example group, which is in the local groups.yml as well
        groups().getGroup("Owner").delete();
        addPlayer("Steve");

        execute(server.getConsoleSender(), "ep database migrate");
        server.getScheduler().waitAsyncTasksFinished();
        assertContains(messages(server.getConsoleSender()), "Migration has been completed");

        YamlConfiguration groupsYml = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "groups.yml"));
        assertEquals(40, groupsYml.getInt("groups.Tester.priority"));
        assertEquals(List.of("<green>Mod", "🔥"), groupsYml.getStringList("groups.Tester.hover"));
        assertFalse(groupsYml.isSet("groups.Owner"), "the local groups are replaced (they are in the backup)");

        LocalDatabase local = new LocalDatabase(new File(plugin.getDataFolder(), "storage.db"));
        assertTrue(local.connect());
        try {
            assertEquals("Steve", local.query("SELECT `username` FROM `users`", result -> result.next() ? result.getString(1) : null));
        } finally {
            local.close();
        }
    }

}
