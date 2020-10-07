package com.christian34.easyprefix.database;

import java.sql.Connection;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public interface Database {

    void connect();

    void close();

    void update(String statement);

    boolean exists(String statement);

    String getTablePrefix();

    Connection getConnection();

}
