package com.christian34.easyprefix.sql.database;

/**
 * EasyPrefix 2026.
 * <p>
 * Thrown if a row with the same unique key already exists.
 *
 * @author Christian34
 */
public class DuplicateEntryException extends RuntimeException {

    public DuplicateEntryException(String table, String value) {
        super("Duplicate entry in table '" + table + "': " + value);
    }

}
