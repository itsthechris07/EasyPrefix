package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.sql.Data;
import com.christian34.easyprefix.sql.InsertStatement;
import com.christian34.easyprefix.sql.SelectQuery;
import com.christian34.easyprefix.sql.UpdateStatement;
import com.christian34.easyprefix.sql.database.Database;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class UserData {
    private final UUID uniqueId;
    private final OfflinePlayer player;
    private Database database;
    private Data data;

    public UserData(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.player = Bukkit.getOfflinePlayer(uniqueId);
        EasyPrefix instance = EasyPrefix.getInstance();
        if (instance.getStorageType() == StorageType.SQL) {
            this.database = instance.getSqlDatabase();
        } else {
            this.database = instance.getLocalDatabase();
            try {
                updateData();
            } catch (Exception ex) {
                Debug.handleException(ex);
            }
        }
    }

    public Data getData() {
        return data;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void loadData() {
        SelectQuery selectQuery = new SelectQuery("users", "group", "username", "force_group", "subgroup", "custom_prefix",
                "custom_prefix_update", "custom_suffix", "custom_suffix_update", "gender", "chat_color",
                "chat_formatting").addCondition("uuid", uniqueId.toString()).setDatabase(database);
        this.data = selectQuery.getData();
        String username = Bukkit.getOfflinePlayer(uniqueId).getName();
        if (data.isEmpty()) {
            InsertStatement insertStatement = new InsertStatement("users")
                    .setValue("uuid", uniqueId.toString())
                    .setValue("username", username);
            if (!insertStatement.execute()) {
                Debug.log("§cCouldn't update database! Error UDDB3");
            }
        } else {
            if (player != null && username != null) {
                if (!username.equals(data.getString("username"))) {
                    UpdateStatement updateStatement = new UpdateStatement("users")
                            .addCondition("uuid", player.getUniqueId().toString())
                            .setValue("username", player.getName());
                    if (!updateStatement.execute()) {
                        Debug.log("§cCouldn't update username for player '" + player.getName() + "'!");
                    }
                }
            }
        }
    }

    public String getString(String key) {
        return data.getString(key);
    }

    public boolean getBoolean(String key) {
        return data.getBoolean(key);
    }

    @SuppressWarnings("deprecation")
    private void updateData() {
        UserDataFile userDataFile = new UserDataFile(uniqueId);
        if (userDataFile.getFile() == null || userDataFile.getFileData() == null) return;
        Debug.recordAction("updating user data for user '" + uniqueId + "'");
        OfflinePlayer op = Bukkit.getOfflinePlayer(uniqueId);
        Debug.log("Updating " + op.getName() + "´s data...");

        SelectQuery selectQuery = new SelectQuery("users", "uuid").addCondition("uuid", this.uniqueId.toString());
        if (selectQuery.getData().isEmpty()) {
            Debug.log("Creating database for user...");
            InsertStatement insertStatement = new InsertStatement("users").setValue("uuid", this.uniqueId.toString());
            if (!insertStatement.execute()) {
                Debug.log("Couldn't save data to database! Error UDDB4");
            }
        }
        List<String> rows = Arrays.asList("group", "subgroup", "custom_prefix", "gender", "chat_color", "chat_formatting", "custom_suffix", "custom_prefix");
        for (String row : rows) {
            try {
                UpdateStatement updateStatement = new UpdateStatement("users")
                        .addCondition("uuid", uniqueId.toString())
                        .setValue(row, userDataFile.getFileData().getString(row.replace("_", "-")));
                if (!updateStatement.execute()) {
                    Debug.log("Couldn't save data to database! Error UDDB1");
                }
            } catch (Exception ex) {
                Debug.log("§cAn exception occurred while updating " + op.getName() + "´s data...");
                Debug.handleException(ex);
            }
        }
        if (userDataFile.getFileData().getBoolean("force-group")) {
            UpdateStatement updateStatement = new UpdateStatement("users")
                    .addCondition("uuid", uniqueId.toString())
                    .setValue("force_group", 1);
            if (!updateStatement.execute()) {
                Debug.log("Couldn't save data to database! Error UDDB2");
            }
        }
        File backupDir = new File(FileManager.getPluginFolder() + "/user/backup");
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                Debug.log("§cCouldn't create backup folder!");
            }
        }
        if (userDataFile.getFile().renameTo(new File(backupDir, uniqueId.toString() + ".yml"))) {
            if (!userDataFile.getFile().delete()) {
                Debug.log("§cCouldn't delete user data for '" + uniqueId + "'!");
            }
        }
        Debug.log("§aData has been updated!");
    }

}
