package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

/**
 * EasyPrefix 2026.
 * <p>
 * Copies all data between the MySQL database and the local files (groups.yml, storage.db).
 *
 * @author Christian34
 */
public final class Migration {
    private final SQLDatabase database;

    public Migration() {
        this(EasyPrefix.getInstance().getSqlDatabase());
    }

    Migration(SQLDatabase database) {
        this.database = database;
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

    /**
     * copies the local data (groups.yml, storage.db) into the MySQL database - groups, tags and users that exist in
     * both are overwritten with the local ones, everything else in the database is kept
     */
    public UploadResult upload() {
        if (getInstance().getStorageType().equals(StorageType.LOCAL)) {
            throw new RuntimeException("Please enable sql in 'config.yml'!");
        }
        GroupsData groupsData = new GroupsData();
        try (Connection connection = database.getConnection()) {
            int groups = uploadGroups(connection, groupsData.getSection("groups"));
            int tags = uploadSubgroups(connection, groupsData.getSection("subgroups"));
            int users = uploadUserData(connection);
            // the other servers load everything again
            database.announce(SQLSynchronizer.Type.GROUPS, null);
            return new UploadResult(groups, tags, users);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public record UploadResult(int groups, int tags, int users) {
    }

    /**
     * fills a new database with the example groups and tags of the bundled groups.yml - only if there are no groups
     * yet (the default group can't be removed, so the database is new or its tables were deleted)
     *
     * @return true if the examples were added
     */
    boolean uploadExamples() throws SQLException, IOException {
        try (Connection connection = database.getConnection()) {
            if (!readNames(connection, "groups").isEmpty()) return false;
            YamlConfiguration examples;
            try (InputStream stream = Migration.class.getResourceAsStream("/groups.yml")) {
                if (stream == null) throw new IOException("groups.yml is missing in the plugin jar");
                examples = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
            uploadGroups(connection, examples.getConfigurationSection("groups"));
            // tags can be removed on purpose
            if (readNames(connection, "subgroups").isEmpty()) {
                uploadSubgroups(connection, examples.getConfigurationSection("subgroups"));
            }
            return true;
        }
    }

    private int uploadGroups(Connection connection, @Nullable ConfigurationSection section) throws SQLException {
        if (section == null) return 0;
        int count = 0;
        try (PreparedStatement stmt = connection.prepareStatement(upsert("groups", "group", "priority", "prefix",
                "suffix", "chat_color", "chat_formatting", "join_msg", "quit_msg", "hover"))) {
            for (String name : section.getKeys(false)) {
                ConfigurationSection group = section.getConfigurationSection(name);
                if (group == null) continue;
                stmt.setString(1, name);
                stmt.setInt(2, group.getInt("priority", name.equals("default") ? 0 : 1));
                stmt.setString(3, group.getString("prefix"));
                stmt.setString(4, group.getString("suffix"));
                stmt.setString(5, group.getString("chat-color"));
                stmt.setString(6, group.getString("chat-formatting"));
                stmt.setString(7, group.getString("join-msg"));
                stmt.setString(8, group.getString("quit-msg"));
                // mysql stores the lines in one column
                stmt.setString(9, group.isList("hover") ? String.join("\n", group.getStringList("hover")) : null);
                stmt.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private int uploadSubgroups(Connection connection, @Nullable ConfigurationSection section) throws SQLException {
        if (section == null) return 0;
        int count = 0;
        try (PreparedStatement stmt = connection.prepareStatement(upsert("subgroups", "group", "prefix", "suffix"))) {
            for (String name : section.getKeys(false)) {
                ConfigurationSection subgroup = section.getConfigurationSection(name);
                if (subgroup == null) continue;
                stmt.setString(1, name);
                stmt.setString(2, subgroup.getString("prefix"));
                stmt.setString(3, subgroup.getString("suffix"));
                stmt.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private int uploadUserData(Connection connection) throws SQLException {
        File file = new File(FileManager.getPluginFolder(), "storage.db");
        if (!file.exists()) return 0;
        // users.group and users.subgroup reference the groups (foreign keys), unknown ones are left out
        Map<String, String> groups = readNames(connection, "groups");
        Map<String, String> subgroups = readNames(connection, "subgroups");

        LocalDatabase localDatabase = new LocalDatabase(file);
        if (!localDatabase.connect()) throw new SQLException("Couldn't open " + file.getName());
        try (PreparedStatement stmt = connection.prepareStatement(upsert("users", "uuid", "username", "group",
                "force_group", "subgroup", "custom_prefix", "custom_prefix_update", "custom_suffix",
                "custom_suffix_update", "chat_color", "chat_formatting", "mentions_disabled"))) {
            return localDatabase.query("SELECT * FROM `users`", result -> {
                int count = 0;
                while (result.next()) {
                    String group = result.getString("group");
                    String subgroup = result.getString("subgroup");
                    stmt.setString(1, result.getString("uuid"));
                    stmt.setString(2, result.getString("username"));
                    stmt.setString(3, group == null ? null : groups.get(group.toLowerCase(Locale.ROOT)));
                    stmt.setBoolean(4, result.getBoolean("force_group"));
                    stmt.setString(5, subgroup == null ? null : subgroups.get(subgroup.toLowerCase(Locale.ROOT)));
                    stmt.setString(6, result.getString("custom_prefix"));
                    stmt.setString(7, result.getString("custom_prefix_update"));
                    stmt.setString(8, result.getString("custom_suffix"));
                    stmt.setString(9, result.getString("custom_suffix_update"));
                    stmt.setString(10, result.getString("chat_color"));
                    stmt.setString(11, result.getString("chat_formatting"));
                    stmt.setBoolean(12, result.getBoolean("mentions_disabled"));
                    try {
                        stmt.executeUpdate();
                        count++;
                    } catch (SQLException ex) {
                        Debug.warn("Couldn't upload data of user '" + result.getString("uuid") + "': " + ex.getMessage());
                    }
                }
                return count;
            });
        } finally {
            localDatabase.close();
        }
    }

    /**
     * @return the names of the groups in the database by their lower case name, as the plugin ignores the case
     */
    private Map<String, String> readNames(Connection connection, String table) throws SQLException {
        Map<String, String> names = new HashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT `group` FROM `" + database.getTablePrefix() + table + "`")) {
            while (result.next()) {
                String name = result.getString(1);
                names.put(name.toLowerCase(Locale.ROOT), name);
            }
        }
        return names;
    }

    /**
     * INSERT ... ON DUPLICATE KEY UPDATE with all columns but the first (the key)
     */
    private String upsert(String table, String... columns) {
        StringJoiner names = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");
        StringJoiner updates = new StringJoiner(", ");
        for (int i = 0; i < columns.length; i++) {
            names.add("`" + columns[i] + "`");
            values.add("?");
            // VALUES() is deprecated in mysql 8.0.20, but its replacement does not work on mariadb
            if (i > 0) updates.add("`" + columns[i] + "` = VALUES(`" + columns[i] + "`)");
        }
        return "INSERT INTO `" + database.getTablePrefix() + table + "` (" + names + ") VALUES (" + values
                + ") ON DUPLICATE KEY UPDATE " + updates;
    }

    private void downloadGroups() {
        GroupsData groupsData = new GroupsData();
        // the groups of the database replace the local ones (they are in the backup)
        groupsData.set("groups", null);
        try {
            database.query("SELECT * FROM `%p%groups`", result -> {
                while (result.next()) {
                    String key = "groups." + result.getString("group");
                    groupsData.set(key + ".priority", result.getInt("priority"));
                    groupsData.set(key + ".prefix", result.getString("prefix"));
                    groupsData.set(key + ".suffix", result.getString("suffix"));
                    groupsData.set(key + ".chat-color", result.getString("chat_color"));
                    groupsData.set(key + ".chat-formatting", result.getString("chat_formatting"));
                    groupsData.set(key + ".join-msg", result.getString("join_msg"));
                    groupsData.set(key + ".quit-msg", result.getString("quit_msg"));
                    String hover = result.getString("hover");
                    groupsData.set(key + ".hover", hover == null ? null : java.util.List.of(hover.split("\n", -1)));
                }
                return null;
            });
            groupsData.save();
        } catch (SQLException e) {
            Debug.handleException(e);
        }
    }

    private void downloadSubgroups() {
        GroupsData groupsData = new GroupsData();
        groupsData.set("subgroups", null);
        try {
            database.query("SELECT * FROM `%p%subgroups`", result -> {
                while (result.next()) {
                    String key = "subgroups." + result.getString("group");
                    groupsData.set(key + ".prefix", result.getString("prefix"));
                    groupsData.set(key + ".suffix", result.getString("suffix"));
                }
                return null;
            });
            groupsData.save();
        } catch (SQLException e) {
            Debug.handleException(e);
        }
    }

    private void downloadUserData() {
        LocalDatabase localDatabase = new LocalDatabase();
        if (!localDatabase.connect()) return;

        try (Connection local = localDatabase.getConnection()) {
            database.query("SELECT * FROM `%p%users`", result -> {
                while (result.next()) {
                    try (PreparedStatement stmt = local.prepareStatement("INSERT INTO `users` (`uuid`, `username`, `group`, `force_group`, `subgroup`, `custom_prefix`, `custom_prefix_update`, `custom_suffix`, `custom_suffix_update`, `chat_color`, `chat_formatting`, `mentions_disabled`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        stmt.setObject(1, result.getString("uuid"));
                        stmt.setObject(2, result.getString("username"));
                        stmt.setObject(3, result.getString("group"));
                        stmt.setBoolean(4, result.getBoolean("force_group"));
                        stmt.setObject(5, result.getString("subgroup"));
                        stmt.setObject(6, result.getString("custom_prefix"));
                        stmt.setObject(7, result.getString("custom_prefix_update"));
                        stmt.setObject(8, result.getString("custom_suffix"));
                        stmt.setObject(9, result.getString("custom_suffix_update"));
                        stmt.setObject(10, result.getString("chat_color"));
                        stmt.setObject(11, result.getString("chat_formatting"));
                        stmt.setBoolean(12, result.getBoolean("mentions_disabled"));
                        stmt.execute();
                    } catch (SQLException ignored) {
                        Debug.warn("Couldn't migrate data for user '" + result.getString("uuid") + "'");
                    }
                }
                return null;
            });
        } catch (SQLException e) {
            Debug.handleException(e);
        } finally {
            localDatabase.close();
        }
    }

    private void createFileBackup() throws IOException {
        File fileGroups = new File(FileManager.getPluginFolder(), "groups.yml");
        File fileUserDatabase = new File(FileManager.getPluginFolder(), "storage.db");
        if (!fileGroups.exists() && !fileUserDatabase.exists()) return;

        File dir = new File(FileManager.getPluginFolder(), "Backup " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")));
        if (!dir.mkdirs() && !dir.isDirectory()) throw new IOException("Couldn't create the backup folder " + dir.getName());

        if (fileGroups.exists()) {
            Files.copy(fileGroups.toPath(), new File(dir, "groups.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        if (fileUserDatabase.exists()) {
            Files.copy(fileUserDatabase.toPath(), new File(dir, "storage.db").toPath(), StandardCopyOption.REPLACE_EXISTING);
            // the users are downloaded into a new, empty storage
            Files.delete(fileUserDatabase.toPath());
        }
    }

    private EasyPrefix getInstance() {
        return EasyPrefix.getInstance();
    }

}
