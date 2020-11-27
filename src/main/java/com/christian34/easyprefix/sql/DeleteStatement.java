package com.christian34.easyprefix.sql;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.sql.database.Database;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;

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
public class DeleteStatement {
    private final String table;
    private final Map<String, String> conditions;

    public DeleteStatement(String table) {
        this.table = table;
        this.conditions = new HashMap<>();
    }

    public DeleteStatement addCondition(String column, String value) {
        this.conditions.put(column, value);
        return this;
    }

    private PreparedStatement buildStatement() throws SQLException {
        StringBuilder query = new StringBuilder("DELETE FROM ");
        EasyPrefix instance = EasyPrefix.getInstance();
        Database database = instance.getStorageType() == StorageType.SQL
                ? instance.getSqlDatabase()
                : instance.getLocalDatabase();

        query.append("`").append(database.getTablePrefix()).append(this.table).append("`");

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

    public boolean execute() {
        CompletableFuture<Boolean> compFuture = CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = buildStatement()) {
                stmt.executeUpdate();
                return true;
            } catch (SQLException ex) {
                Debug.captureException(ex);
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
