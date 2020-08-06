package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.DataStatement;
import com.christian34.easyprefix.database.Database;
import com.christian34.easyprefix.database.Query;
import com.christian34.easyprefix.database.StorageType;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class UserData {
    private final UUID uniqueId;
    private final Database database;
    private HashMap<String, String> data;

    UserData(UUID uniqueId) {
        this.uniqueId = uniqueId;
        EasyPrefix instance = EasyPrefix.getInstance();
        if (instance.getStorageType() == StorageType.SQL) {
            this.database = instance.getSqlDatabase();
        } else {
            this.database = instance.getLocalDatabase();
            updateData();
        }
        loadData();
    }

    void loadData() {
        Query query = new Query("users").setRow("group", "username", "force_group", "subgroup", "custom_prefix", "custom_prefix_update", "custom_suffix", "custom_suffix_update", "gender", "chat_color", "chat_formatting").setCondition("`uuid` = '" + uniqueId.toString() + "'");
        this.data = database.getData(query);
        if (data == null) {
            database.update("INSERT INTO `%p%users`(`uuid`) VALUES ('" + this.uniqueId.toString() + "')");
            loadData();
            return;
        }
        String username = data.get("username");
        OfflinePlayer op = Bukkit.getOfflinePlayer(uniqueId);
        if (username == null || !username.equals(op.getName())) {
            Messages.log("Updating username for player " + op.getName());
            DataStatement stmt = new DataStatement("UPDATE `%p%users` SET `username`=? WHERE `uuid`=?");
            stmt.setObject(1, op.getName());
            stmt.setObject(2, uniqueId.toString());
            if (!stmt.execute()) {
                stmt.getException().printStackTrace();
            }
        }
    }

    String getString(String key) {
        return data.get(key);
    }

    boolean getBoolean(String key) {
        String obj = data.get(key);
        if (obj == null) {
            return false;
        } else return obj.equals("1");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void updateData() {
        UserDataFile userDataFile = new UserDataFile(uniqueId);
        if (userDataFile.getFile() == null || userDataFile.getFileData() == null) return;
        OfflinePlayer op = Bukkit.getOfflinePlayer(uniqueId);
        Messages.log("Updating " + op.getName() + "´s data...");
        if (!database.exists("SELECT `uuid` FROM `%p%users` WHERE `uuid` = '" + this.uniqueId.toString() + "'")) {
            Messages.log("Creating database for user...");
            database.update("INSERT INTO `%p%users`(`uuid`) VALUES ('" + this.uniqueId.toString() + "')");
        }
        List<String> rows = Arrays.asList("group", "subgroup", "custom_prefix", "gender", "chat_color", "chat_formatting", "custom_suffix", "custom_prefix");
        for (String row : rows) {
            try {
                DataStatement stmt = new DataStatement("UPDATE `%p%users` SET `" + row + "`=? WHERE `uuid`=?");
                stmt.setObject(1, userDataFile.getFileData().getString(row.replace("_", "-")));
                stmt.setObject(2, uniqueId.toString());
                if (!stmt.execute()) {
                    stmt.getException().printStackTrace();
                }
            } catch (Exception ex) {
                Messages.log("§cAn exception occurred while updating " + op.getName() + "´s data...");
                Messages.log("§cError: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        if (userDataFile.getFileData().getBoolean("force-group")) {
            DataStatement stmt = new DataStatement("UPDATE `%p%users` SET `force_group`=? WHERE `uuid`=?");
            stmt.setObject(1, 1);
            stmt.setObject(2, uniqueId.toString());
            if (!stmt.execute()) {
                stmt.getException().printStackTrace();
            }
        }
        File backupDir = new File(FileManager.getPluginFolder() + "/user/backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        userDataFile.getFile().renameTo(new File(backupDir, uniqueId.toString() + ".yml"));
        Messages.log("§aData has been updated!\n ");
    }

}
