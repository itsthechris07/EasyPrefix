package com.christian34.easyprefix.sql.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * EasyPrefix 2026.
 * <p>
 * The optional test database: ./gradlew test -PtestMysql="host:port/database;user;password". The tables get the
 * prefix eptest_, so the database can be shared with a server.
 *
 * @author Christian34
 */
public final class TestMySql {
    public static final String PROPERTY = "easyprefix.test.mysql";
    public static final String PREFIX = "eptest_";

    public record Settings(String host, int port, String database, String username, String password) {
    }

    private TestMySql() {
    }

    public static Settings settings() {
        String[] settings = System.getProperty(PROPERTY).split(";", 3);
        String[] address = settings[0].split("/", 2);
        String[] hostPort = address[0].split(":", 2);
        return new Settings(hostPort[0], hostPort.length > 1 ? Integer.parseInt(hostPort[1]) : 3306, address[1],
                settings[1], settings.length > 2 ? settings[2] : "");
    }

    public static SQLDatabase database() {
        Settings settings = settings();
        return new SQLDatabase(settings.host(), settings.port(), settings.database(), settings.username(),
                settings.password(), PREFIX);
    }

    /**
     * a connection without the pool of the plugin, e.g. to check what it stored or to act as another server
     */
    public static Connection connect() throws SQLException {
        Settings settings = settings();
        return DriverManager.getConnection("jdbc:mysql://" + settings.host() + ":" + settings.port() + "/"
                + settings.database() + "?characterEncoding=UTF-8", settings.username(), settings.password());
    }

    /**
     * removes all tables of the tests, the users first as they reference the groups
     */
    public static void dropTables() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String table : List.of("users", "groups", "subgroups", "options", "messages", "schema_version")) {
                statement.executeUpdate("DROP TABLE IF EXISTS `" + PREFIX + table + "`");
            }
        }
    }

    /**
     * @param sql %p% is replaced with the table prefix
     */
    public static void execute(String sql) throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql.replace("%p%", PREFIX));
        }
    }

    /**
     * @param sql %p% is replaced with the table prefix
     * @return the first column of the first row, null if there is none
     */
    public static String queryString(String sql) throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement();
             var result = statement.executeQuery(sql.replace("%p%", PREFIX))) {
            return result.next() ? result.getString(1) : null;
        }
    }

}
