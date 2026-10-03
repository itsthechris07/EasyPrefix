package com.christian34.easyprefix.sql.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * EasyPrefix 2026.
 * <p>
 * A database of EasyPrefix: the local SQLite storage or a MySQL database.
 *
 * @author Christian34
 */
public interface Database {

    /**
     * opens the connection pool and updates the tables to the latest version
     *
     * @return false if the database can't be used
     */
    boolean connect();

    void close();

    String getTablePrefix();

    Dialect getDialect();

    /**
     * Borrows a connection from the pool - it has to be closed (try-with-resources) to give it back.
     */
    Connection getConnection() throws SQLException;

    /**
     * runs a query and reads the result while the connection is still open
     *
     * @param sql the query, %p% is replaced with the table prefix
     */
    default <T> T query(String sql, ResultHandler<T> handler) throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql.replace("%p%", getTablePrefix()))) {
            return handler.handle(result);
        }
    }

    @FunctionalInterface
    interface ResultHandler<T> {
        T handle(ResultSet result) throws SQLException;
    }

    enum Dialect {
        MYSQL, SQLITE
    }

}
