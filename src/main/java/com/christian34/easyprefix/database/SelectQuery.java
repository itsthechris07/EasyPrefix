package com.christian34.easyprefix.database;

import com.christian34.easyprefix.EasyPrefix;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class SelectQuery {
    private final HashMap<String, String> conditions = new HashMap<>();
    private final String table;
    private List<String> columns;
    private Data data = null;

    public SelectQuery(String table, String... columns) {
        this.table = table;
        this.columns = new ArrayList<>(Arrays.asList(columns));
    }

    public SelectQuery setColumns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    public SelectQuery addCondition(String column, String value) {
        this.conditions.put(column, value);
        return this;
    }

    /**
     * Loads first row from query
     *
     * @return Data
     */
    public Data getData() {
        if (data != null) {
            return data;
        }

        try {
            data = new Data(retrieveData().get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            data = new Data(null);
        }
        return data;
    }

    private CompletableFuture<HashMap<String, Object>> retrieveData() {
        return CompletableFuture.supplyAsync(() -> {
            HashMap<String, Object> map = new HashMap<>();
            try {
                ResultSet result = buildStatement().executeQuery();
                if (result.next()) {
                    for (String key : columns) {
                        map.put(key, result.getString(key));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return map;
        });
    }

    public PreparedStatement buildStatement() throws SQLException {
        StringBuilder query = new StringBuilder("SELECT ");
        EasyPrefix instance = EasyPrefix.getInstance();
        Database database;
        if (instance.getStorageType() == StorageType.SQL) {
            database = instance.getSqlDatabase();
        } else {
            database = instance.getLocalDatabase();
        }

        for (int i = 0; i < columns.size(); i++) {
            query.append("`").append(columns.get(i)).append("`");
            if (i != columns.size() - 1) {
                query.append(", ");
            }
        }

        query.append(" FROM `").append(database.getTablePrefix()).append(this.table).append("`");

        int i = 1;
        for (String key : conditions.keySet()) {
            if (i == 1) {
                query.append(" WHERE ");
            } else {
                query.append(" AND ");
            }
            query.append("`").append(key).append("` = ?");
            i++;
        }

        PreparedStatement stmt = database.getConnection().prepareStatement(query.toString());
        i = 1;
        for (String value : conditions.values()) {
            stmt.setObject(i, value);
            i++;
        }
        return stmt;
    }

}
