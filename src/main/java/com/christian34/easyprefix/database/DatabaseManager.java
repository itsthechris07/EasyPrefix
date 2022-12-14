package com.christian34.easyprefix.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.tables.TableGroup;
import com.christian34.easyprefix.database.tables.TableOptions;
import com.christian34.easyprefix.database.tables.TableSubgroup;
import com.christian34.easyprefix.database.tables.TableUser;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.utils.Debug;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class DatabaseManager {
    private final DatabaseType databaseType;
    private final EasyPrefix instance;

    private ConnectionSource connectionSource;

    private Synchronizer synchronizer;

    private Dao<TableGroup, String> tableGroupDao;
    private Dao<TableSubgroup, String> tableSubgroupDao;

    private Dao<TableOptions, String> tableOptionsDao;

    private Dao<TableUser, String> tableUserDao;

    public DatabaseManager(DatabaseType databaseType) {
        this.databaseType = databaseType;
        this.instance = EasyPrefix.getInstance();

        try {
            connect(this.databaseType);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            this.connectionSource.close();
            if (this.synchronizer != null) this.synchronizer.disable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Synchronizer getSynchronizer() {
        return synchronizer;
    }

    public Dao<TableGroup, String> getTableGroupDao() {
        return tableGroupDao;
    }

    public Dao<TableSubgroup, String> getTableSubgroupDao() {
        return tableSubgroupDao;
    }

    public Dao<TableUser, String> getTableUserDao() {
        return tableUserDao;
    }

    public Dao<TableOptions, String> getTableOptionsDao() {
        return tableOptionsDao;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    void connect(DatabaseType databaseType) throws SQLException {
        if (databaseType == DatabaseType.MYSQL) {
            ConfigData config = instance.getFileManager().getConfig();
            String host = config.getString("sql.host");
            String database = config.getString("sql.database");
            String username = config.getString("sql.username");
            String password = config.getString("sql.password");
            int port = config.getInt("sql.port");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            try {
                this.connectionSource = new JdbcConnectionSource(url, username, password);
            } catch (SQLSyntaxErrorException e) {
                Debug.warn("§cDatabase '" + database + "' does not exist!");
            } catch (SQLException e) {
                Debug.warn("§cAccess denied for user '" + username + "'@'" + host + "'");
                Debug.warn("§cPlease check if the sql server is running and you entered the right username and password.");
            }
            this.tableGroupDao = DaoManager.createDao(this.connectionSource, TableGroup.class);
            TableUtils.createTableIfNotExists(this.connectionSource, TableGroup.class);

            this.tableSubgroupDao = DaoManager.createDao(this.connectionSource, TableSubgroup.class);
            TableUtils.createTableIfNotExists(this.connectionSource, TableSubgroup.class);

            this.tableOptionsDao = DaoManager.createDao(this.connectionSource, TableOptions.class);
            TableUtils.createTableIfNotExists(this.connectionSource, TableOptions.class);

            this.synchronizer = new Synchronizer(this);
            try {
                this.synchronizer.enable();
            } catch (Exception e) {
                Debug.warn("An error occurred while enabling database synchronisation. Please report following message on github:");
                e.printStackTrace();
            }
        } else {
            File file = new File(FileManager.getPluginFolder() + "/storage.db");
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) throw new RuntimeException("Couldn't create storage file!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + file.getAbsolutePath());
        }

        this.tableUserDao = DaoManager.createDao(this.connectionSource, TableUser.class);
        TableUtils.createTableIfNotExists(this.connectionSource, TableUser.class);
        LoggerFactory.setLogBackendFactory(LogBackendType.NULL);
        Logger.setGlobalLogLevel(Level.WARNING);
    }

}
