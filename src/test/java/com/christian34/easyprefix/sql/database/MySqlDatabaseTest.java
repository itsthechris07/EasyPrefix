package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.groups.Group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.sql.SQLException;
import java.util.List;

import static com.christian34.easyprefix.sql.database.TestMySql.PREFIX;
import static com.christian34.easyprefix.sql.database.TestMySql.execute;
import static com.christian34.easyprefix.sql.database.TestMySql.queryString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Only runs with a test database, see {@link TestMySql}. The tables are removed afterwards.
 *
 * @author Christian34
 */
@EnabledIfSystemProperty(named = TestMySql.PROPERTY, matches = ".+")
class MySqlDatabaseTest {
    private SQLDatabase database;

    @BeforeEach
    void connect() throws SQLException {
        TestMySql.dropTables();
        this.database = TestMySql.database();
        assertTrue(database.connect(), "couldn't connect to the test database");
    }

    @AfterEach
    void cleanUp() throws SQLException {
        database.close();
        TestMySql.dropTables();
    }

    @Test
    void createsAllTables() throws SQLException {
        assertEquals(SchemaMigrations.latestVersion(), SchemaMigrations.currentVersion(database));
        for (String table : List.of("users", "groups", "subgroups", "options", "messages")) {
            assertNotNull(queryString("SHOW TABLES LIKE '" + PREFIX + table + "'"), table + " is missing");
        }
    }

    @Test
    void storesColorNames() throws SQLException {
        execute("INSERT INTO `%p%users` (`uuid`, `chat_color`, `chat_formatting`) VALUES ('0000', 'light_purple', 'strikethrough')");
        assertEquals("light_purple", queryString("SELECT `chat_color` FROM `%p%users` WHERE `uuid` = '0000'"));
    }

    @Test
    void groupsHavePriority() throws SQLException {
        execute("INSERT INTO `%p%groups` (`group`) VALUES ('Tester')");
        assertEquals("1", queryString("SELECT `priority` FROM `%p%groups` WHERE `group` = 'Tester'"));
    }

    @Test
    void storesEmojisAndLongTexts() throws SQLException {
        String prefix = "<gradient:#ff5555:#ffaa00>🔥 ".repeat(20);
        execute("INSERT INTO `%p%groups` (`group`, `prefix`) VALUES ('Tester', '" + prefix + "')");
        assertEquals(prefix, queryString("SELECT `prefix` FROM `%p%groups` WHERE `group` = 'Tester'"));
    }

    /**
     * databases of older versions have foreign keys from the users to the groups, whose charset can't be changed
     * with the checks enabled
     */
    @Test
    void updatesTablesWithForeignKeys() throws SQLException {
        database.close();
        TestMySql.dropTables();
        execute("CREATE TABLE `%p%schema_version` (`version` INT NOT NULL PRIMARY KEY, `description` VARCHAR(255) NOT NULL, `installed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        for (SchemaMigrations.Migration migration : SchemaMigrations.MIGRATIONS.subList(0, 3)) {
            for (SchemaMigrations.Step step : migration.mysql()) {
                // the optional steps are for databases of older versions, which the create statements already cover
                if (!step.optional()) execute(step.sql());
            }
            execute("INSERT INTO `%p%schema_version` (`version`, `description`) VALUES (" + migration.version() + ", 'test')");
        }
        // named after the table, constraint names are unique per database
        execute("ALTER TABLE `%p%users` ADD CONSTRAINT `%p%group` FOREIGN KEY (`group`) REFERENCES `%p%groups`(`group`) ON DELETE SET NULL ON UPDATE CASCADE");
        execute("INSERT INTO `%p%groups` (`group`) VALUES ('Admin')");
        execute("INSERT INTO `%p%users` (`uuid`, `group`) VALUES ('0000', 'Admin')");

        assertTrue(database.connect());
        assertEquals(SchemaMigrations.latestVersion(), SchemaMigrations.currentVersion(database));
        assertEquals("utf8mb4", queryString("SELECT CHARACTER_SET_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '%p%users' AND COLUMN_NAME = 'group'"));
        // version 5 replaces the old constraint "group" - this one stands in for it (the name is taken by the tables
        // without prefix in the test database), so it is removed here
        execute("ALTER TABLE `%p%users` DROP FOREIGN KEY `%p%group`");
        // the foreign key still works
        execute("UPDATE `%p%groups` SET `group` = 'Administrator' WHERE `group` = 'Admin'");
        assertEquals("Administrator", queryString("SELECT `group` FROM `%p%users` WHERE `uuid` = '0000'"));
    }

