package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.utils.Debug;

import java.io.File;
import java.io.IOException;
import java.sql.*;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class LocalDatabase implements Database {
    private Connection connection;

    public LocalDatabase() {
        File file = new File(FileManager.getPluginFolder() + "/storage.db");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) throw new RuntimeException("Couldn't create storage file!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        connect();

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + getTablePrefix() + "users` (`uuid` CHAR(36) NOT NULL, `username` VARCHAR(20) NULL DEFAULT NULL, `group` VARCHAR(64) NULL DEFAULT NULL, `force_group` BOOLEAN NULL DEFAULT NULL, `subgroup` VARCHAR(64) NULL DEFAULT NULL, " + "`custom_prefix` VARCHAR(128) NULL DEFAULT NULL, `custom_prefix_update` TIMESTAMP NULL DEFAULT NULL, " + "`custom_suffix` VARCHAR(128) NULL DEFAULT NULL, `custom_suffix_update` TIMESTAMP NULL DEFAULT NULL, " + "`gender` VARCHAR(32) NULL DEFAULT NULL, `chat_color` CHAR(2) NULL DEFAULT NULL, `chat_formatting` CHAR(2) NULL DEFAULT NULL, PRIMARY KEY(`uuid`))");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection != null && connection.isClosed()) {
                connect();
            }
        } catch (SQLException ex) {
            Debug.handleException(ex);
        }
        return connection;
    }

    @Override
    public boolean connect() {
        synchronized (this) {
            try {
                if (connection != null && !connection.isClosed()) return true;
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + FileManager.getPluginFolder() + "/storage" + ".db");
                return true;
            } catch (SQLException e) {
                Debug.log("§cCouldn't connect to local storage!");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                Debug.log("§cYour installation does not support sqlite!");
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
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public String getTablePrefix() {
        return "";
    }

    public ResultSet getValue(String statement) {
        try (Statement stmt = getConnection().createStatement()) {
            return stmt.executeQuery(statement.replace("%p%", getTablePrefix()));
        } catch (SQLException e) {
            Debug.handleException(e);
            e.printStackTrace();
            return null;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

}
