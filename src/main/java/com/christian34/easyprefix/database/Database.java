package com.christian34.easyprefix.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public interface Database {

    void connect();

    void close();

    ResultSet getValue(Query query);

    HashMap<String, String> getData(Query query);

    void update(String statement);

    boolean exists(String statement);

    String getTablePrefix();

    Connection getConnection();

}
