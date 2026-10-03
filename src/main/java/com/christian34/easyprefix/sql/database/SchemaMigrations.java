package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.utils.Debug;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * EasyPrefix 2026.
 * <p>
 * Versioned updates of the tables. The applied versions are stored in the table 'schema_version', so every update
 * runs exactly once per database. New changes are added as a new {@link Migration} at the end - never change a
 * released one. %p% is replaced with the table prefix.
 *
 * @author Christian34
 */
final class SchemaMigrations {

    /**
     * @param optional errors are ignored, e.g. for columns older databases might already have
     */
    record Step(String sql, boolean optional) {
    }

    record Migration(int version, String description, List<Step> mysql, List<Step> sqlite) {
        List<Step> steps(Database.Dialect dialect) {
            return dialect == Database.Dialect.MYSQL ? mysql : sqlite;
        }
    }

    private static Step required(String sql) {
        return new Step(sql, false);
    }

    private static Step optional(String sql) {
        return new Step(sql, true);
    }

    static final List<Migration> MIGRATIONS = List.of(
            // the schema before versioning - existing databases of older versions end up here as well
            new Migration(1, "initial schema",
                    List.of(
                            required("CREATE TABLE IF NOT EXISTS `%p%users` (`uuid` CHAR(36) NOT NULL, `username` VARCHAR(20) NULL DEFAULT NULL, `group` VARCHAR(64) NULL DEFAULT NULL, `force_group` BOOLEAN NULL DEFAULT NULL, `subgroup` VARCHAR(64) NULL DEFAULT NULL, `custom_prefix` VARCHAR(128) NULL DEFAULT NULL, `custom_suffix` VARCHAR(128) NULL DEFAULT NULL, `chat_color` CHAR(2) NULL DEFAULT NULL, `chat_formatting` CHAR(2) NULL DEFAULT NULL, PRIMARY KEY(`uuid`)) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin"),
                            required("CREATE TABLE IF NOT EXISTS `%p%groups` (`group` VARCHAR(64) NOT NULL, UNIQUE(`group`), `prefix` VARCHAR(128) NULL DEFAULT NULL, `suffix` VARCHAR(128) NULL DEFAULT NULL, `chat_color` CHAR(2) NULL DEFAULT NULL, `chat_formatting` CHAR(2) NULL DEFAULT NULL, `join_msg` VARCHAR(255) NULL DEFAULT NULL, `quit_msg` VARCHAR(255) NULL DEFAULT NULL) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin"),
                            required("CREATE TABLE IF NOT EXISTS `%p%subgroups` (`group` VARCHAR(64) NOT NULL, UNIQUE(`group`), `prefix` VARCHAR(128) NULL DEFAULT NULL, `suffix` VARCHAR(128) NULL DEFAULT NULL) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin"),
                            required("CREATE TABLE IF NOT EXISTS `%p%options` (`option_id` INT NOT NULL AUTO_INCREMENT, `option_name` VARCHAR(64) NOT NULL, `option_value` LONGTEXT NULL DEFAULT NULL, PRIMARY KEY (`option_id`)) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin"),
                            // older versions added these on every start
                            optional("ALTER TABLE `%p%users` ADD `username` VARCHAR(20) NULL DEFAULT NULL AFTER `uuid`"),
                            optional("ALTER TABLE `%p%users` ADD `custom_prefix_update` TIMESTAMP NULL DEFAULT NULL AFTER `custom_prefix`"),
                            optional("ALTER TABLE `%p%users` ADD `custom_suffix_update` TIMESTAMP NULL DEFAULT NULL AFTER `custom_suffix`"),
                            optional("ALTER TABLE `%p%users` ADD CONSTRAINT `group` FOREIGN KEY (`group`) REFERENCES `%p%groups`(`group`) ON DELETE SET NULL ON UPDATE CASCADE"),
                            optional("ALTER TABLE `%p%users` ADD CONSTRAINT `subgroup` FOREIGN KEY (`subgroup`) REFERENCES `%p%subgroups`(`group`) ON DELETE SET NULL ON UPDATE CASCADE")
                    ),
                    List.of(
                            required("CREATE TABLE IF NOT EXISTS `%p%users` (`uuid` CHAR(36) NOT NULL, `username` VARCHAR(20) NULL DEFAULT NULL, `group` VARCHAR(64) NULL DEFAULT NULL, `force_group` BOOLEAN NULL DEFAULT NULL, `subgroup` VARCHAR(64) NULL DEFAULT NULL, `custom_prefix` VARCHAR(128) NULL DEFAULT NULL, `custom_prefix_update` TIMESTAMP NULL DEFAULT NULL, `custom_suffix` VARCHAR(128) NULL DEFAULT NULL, `custom_suffix_update` TIMESTAMP NULL DEFAULT NULL, `chat_color` CHAR(2) NULL DEFAULT NULL, `chat_formatting` CHAR(2) NULL DEFAULT NULL, PRIMARY KEY(`uuid`))")
                    )),
            // colors and formattings are stored by name (e.g. light_purple) instead of a color code
            new Migration(2, "color names and group priority",
                    List.of(
                            required("ALTER TABLE `%p%users` MODIFY `chat_color` VARCHAR(32) NULL DEFAULT NULL, MODIFY `chat_formatting` VARCHAR(32) NULL DEFAULT NULL"),
                            required("ALTER TABLE `%p%groups` MODIFY `chat_color` VARCHAR(32) NULL DEFAULT NULL, MODIFY `chat_formatting` VARCHAR(32) NULL DEFAULT NULL"),
                            required("ALTER TABLE `%p%groups` ADD `priority` INT NOT NULL DEFAULT 1"),
                            required("UPDATE `%p%groups` SET `priority` = 0 WHERE `group` = 'default'")
                    ),
                    // sqlite does not limit the length of text columns and groups are stored in groups.yml
                    List.of()),
            // own hover text of the name in the chat per group, the lines are separated by line breaks
            new Migration(3, "hover text of groups",
                    List.of(required("ALTER TABLE `%p%groups` ADD `hover` TEXT NULL DEFAULT NULL")),
                    // groups are stored in groups.yml
                    List.of()),
            // utf8 of mysql can't store emojis, and MiniMessage prefixes (e.g. gradients) easily exceed 128 characters
            new Migration(4, "utf8mb4 and longer texts",
                    List.of(
                            // the charset of columns with a foreign key can only be changed without the checks
                            required("SET FOREIGN_KEY_CHECKS = 0"),
                            required("ALTER TABLE `%p%users` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_bin"),
                            required("ALTER TABLE `%p%groups` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_bin"),
                            required("ALTER TABLE `%p%subgroups` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_bin"),
                            required("ALTER TABLE `%p%options` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_bin"),
                            required("SET FOREIGN_KEY_CHECKS = 1"),
                            required("ALTER TABLE `%p%users` MODIFY `custom_prefix` TEXT NULL DEFAULT NULL, MODIFY `custom_suffix` TEXT NULL DEFAULT NULL"),
                            required("ALTER TABLE `%p%groups` MODIFY `prefix` TEXT NULL DEFAULT NULL, MODIFY `suffix` TEXT NULL DEFAULT NULL, MODIFY `join_msg` TEXT NULL DEFAULT NULL, MODIFY `quit_msg` TEXT NULL DEFAULT NULL"),
                            required("ALTER TABLE `%p%subgroups` MODIFY `prefix` TEXT NULL DEFAULT NULL, MODIFY `suffix` TEXT NULL DEFAULT NULL")
                    ),
                    // sqlite does not limit the length of text columns and always stores utf-8
                    List.of()),
            // names of foreign keys are unique per database: the ones of version 1 ("group", "subgroup") failed
            // silently for a second table prefix in the same database
            new Migration(5, "foreign keys named by table prefix",
                    List.of(
                            // fails if the constraint belongs to the tables of another prefix
                            optional("ALTER TABLE `%p%users` DROP FOREIGN KEY `group`"),
                            optional("ALTER TABLE `%p%users` DROP FOREIGN KEY `subgroup`"),
                            // the plugin ignores the case of group names, the constraints don't
                            required("UPDATE `%p%users` u JOIN `%p%groups` g ON LOWER(u.`group`) = LOWER(g.`group`) SET u.`group` = g.`group` WHERE u.`group` <> g.`group`"),
                            required("UPDATE `%p%users` u JOIN `%p%subgroups` s ON LOWER(u.`subgroup`) = LOWER(s.`group`) SET u.`subgroup` = s.`group` WHERE u.`subgroup` <> s.`group`"),
                            // users of groups that no longer exist would break the new constraints
                            required("UPDATE `%p%users` SET `group` = NULL WHERE `group` IS NOT NULL AND `group` NOT IN (SELECT `group` FROM `%p%groups`)"),
                            required("UPDATE `%p%users` SET `subgroup` = NULL WHERE `subgroup` IS NOT NULL AND `subgroup` NOT IN (SELECT `group` FROM `%p%subgroups`)"),
                            required("ALTER TABLE `%p%users` ADD CONSTRAINT `%p%fk_group` FOREIGN KEY (`group`) REFERENCES `%p%groups`(`group`) ON DELETE SET NULL ON UPDATE CASCADE"),
                            required("ALTER TABLE `%p%users` ADD CONSTRAINT `%p%fk_subgroup` FOREIGN KEY (`subgroup`) REFERENCES `%p%subgroups`(`group`) ON DELETE SET NULL ON UPDATE CASCADE")
                    ),
                    // groups are stored in groups.yml
                    List.of()),
            // servers announce their changes as messages instead of overwriting a single id in the options table
            // (see SQLSynchronizer)
            new Migration(6, "sync messages",
                    List.of(
                            required("CREATE TABLE IF NOT EXISTS `%p%messages` (`id` BIGINT NOT NULL AUTO_INCREMENT, `server` CHAR(36) NOT NULL, `type` VARCHAR(16) NOT NULL, `target` VARCHAR(64) NULL DEFAULT NULL, `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`)) ENGINE = InnoDB CHARSET = utf8mb4 COLLATE utf8mb4_bin"),
                            required("DELETE FROM `%p%options` WHERE `option_name` = 'perform_sync'")
                    ),
                    // a local storage is not shared
                    List.of()),
            // players can turn off being pinged by @mentions (null = mentions are on)
            new Migration(7, "mentions per player",
                    List.of(required("ALTER TABLE `%p%users` ADD `mentions_disabled` BOOLEAN NULL DEFAULT NULL")),
                    List.of(required("ALTER TABLE `%p%users` ADD `mentions_disabled` BOOLEAN NULL DEFAULT NULL")))
    );

