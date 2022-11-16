package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.utils.Debug;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class Migration {
    private final File pluginFolder;
    private final SQLDatabase database;

    public Migration() {
        this.pluginFolder = FileManager.getPluginFolder();
        this.database = getInstance().getSqlDatabase();
    }

    public void download() {
        if (getInstance().getStorageType().equals(StorageType.LOCAL)) {
            throw new RuntimeException("Please enable sql in 'config.yml'!");
        }
        try {
            createFileBackup();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        downloadGroups();
        downloadSubgroups();
        downloadUserData();
    }

    private void downloadGroups() {
        GroupsData groupsData = new GroupsData();

        try (ResultSet groupsResult = database.getValue("SELECT * FROM `%p%groups`")) {
            while (groupsResult.next()) {
                String key = "groups." + groupsResult.getString("group");
                groupsData.set(key + ".prefix", groupsResult.getString("prefix"));
                groupsData.set(key + ".suffix", groupsResult.getString("suffix"));
                groupsData.set(key + ".chat-color", groupsResult.getString("chat_color"));
                groupsData.set(key + ".chat-formatting", groupsResult.getString("chat_formatting"));
                groupsData.set(key + ".join-msg", groupsResult.getString("join_msg"));
                groupsData.set(key + ".quit-msg", groupsResult.getString("quit_msg"));
                groupsData.save();
            }
        } catch (SQLException e) {
            Debug.handleException(e);
        }
    }

    private void downloadSubgroups() {
        GroupsData groupsData = new GroupsData();

        try (ResultSet groupsResult = database.getValue("SELECT * FROM `%p%subgroups`")) {
            while (groupsResult.next()) {
                String key = "subgroups." + groupsResult.getString("group");
                groupsData.set(key + ".prefix", groupsResult.getString("prefix"));
                groupsData.set(key + ".suffix", groupsResult.getString("suffix"));
                groupsData.save();
            }
        } catch (SQLException e) {
            Debug.handleException(e);
        }
    }

    private void downloadUserData() {
        LocalDatabase localDatabase = new LocalDatabase();
        localDatabase.connect();

        try (ResultSet groupsResult = database.getValue("SELECT * FROM `%p%users`")) {
            while (groupsResult.next()) {
                PreparedStatement stmt = localDatabase.getConnection().prepareStatement("INSERT INTO `users` (`uuid`, `username`, `group`, `force_group`, `subgroup`, `custom_prefix`, `custom_prefix_update`, `custom_suffix`, `custom_suffix_update`, `chat_color`, `chat_formatting`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                stmt.setObject(1, groupsResult.getString("uuid"));
                stmt.setObject(2, groupsResult.getString("username"));
                stmt.setObject(3, groupsResult.getString("group"));
                stmt.setBoolean(4, groupsResult.getBoolean("force_group"));
                stmt.setObject(5, groupsResult.getString("subgroup"));
                stmt.setObject(6, groupsResult.getString("custom_prefix"));
                stmt.setObject(7, groupsResult.getString("custom_prefix_update"));
                stmt.setObject(8, groupsResult.getString("custom_suffix"));
                stmt.setObject(9, groupsResult.getString("custom_suffix_update"));
                stmt.setObject(10, groupsResult.getString("chat_color"));
                stmt.setObject(11, groupsResult.getString("chat_formatting"));
                try {
                    stmt.execute();
                } catch (SQLException ignored) {
                    Debug.warn("Couldn't migrate data for user '" + groupsResult.getString("uuid") + "'");
                }
            }
        } catch (SQLException e) {
            Debug.handleException(e);
        }
        localDatabase.close();
    }

    private void createFileBackup() throws IOException {
        File fileGroups = new File(pluginFolder, "groups.yml");
        File fileUserDatabase = new File(pluginFolder, "storage.db");
        if (!fileGroups.exists() && !fileUserDatabase.exists()) return;

        File dir = new File(this.pluginFolder, "Backup " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")));
        if (!dir.mkdirs()) return;

        if (fileGroups.exists()) {
            Files.copy(fileGroups.toPath(), new File(dir.getAbsolutePath(), "groups.yml").toPath());
        }
        if (fileUserDatabase.exists()) {
            Files.copy(fileUserDatabase.toPath(), new File(dir.getAbsolutePath(), "storage.db").toPath());
            if (!fileUserDatabase.delete()) {
                Debug.warn("Couldn't delete file 'storage.db'!");
            }
        }
    }

    private EasyPrefix getInstance() {
        return EasyPrefix.getInstance();
    }

}
