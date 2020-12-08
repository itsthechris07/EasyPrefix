package com.christian34.easyprefix.sql;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.sql.database.Database;
import com.christian34.easyprefix.sql.database.StorageType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class UpdateStatement {
    private static final Database database;
    private static final EasyPrefix instance;

    static {
        instance = EasyPrefix.getInstance();
        database = instance.getStorageType() == StorageType.SQL
                ? instance.getSqlDatabase()
                : instance.getLocalDatabase();
    }

    private final HashMap<String, Object> values;
    private final HashMap<String, String> conditions;
    private final String table;

    public UpdateStatement(String table) {
        this.table = table;
        this.values = new HashMap<>();
        this.conditions = new HashMap<>();
    }

    public UpdateStatement setValue(String column, Object value) {
        this.values.put(column, value);
        return this;
    }

    public UpdateStatement addCondition(String column, String value) {
        this.conditions.put(column, value);
        return this;
    }

    private PreparedStatement buildStatement() throws SQLException {
        StringBuilder query = new StringBuilder("UPDATE ");
        query.append("`").append(database.getTablePrefix()).append(this.table).append("`");

        int i = 1;
        for (String column : values.keySet()) {
            if (i == 1) {
                query.append(" SET ");
            } else {
                query.append(", ");
            }
            query.append(" `").append(column).append("`=?");
            i++;
        }

        i = 1;
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
        for (Map.Entry<String, Object> value : values.entrySet()) {
            stmt.setObject(i, value.getValue());
            i++;
        }

        for (String value : conditions.values()) {
            stmt.setObject(i, value);
            i++;
        }
        return stmt;
    }

    public boolean execute() {
        CompletableFuture<Boolean> compFuture = CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = buildStatement()) {
                stmt.executeUpdate();
                return true;
            } catch (SQLException ex) {
                return false;
            }
        });
        try {
            return compFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

}
