package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.utils.Debug;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * EasyPrefix 2026.
 * <p>
 * A database behind a HikariCP connection pool, which takes care of broken connections and reconnecting.
 *
 * @author Christian34
 */
public abstract class PooledDatabase implements Database {
    private volatile HikariDataSource dataSource;

    protected abstract HikariConfig createConfig();

    /**
     * explains to the server owner why the database can't be used
     */
    protected abstract void onConnectFailure(Exception exception);

    @Override
    public synchronized boolean connect() {
        if (dataSource != null && !dataSource.isClosed()) return true;
        try {
            this.dataSource = new HikariDataSource(createConfig());
            SchemaMigrations.migrate(this);
            afterMigration();
            return true;
        } catch (Exception ex) {
            onConnectFailure(ex);
            close();
            return false;
        }
    }

    /**
     * called once the tables are up to date, e.g. to fill a new database
     */
    protected void afterMigration() {
    }

    @Override
    public synchronized void close() {
        if (dataSource != null) {
            dataSource.close();
            this.dataSource = null;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        HikariDataSource source = this.dataSource;
        if (source == null || source.isClosed()) {
            throw new SQLException("The database of EasyPrefix is not connected!");
        }
        return source.getConnection();
    }

    protected static void log(String message) {
        Debug.recordAction(message);
    }

}
