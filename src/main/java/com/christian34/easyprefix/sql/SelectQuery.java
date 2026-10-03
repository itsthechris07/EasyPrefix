package com.christian34.easyprefix.sql;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.sql.database.Database;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Builds and runs a SELECT query and returns the first row.
 *
 * @author Christian34
 */
public class SelectQuery {
    private final Map<String, String> conditions = new HashMap<>();
    private final String table;
    private List<String> columns;
    private Data data = null;
    private Database database = null;

    public SelectQuery(String table, String... columns) {
        this.table = table;
        this.columns = new ArrayList<>(Arrays.asList(columns));
    }

    public SelectQuery setDatabase(Database database) {
        this.database = database;
        return this;
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

        this.data = new Data(retrieveData());
        return data;
    }

    private HashMap<String, Object> retrieveData() {
        HashMap<String, Object> map = new HashMap<>();
        Database db = database();
        try (Connection connection = db.getConnection();
             PreparedStatement stmt = prepareStatement(connection, db);
             ResultSet result = stmt.executeQuery()) {
            if (result.next()) {
                for (String key : columns) {
                    map.put(key, result.getString(key));
                }
            }
        } catch (SQLException ex) {
            Debug.catchException(ex);
        }
        return map;
    }

    private Database database() {
        if (this.database != null) return this.database;
        EasyPrefix instance = EasyPrefix.getInstance();
        return instance.getStorageType() == StorageType.SQL ? instance.getSqlDatabase() : instance.getLocalDatabase();
    }

    private PreparedStatement prepareStatement(Connection connection, Database db) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT ");

        for (int i = 0; i < columns.size(); i++) {
            query.append("`").append(columns.get(i)).append("`");
            if (i != columns.size() - 1) {
                query.append(", ");
            }
        }

        query.append(" FROM `").append(db.getTablePrefix()).append(this.table).append("`");

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

        PreparedStatement stmt = connection.prepareStatement(query.toString());
        i = 1;
        for (String value : conditions.values()) {
            stmt.setObject(i, value);
            i++;
        }
        return stmt;
    }

}
