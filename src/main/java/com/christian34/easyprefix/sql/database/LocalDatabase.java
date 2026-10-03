package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.utils.Debug;
import com.zaxxer.hikari.HikariConfig;

import java.io.File;

/**
 * EasyPrefix 2026.
 * <p>
 * User data in a SQLite file (storage.db), groups are stored in groups.yml.
 *
 * @author Christian34
 */
public class LocalDatabase extends PooledDatabase {
    private final File file;

    public LocalDatabase() {
        this(new File(FileManager.getPluginFolder(), "storage.db"));
    }

    public LocalDatabase(File file) {
        this.file = file;
    }

    @Override
    protected HikariConfig createConfig() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("EasyPrefix-SQLite");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        // sqlite allows only one writer at a time
        config.setMaximumPoolSize(1);
        // a connection that is not given back blocks everything, so fail fast instead of waiting 30 seconds
        config.setConnectionTimeout(5_000);
        return config;
    }

    @Override
    protected void onConnectFailure(Exception exception) {
        Debug.warn("§cCouldn't open the local storage (" + file.getName() + "): " + exception.getMessage());
    }

    @Override
    public String getTablePrefix() {
        return "";
    }

    @Override
    public Dialect getDialect() {
        return Dialect.SQLITE;
    }

}
