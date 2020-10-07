package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.*;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.messages.Messages;
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
    private final Database database;
    private final OfflinePlayer player;
    private Data data;

    UserData(UUID uniqueId) {
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
                Debug.captureException(ex);
            }
        }
        loadData();
    }

    void loadData() {
        SelectQuery selectQuery = new SelectQuery("users", "group", "username", "force_group", "subgroup", "custom_prefix",
                "custom_prefix_update", "custom_suffix", "custom_suffix_update", "gender", "chat_color",
                "chat_formatting").addCondition("uuid", uniqueId.toString());
        this.data = selectQuery.getData();
        String username = Bukkit.getOfflinePlayer(uniqueId).getName();
        if (data.isEmpty()) {
            database.update("INSERT INTO `users`(`uuid`, `username`) VALUES ('" + uniqueId.toString() + "', '" + username + "')");
        } else {
            if (player != null && username != null) {
                if (!username.equals(data.getString("username"))) {
                    database.update("UPDATE `users` SET `username` = '" + player.getName() + "'");
                }
            }
        }
    }

    String getString(String key) {
        return data.getString(key);
    }

    boolean getBoolean(String key) {
        return data.getBoolean(key);
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
        if (userDataFile.getFile().renameTo(new File(backupDir, uniqueId.toString() + ".yml"))) {
            userDataFile.getFile().delete();
        }
        Messages.log("§aData has been updated!");
    }

}