    @Test
    void namesForeignKeysByPrefix() throws SQLException {
        for (String name : List.of("fk_group", "fk_subgroup")) {
            assertNotNull(queryString("SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = '%p%users' AND CONSTRAINT_NAME = '%p%" + name + "'"), name + " is missing");
        }
        execute("INSERT INTO `%p%groups` (`group`) VALUES ('Tester')");
        execute("INSERT INTO `%p%users` (`uuid`, `group`) VALUES ('0000', 'Tester')");
        execute("DELETE FROM `%p%groups` WHERE `group` = 'Tester'");
        assertNull(queryString("SELECT `group` FROM `%p%users` WHERE `uuid` = '0000'"));
    }

    /**
     * version 5 adds the foreign keys to databases that couldn't get them before, without losing the group of users
     * that is written in another case
     */
    @Test
    void fixesUsersBeforeAddingForeignKeys() throws SQLException {
        database.close();
        TestMySql.dropTables();
        execute("CREATE TABLE `%p%schema_version` (`version` INT NOT NULL PRIMARY KEY, `description` VARCHAR(255) NOT NULL, `installed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        for (SchemaMigrations.Migration migration : SchemaMigrations.MIGRATIONS.subList(0, 4)) {
            for (SchemaMigrations.Step step : migration.mysql()) {
                if (!step.optional()) execute(step.sql());
            }
            execute("INSERT INTO `%p%schema_version` (`version`, `description`) VALUES (" + migration.version() + ", 'test')");
        }
        execute("INSERT INTO `%p%groups` (`group`) VALUES ('Admin')");
        execute("INSERT INTO `%p%users` (`uuid`, `group`, `subgroup`) VALUES ('0000', 'admin', 'removed')");

        assertTrue(database.connect());
        assertEquals("Admin", queryString("SELECT `group` FROM `%p%users` WHERE `uuid` = '0000'"));
        assertNull(queryString("SELECT `subgroup` FROM `%p%users` WHERE `uuid` = '0000'"));
    }

    @Test
    void fillsNewDatabaseWithExamples() throws SQLException {
        assertEquals("0", queryString("SELECT `priority` FROM `%p%groups` WHERE `group` = 'default'"));
        assertEquals("100", queryString("SELECT `priority` FROM `%p%groups` WHERE `group` = 'Owner'"));
        assertEquals("rainbow", queryString("SELECT `chat_color` FROM `%p%groups` WHERE `group` = 'Vip'"));
        assertEquals(String.join("\n", Group.DEFAULT_HOVER), queryString("SELECT `hover` FROM `%p%groups` WHERE `group` = 'default'"));
        assertEquals("&6VIP", queryString("SELECT `prefix` FROM `%p%subgroups` WHERE `group` = 'Vip'"));
    }

    /**
     * the examples are only for a new database, removed groups and tags don't come back
     */
    @Test
    void addsExamplesOnlyOnce() throws SQLException {
        execute("DELETE FROM `%p%groups` WHERE `group` <> 'default'");
        execute("DELETE FROM `%p%subgroups`");
        database.close();
        assertTrue(database.connect());
        assertEquals("1", queryString("SELECT COUNT(*) FROM `%p%groups`"));
        assertEquals("0", queryString("SELECT COUNT(*) FROM `%p%subgroups`"));
    }

    @Test
    void migratesOnlyOnce() throws SQLException {
        database.close();
        assertTrue(database.connect());
        SchemaMigrations.migrate(database);
        assertEquals(SchemaMigrations.latestVersion(), SchemaMigrations.currentVersion(database));
    }

}
