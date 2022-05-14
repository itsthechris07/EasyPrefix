package com.christian34.easyprefix.sql.database;

import java.sql.Connection;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public interface Database {

    boolean connect();

    void close();

    String getTablePrefix();

    Connection getConnection();

}
