package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.utils.Debug;
import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TimeZone;

/**
 * EasyPrefix 2026.
 * <p>
 * MySQL/MariaDB database, shared by several servers. The driver is provided by the server.
 *
 * @author Christian34
 */
public class SQLDatabase extends PooledDatabase {
    /**
     * current driver first - the old name was removed in mysql-connector-j 9
     */
    private static final String[] DRIVERS = {"com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"};
    private final String host;
    private final String database;
    private final String username;
    private final String tablePrefix;
    private final String password;
    private final int port;
    private SQLSynchronizer sqlSynchronizer;

    public SQLDatabase(EasyPrefix instance) {
        ConfigData config = instance.getFileManager().getConfig();
        this(config.getString("sql.host"), config.getInt("sql.port"), config.getString("sql.database"),
                config.getString("sql.username"), config.getString("sql.password"), config.getString("sql.table-prefix"));
    }

    public SQLDatabase(String host, int port, String database, String username, String password, @Nullable String tablePrefix) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        if (tablePrefix == null || tablePrefix.isEmpty()) {
            tablePrefix = "";
        } else if (!tablePrefix.endsWith("_")) tablePrefix += "_";
        this.tablePrefix = tablePrefix;
    }

    @Override
    protected HikariConfig createConfig() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("EasyPrefix-MySQL");
        config.setDriverClassName(findDriver());
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(4);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10_000);
        config.addDataSourceProperty("useSSL", "false");
        config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "UTF-8");
        config.addDataSourceProperty("serverTimezone", TimeZone.getDefault().getID());
        config.addDataSourceProperty("cachePrepStmts", "true");
        return config;
    }

    private static String findDriver() {
        for (String driver : DRIVERS) {
            try {
                Class.forName(driver);
                return driver;
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new IllegalStateException("No MySQL driver found - your server does not support mysql!");
    }

    @Override
    protected void onConnectFailure(Exception exception) {
        Debug.warn("§c************************************************************");
        Debug.warn(String.format("§cCouldn't connect to the MySQL database '%s' on %s:%s!", database, host, port));
        Debug.warn("§cPlease check if the sql server is running and the settings in config.yml are right.");
        Debug.warn("§cReason: " + exception.getMessage());
        Debug.warn("§c************************************************************");
    }

    /**
     * a new database gets the example groups and tags, just like a new groups.yml
     */
    @Override
    protected void afterMigration() {
        try {
            if (new Migration(this).uploadExamples()) {
                Debug.log("The database was empty, the example groups and tags have been added.");
            }
        } catch (SQLException | IOException ex) {
            Debug.warn("Couldn't add the example groups to the database: " + ex.getMessage());
        }
    }

    /**
     * starts syncing changes with other servers using the same database
     */
    public void startSynchronizer(EasyPrefix instance) {
        if (sqlSynchronizer == null) {
            this.sqlSynchronizer = new SQLSynchronizer(instance, this);
        }
    }

    @Override
    public synchronized void close() {
        if (sqlSynchronizer != null) {
            sqlSynchronizer.stop();
            this.sqlSynchronizer = null;
        }
        super.close();
    }

    /**
     * tells the other servers what they have to reload
     *
     * @param target the uuid of a user, null for all users
     */
    public void announce(SQLSynchronizer.Type type, @Nullable String target) {
        if (sqlSynchronizer != null) sqlSynchronizer.announce(type, target);
    }

    /**
     * announces a change of a table (by the statements)
     *
     * @param uuid the user, if known
     */
    public void announce(String table, @Nullable Object uuid) {
        switch (table) {
            case "users" -> announce(SQLSynchronizer.Type.USER, uuid == null ? null : uuid.toString());
            case "groups", "subgroups" -> announce(SQLSynchronizer.Type.GROUPS, null);
            // options: the shared config announces itself
            default -> {
            }
        }
    }

    public SQLSynchronizer getSqlSynchronizer() {
        return sqlSynchronizer;
    }

    @Override
    public String getTablePrefix() {
        return tablePrefix;
    }

    @Override
    public Dialect getDialect() {
        return Dialect.MYSQL;
    }

}