    private SchemaMigrations() {
    }

    static int latestVersion() {
        return MIGRATIONS.getLast().version();
    }

    static void migrate(Database database) throws SQLException {
        migrate(database, MIGRATIONS);
    }

    /**
     * applies every migration that is newer than the version of the database, each in its own transaction
     * (mysql can't roll back changes of the table structure, so those steps have to be safe on their own)
     */
    static void migrate(Database database, List<Migration> migrations) throws SQLException {
        try (Connection connection = database.getConnection()) {
            execute(connection, database, "CREATE TABLE IF NOT EXISTS `%p%schema_version` (`version` INT NOT NULL PRIMARY KEY, `description` VARCHAR(255) NOT NULL, `installed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            int current = currentVersion(connection, database);

            for (Migration migration : migrations) {
                if (migration.version() <= current) continue;
                Debug.log(String.format("Updating database to version %d (%s)...", migration.version(), migration.description()));
                connection.setAutoCommit(false);
                try {
                    for (Step step : migration.steps(database.getDialect())) {
                        try {
                            execute(connection, database, step.sql());
                        } catch (SQLException ex) {
                            if (!step.optional()) throw ex;
                        }
                    }
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO `" + database.getTablePrefix() + "schema_version` (`version`, `description`) VALUES (?, ?)")) {
                        insert.setInt(1, migration.version());
                        insert.setString(2, migration.description());
                        insert.executeUpdate();
                    }
                    connection.commit();
                } catch (SQLException ex) {
                    connection.rollback();
                    throw new SQLException(String.format("Couldn't update the database to version %d: %s", migration.version(), ex.getMessage()), ex);
                } finally {
                    connection.setAutoCommit(true);
                }
            }
        }
    }

    static int currentVersion(Database database) throws SQLException {
        try (Connection connection = database.getConnection()) {
            return currentVersion(connection, database);
        }
    }

    private static int currentVersion(Connection connection, Database database) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT MAX(`version`) FROM `" + database.getTablePrefix() + "schema_version`")) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private static void execute(Connection connection, Database database, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql.replace("%p%", database.getTablePrefix()));
        }
    }

}
