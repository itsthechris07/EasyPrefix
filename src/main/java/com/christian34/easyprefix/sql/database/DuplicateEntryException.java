package com.christian34.easyprefix.sql.database;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class DuplicateEntryException extends RuntimeException {

    public DuplicateEntryException(String table, String value) {
        super("Duplicate entry in table '" + table + "': " + value);
    }

}
