package com.christian34.easyprefix.database;

import com.christian34.easyprefix.EasyPrefix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Query {
    private static String tablePrefix = null;

    static {
        if (getStorageType() == StorageType.SQL) {
            tablePrefix = EasyPrefix.getInstance().getSqlDatabase().getTablePrefix();
        }
    }

    private final List<String> rows;
    private final String table;
    private String condition;

    public Query(String table) {
        if (getStorageType() == StorageType.SQL) {
            this.table = getTablePrefix() + table;
        } else {
            this.table = table.replace("%p%", "");
        }
        this.rows = new ArrayList<>();
    }

    public static String getTablePrefix() {
        return tablePrefix;
    }

    public static StorageType getStorageType() {
        return EasyPrefix.getInstance().getStorageType();
    }

    public List<String> getRows() {
        return rows;
    }

    public Query setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public Query setRow(String... row) {
        rows.addAll(Arrays.asList(row));
        return this;
    }

    public Query setRow(List<String> list) {
        rows.addAll(list);
        return this;
    }

    public String getStatement() {
        StringBuilder rowBuilder = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            String row = rows.get(i);
            rowBuilder.append("`").append(row).append("`");
            if (i != rows.size() - 1) {
                rowBuilder.append(", ");
            }
        }
        return "SELECT " + rowBuilder.toString() + " FROM `" + table + "` WHERE " + this.condition;
    }

}
