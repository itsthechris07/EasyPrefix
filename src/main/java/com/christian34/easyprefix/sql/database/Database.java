package com.christian34.easyprefix.sql.database;

import java.sql.Connection;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public interface Database {

    void connect();

    void close();

    boolean exists(String statement);

    String getTablePrefix();

    Connection getConnection();

}
