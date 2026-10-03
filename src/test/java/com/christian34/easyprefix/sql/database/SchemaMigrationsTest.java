package com.christian34.easyprefix.sql.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Runs the migrations against a real SQLite file - no server needed.
 *
 * @author Christian34
 */
class SchemaMigrationsTest {
    @TempDir
    File folder;
    private LocalDatabase database;

    @BeforeEach
    void createDatabase() {
        this.database = new LocalDatabase(new File(folder, "storage.db"));
    }

    @AfterEach
    void closeDatabase() {
        database.close();
    }

    private void execute(String sql) throws SQLException {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private boolean tableExists(String table) throws SQLException {
        return database.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = '" + table + "'", ResultSet::next);
    }

    private List<String> columns(String table) throws SQLException {
        return database.query("PRAGMA table_info(`" + table + "`)", result -> {
            List<String> columns = new ArrayList<>();
            while (result.next()) columns.add(result.getString("name"));
            return columns;
        });
    }

    @Test
    void createsFreshDatabase() throws SQLException {
        assertTrue(database.connect());
        assertEquals(SchemaMigrations.latestVersion(), SchemaMigrations.currentVersion(database));
        assertTrue(columns("users").containsAll(List.of("uuid", "username", "group", "subgroup", "custom_prefix",
                "custom_prefix_update", "custom_suffix", "custom_suffix_update", "chat_color", "chat_formatting",
                "mentions_disabled")));
    }

    @Test
    void recordsEveryVersion() throws SQLException {
        database.connect();
        int versions = database.query("SELECT COUNT(*) FROM `schema_version`", result -> result.next() ? result.getInt(1) : 0);
        assertEquals(SchemaMigrations.MIGRATIONS.size(), versions);
    }

    @Test
    void migratesOnlyOnce() throws SQLException {
        assertTrue(database.connect());
        database.close();
        assertTrue(database.connect(), "a second start must not fail");
        SchemaMigrations.migrate(database);
        assertEquals(SchemaMigrations.latestVersion(), SchemaMigrations.currentVersion(database));
    }

    @Test
    void keepsDataOfOldVersions() throws SQLException {
        // storage.db of a version before the migrations: table without schema_version
        try (Connection connection = java.sql.DriverManager.getConnection("jdbc:sqlite:" + new File(folder, "storage.db").getAbsolutePath());
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `users` (`uuid` CHAR(36) NOT NULL, `username` VARCHAR(20) NULL DEFAULT NULL, `group` VARCHAR(64) NULL DEFAULT NULL, `force_group` BOOLEAN NULL DEFAULT NULL, `subgroup` VARCHAR(64) NULL DEFAULT NULL, `custom_prefix` VARCHAR(128) NULL DEFAULT NULL, `custom_prefix_update` TIMESTAMP NULL DEFAULT NULL, `custom_suffix` VARCHAR(128) NULL DEFAULT NULL, `custom_suffix_update` TIMESTAMP NULL DEFAULT NULL, `chat_color` CHAR(2) NULL DEFAULT NULL, `chat_formatting` CHAR(2) NULL DEFAULT NULL, PRIMARY KEY(`uuid`))");
            statement.executeUpdate("INSERT INTO `users` (`uuid`, `group`) VALUES ('0000', 'Admin')");
        }

        assertTrue(database.connect());
        assertEquals(SchemaMigrations.latestVersion(), SchemaMigrations.currentVersion(database));
        assertEquals("Admin", database.query("SELECT `group` FROM `users` WHERE `uuid` = '0000'", result -> result.next() ? result.getString(1) : null));
    }

    @Test
    void storesColorNames() throws SQLException {
        database.connect();
        execute("INSERT INTO `users` (`uuid`, `chat_color`, `chat_formatting`) VALUES ('0000', 'light_purple', 'strikethrough')");
        assertEquals("light_purple", database.query("SELECT `chat_color` FROM `users`", result -> result.next() ? result.getString(1) : null));
    }

    @Test
    void failedMigrationIsRolledBack() throws SQLException {
        database.connect();
        int version = SchemaMigrations.latestVersion();
        List<SchemaMigrations.Migration> migrations = new ArrayList<>(SchemaMigrations.MIGRATIONS);
        migrations.add(new SchemaMigrations.Migration(version + 1, "broken", List.of(), List.of(
                new SchemaMigrations.Step("CREATE TABLE `half_done` (`id` INT)", false),
                new SchemaMigrations.Step("THIS IS NOT SQL", false))));

        SQLException exception = assertThrows(SQLException.class, () -> SchemaMigrations.migrate(database, migrations));
        assertTrue(exception.getMessage().contains("version " + (version + 1)));
        assertEquals(version, SchemaMigrations.currentVersion(database), "the failed version must not be recorded");
        assertFalse(tableExists("half_done"), "the changes of the failed migration must be rolled back");
    }

    @Test
    void optionalStepsMayFail() throws SQLException {
        database.connect();
        int version = SchemaMigrations.latestVersion();
        List<SchemaMigrations.Migration> migrations = new ArrayList<>(SchemaMigrations.MIGRATIONS);
        migrations.add(new SchemaMigrations.Migration(version + 1, "optional", List.of(), List.of(
                new SchemaMigrations.Step("ALTER TABLE `users` ADD `uuid` CHAR(36)", true),
                new SchemaMigrations.Step("CREATE TABLE `extra` (`id` INT)", false))));

        SchemaMigrations.migrate(database, migrations);
        assertEquals(version + 1, SchemaMigrations.currentVersion(database));
        assertTrue(tableExists("extra"));
    }

    @Test
    void connectionsAreGivenBack() throws SQLException {
        database.connect();
        // the pool has a single connection - a leak would block the second query
        for (int i = 0; i < 50; i++) {
            try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
                statement.executeQuery().close();
            }
        }
        assertNotNull(database.query("SELECT 1", result -> result.next() ? result.getInt(1) : null));
    }

    @Test
    void closedDatabaseGivesClearError() {
        SQLException exception = assertThrows(SQLException.class, () -> database.getConnection());
        assertTrue(exception.getMessage().contains("not connected"));
    }

}
