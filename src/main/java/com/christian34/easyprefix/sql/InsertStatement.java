package com.christian34.easyprefix.sql;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.sql.database.Database;
import com.christian34.easyprefix.sql.database.DuplicateEntryException;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.TaskManager;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * EasyPrefix 2026.
 * <p>
 * Builds and runs an INSERT statement on a table of the current storage.
 *
 * @author Christian34
 */
public class InsertStatement {
    private final Database database;
    private final EasyPrefix instance;
    private final String table;
    private final Map<String, Object> values;

    public InsertStatement(String table) {
        this.table = table;
        this.values = new HashMap<>();
        this.instance = EasyPrefix.getInstance();
        this.database = instance.getStorageType() == StorageType.SQL
                ? instance.getSqlDatabase()
                : instance.getLocalDatabase();
    }

    public InsertStatement setValue(String column, Object value) {
        values.put(column, value);
        return this;
    }

    public boolean execute() throws DuplicateEntryException {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = buildStatement(connection)) {
            stmt.executeUpdate();
            if (database instanceof SQLDatabase sql) sql.announce(table, values.get("uuid"));
            return true;
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new DuplicateEntryException(table, String.valueOf(ex.getMessage()));
        } catch (SQLException ex) {
            if (String.valueOf(ex.getMessage()).startsWith("[SQLITE_CONSTRAINT]")) {
                throw new DuplicateEntryException(table, "constraint violation");
            } else if (instance.getStorageType() == StorageType.LOCAL) {
                if (String.valueOf(ex.getMessage()).startsWith("[SQLITE_READONLY")) {
                    Debug.warn("************************************************************");
                    Debug.warn("* WARNING: File 'storage.db' is not writable!");
                    Debug.warn("* stopping plugin...");
                    Debug.warn("************************************************************");
                    TaskManager.global(() -> Bukkit.getPluginManager().disablePlugin(instance));
                    return false;
                }
            }
            Debug.catchException(ex);
        }
        return false;
    }

    private PreparedStatement buildStatement(Connection connection) throws SQLException {
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append("`").append(database.getTablePrefix()).append(this.table).append("`");

        int i = 1;
        for (String column : values.keySet()) {
            if (i == 1) {
                query.append("(");
            } else {
                query.append(", ");
            }
            query.append("`").append(column).append("`");
            i++;
        }
        query.append(") VALUES (");

        for (int slot = 1; slot <= values.size(); slot++) {
            if (slot != 1) {
                query.append(", ");
            }
            query.append("?");
            if (slot == values.size()) {
                query.append(")");
            }
        }

        PreparedStatement stmt = connection.prepareStatement(query.toString());
        i = 1;
        for (Object value : values.values()) {
            stmt.setObject(i, value);
            i++;
        }

        return stmt;
    }

}
