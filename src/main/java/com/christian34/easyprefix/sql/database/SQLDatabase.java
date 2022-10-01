package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.utils.Debug;

import java.sql.*;
import java.util.TimeZone;

/**
 * EasyPrefix 2022.
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
    private final EasyPrefix instance;
    private Connection connection;
    private SQLSynchronizer sqlSynchronizer;

    public SQLDatabase(EasyPrefix instance) {
        this.instance = instance;
        ConfigData config = instance.getFileManager().getConfig();
        this.host = config.getString("sql.host");
        this.database = config.getString("sql.database");
        this.username = config.getString("sql.username");
        this.password = config.getString("sql.password");
        this.port = config.getInt("sql.port");
        String tPrefix = config.getString("sql.table-prefix");
        if (tPrefix == null || tPrefix.isEmpty()) {
            tPrefix = "";
        } else if (!tPrefix.endsWith("_")) tPrefix += "_";
        this.tablePrefix = tPrefix;
    }

    public SQLSynchronizer getSqlSynchronizer() {
        return sqlSynchronizer;
    }

    @Override
    public boolean connect() {
        synchronized (this) {
            try {
                if (connection != null && !connection.isClosed()) return true;
                Debug.recordAction("initialize connection to mysql");
                Class.forName("com.mysql.jdbc.Driver");
                this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&useUnicode=true&characterEncoding=utf-8" + "&autoReconnect=true&serverTimezone=" + TimeZone.getDefault().getID(), username, password);
                updateTables();
                this.sqlSynchronizer = new SQLSynchronizer(instance);
                return true;
            } catch (SQLSyntaxErrorException e) {
                Debug.warn("§cDatabase '" + database + "' does not exist!");
            } catch (SQLException e) {
                Debug.warn("§cAccess denied for user '" + this.username + "'@'" + this.host + "'");
                Debug.warn("§cPlease check if the sql server is running and you entered the right username and password.");
            } catch (ClassNotFoundException e) {
                Debug.warn("§cYour installation does not support sql!");
            }
            throw new Error("Couldn't enable plugin, because the database is not working!");
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public ResultSet getValue(String statement) {
        try {
            Statement stmt = getConnection().createStatement();
            return stmt.executeQuery(statement.replace("%p%", getTablePrefix()));
        } catch (SQLException e) {
            Debug.handleException(e);
            return null;
        }
    }

    private void update(String statement) {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(statement.replace("%p%", getTablePrefix()));
        } catch (SQLException e) {
            Debug.handleException(e);
        }
    }

    @Override
    public String getTablePrefix() {
        return tablePrefix;
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            return connection;
        } catch (SQLException ex) {
            Debug.handleException(ex);
        }
        return null;
    }

    public void alterTable(String statement) {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(statement.replace("%p%", getTablePrefix()));
        } catch (SQLException ignored) {
        }
    }

    private void updateTables() {
        String create = "CREATE TABLE IF NOT EXISTS ";
        update(create + "`%p%users` (`uuid` CHAR(36) NOT NULL, `username` VARCHAR(20) NULL DEFAULT NULL, `group` VARCHAR(64) NULL DEFAULT NULL, `force_group` BOOLEAN NULL DEFAULT NULL, `subgroup` VARCHAR(64) NULL DEFAULT NULL, `custom_prefix` VARCHAR(128) NULL DEFAULT NULL, `custom_suffix` VARCHAR(128) NULL DEFAULT NULL, `chat_color` CHAR(2) NULL DEFAULT NULL, `chat_formatting` CHAR(2) NULL DEFAULT NULL, PRIMARY KEY(`uuid`)) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update(create + "`%p%groups` (`group` VARCHAR(64) not null, UNIQUE(`group`), prefix VARCHAR(128) default NULL null, suffix VARCHAR(128) default NULL null, chat_color CHAR(2) default NULL null, chat_formatting CHAR(2) default NULL null, join_msg VARCHAR(255) default NULL null, quit_msg VARCHAR(255) default NULL null)ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update(create + "`%p%subgroups` ( `group` VARCHAR(64) NOT NULL , UNIQUE(`group`), `prefix` VARCHAR(128) default NULL null , `suffix` VARCHAR(128) default NULL null ) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update(create + "`%p%options` ( `option_id` INT NOT NULL AUTO_INCREMENT , `option_name` VARCHAR(64) NOT NULL , `option_value` LONGTEXT NULL DEFAULT NULL , PRIMARY KEY (`option_id`)) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");

        String alter = "ALTER TABLE `%p%users` ADD ";
        alterTable(alter + "`username` VARCHAR(20) NULL AFTER `uuid`; ");
        alterTable(alter + "`custom_prefix_update` TIMESTAMP NULL DEFAULT NULL AFTER `custom_prefix`;");
        alterTable(alter + "`username` VARCHAR(20) NULL DEFAULT NULL AFTER `uuid`;");
        alterTable(alter + "`custom_suffix_update` TIMESTAMP NULL DEFAULT NULL AFTER `custom_suffix`;");
        alterTable(alter + "CONSTRAINT `group` FOREIGN KEY (`group`) REFERENCES `%p%groups`(`group`) ON DELETE SET NULL ON UPDATE CASCADE;");
        alterTable(alter + "CONSTRAINT `subgroup` FOREIGN KEY (`subgroup`) REFERENCES `%p%subgroups`(`group`) ON DELETE SET NULL ON UPDATE CASCADE;");
    }

}
