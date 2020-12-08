package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.utils.Debug;

import java.sql.*;
import java.util.TimeZone;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class SQLDatabase implements Database {
    private final String host;
    private final String database;
    private final String username;
    private final String tablePrefix;
    private final String password;
    private final int port;
    private Connection connection;

    public SQLDatabase() {
        this.host = ConfigKeys.SQL_HOST.toString();
        this.database = ConfigKeys.SQL_DATABASE.toString();
        this.username = ConfigKeys.SQL_USERNAME.toString();
        this.password = ConfigKeys.SQL_PASSWORD.toString();
        this.port = ConfigKeys.SQL_PORT.toInt();
        String tPrefix = ConfigKeys.SQL_TABLEPREFIX.toString();
        if (tPrefix == null || tPrefix.isEmpty()) {
            tPrefix = "";
        } else if (!tPrefix.endsWith("_")) tPrefix += "_";
        this.tablePrefix = tPrefix;
    }

    @Override
    public boolean connect() {
        synchronized (this) {
            try {
                if (connection != null && !connection.isClosed()) return true;
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database
                                + "?useSSL=false&useUnicode=true&characterEncoding=utf-8"
                                + "&autoReconnect=true&serverTimezone=" + TimeZone.getDefault().getID(),
                        username, password);
                updateTables();
                return true;
            } catch (SQLSyntaxErrorException e) {
                Debug.log("§cDatabase '" + database + "' does not exist!");
            } catch (SQLException e) {
                Debug.log("§cAccess denied for user '" + this.username + "'@'" + this.host + "'");
                Debug.log("§cPlease check if the sql server is running and you entered the right username and password.");
            } catch (ClassNotFoundException e) {
                Debug.log("§cYour installation does not support sql!");
            }
            return false;
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            try {
                if (getConnection() != null && !getConnection().isClosed()) {
                    getConnection().close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public ResultSet getValue(String statement) {
        try {
            if (connection.isClosed()) connect();
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(statement.replace("%p%", getTablePrefix()));
        } catch (SQLException e) {
            Debug.captureException(e);
            return null;
        }
    }

    private void update(String statement) {
        try {
            if (connection.isClosed()) connect();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(statement.replace("%p%", getTablePrefix()));
            stmt.close();
        } catch (SQLException e) {
            Debug.captureException(e);
        }
    }

    @Override
    public String getTablePrefix() {
        return tablePrefix;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    public void alterTable(String statement) {
        try {
            if (connection.isClosed()) connect();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(statement.replace("%p%", getTablePrefix()));
            stmt.close();
        } catch (SQLException ignored) {
        }
    }

    private void updateTables() {
        String create = "CREATE TABLE IF NOT EXISTS ";
        update(create + "`%p%users` (`uuid` CHAR(36) NOT NULL, `username` VARCHAR(20) NULL DEFAULT NULL, `group` VARCHAR(64) NULL DEFAULT NULL, `force_group` BOOLEAN NULL DEFAULT NULL, `subgroup` VARCHAR(64) NULL DEFAULT NULL, `custom_prefix` VARCHAR(128) NULL DEFAULT NULL, `custom_suffix` VARCHAR(128) NULL DEFAULT NULL, `gender` VARCHAR(32) NULL DEFAULT NULL, `chat_color` CHAR(2) NULL DEFAULT NULL, `chat_formatting` CHAR(2) NULL DEFAULT NULL, PRIMARY KEY(`uuid`)) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update(create + "`%p%groups` (`group` VARCHAR(64) not null, UNIQUE(`group`), prefix VARCHAR(128) default NULL null, suffix VARCHAR(128) default NULL null, chat_color CHAR(2) default NULL null, chat_formatting CHAR(2) default NULL null, join_msg VARCHAR(255) default NULL null, quit_msg VARCHAR(255) default NULL null)ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update(create + "`%p%subgroups` ( `group` VARCHAR(64) NOT NULL , UNIQUE(`group`), `prefix` VARCHAR(128) default NULL null , `suffix` VARCHAR(128) default NULL null ) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update(create + "`%p%groups_gendered` ( `id` INT NOT NULL AUTO_INCREMENT , `group` VARCHAR(64) NOT NULL , `gender` VARCHAR(32) NOT NULL , `prefix` VARCHAR(128) default NULL null , `suffix` VARCHAR(128) default NULL null , PRIMARY KEY (`id`)) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update(create + "`%p%subgroups_gendered` ( `id` INT NOT NULL AUTO_INCREMENT , `group` VARCHAR(64) NOT NULL , `gender` VARCHAR(32) NOT NULL , `prefix` VARCHAR(128) default NULL null , `suffix` VARCHAR(128) default NULL null , PRIMARY KEY (`id`)) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update(create + "`%p%options` ( `option_id` INT NOT NULL AUTO_INCREMENT , `option_name` VARCHAR(64) NOT NULL , `option_value` LONGTEXT NOT NULL , PRIMARY KEY (`option_id`)) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");

        String alter = "ALTER TABLE `%p%users` ADD ";
        alterTable(alter + "`username` VARCHAR(20) NULL AFTER `uuid`; ");
        alterTable(alter + "`custom_prefix_update` TIMESTAMP NULL DEFAULT NULL AFTER `custom_prefix`;");
        alterTable(alter + "`username` VARCHAR(20) NULL DEFAULT NULL AFTER `uuid`;");
        alterTable(alter + "`custom_suffix_update` TIMESTAMP NULL DEFAULT NULL AFTER `custom_suffix`;");
        alterTable(alter + "CONSTRAINT `group` FOREIGN KEY (`group`) REFERENCES `%p%groups`(`group`) ON DELETE SET NULL ON UPDATE CASCADE;");
        alterTable(alter + "CONSTRAINT `subgroup` FOREIGN KEY (`subgroup`) REFERENCES `%p%subgroups`(`group`) ON DELETE SET NULL ON UPDATE CASCADE;");
        alterTable("ALTER TABLE `%p%groups_gendered` ADD CONSTRAINT `group_name` FOREIGN KEY (`group`) REFERENCES `%p%groups`(`group`) ON DELETE CASCADE ON UPDATE CASCADE; ");
        alterTable("ALTER TABLE `%p%subgroups_gendered` ADD CONSTRAINT `subgroup_name` FOREIGN KEY (`group`) REFERENCES `%p%subgroups`(`group`) ON DELETE CASCADE ON UPDATE CASCADE;");
    }

}
