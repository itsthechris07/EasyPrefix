package com.christian34.easyprefix;

import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.UserData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.Set;
import java.util.UUID;

public class Database {
    private String host, database, username, tablePrefix, password;
    private Connection connection;
    private int port;

    Database() {
        FileConfiguration config = FileManager.getConfig().getFileData();
        this.host = config.getString(ConfigData.Values.SQL_HOST.toString());
        this.database = config.getString(ConfigData.Values.SQL_DATABASE.toString());
        this.username = config.getString(ConfigData.Values.SQL_USERNAME.toString());
        this.password = config.getString(ConfigData.Values.SQL_PASSWORD.toString());
        this.tablePrefix = config.getString(ConfigData.Values.SQL_TABLE_PREFIX.toString());
        this.port = config.getInt(ConfigData.Values.SQL_PORT.toString());
        if (tablePrefix == null || tablePrefix.isEmpty()) {
            this.tablePrefix = "";
        } else if (!this.tablePrefix.endsWith("_")) this.tablePrefix += "_";
        connect();
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        synchronized(this) {
            try {
                if (getConnection() != null && !getConnection().isClosed()) {
                    getConnection().close();
                }
            } catch(SQLException ignored) {
            }
        }
    }

    private void connect() {
        synchronized(this) {
            try {
                if (connection != null && !connection.isClosed()) return;
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
                createTables();
            } catch(SQLSyntaxErrorException e) {
                Messages.log("§cDatabase '" + database + "' does not exist!");
            } catch(SQLException e) {
                Messages.log("§cAccess denied for user '" + this.username + "'@'" + this.host + "'");
                Messages.log("§cPlease check if the sql server is running and you entered the right username and password.");
            } catch(ClassNotFoundException e) {
                Messages.log("§cYour installation does not support sql!");
            }
        }
    }

    public void update(String statement) {
        try {
            if (connection.isClosed()) connect();
            statement = statement.replace("%p%", getTablePrefix());
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(statement);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean exists(String statement) {
        try {
            if (connection.isClosed()) connect();
            statement = statement.replace("%p%", getTablePrefix());
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery(statement);
            return result.next();
        } catch(SQLException e) {
            Messages.log("§cCouldn't get value from statement '" + statement + "'!");
            Messages.log("§c" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public ResultSet getValue(String statement) {
        try {
            if (connection.isClosed()) connect();
            statement = statement.replace("%p%", getTablePrefix());
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(statement);
        } catch(SQLException e) {
            Messages.log("§cCouldn't get value from statement '" + statement + "'!");
            Messages.log("§c" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void createTables() {
        update("CREATE TABLE IF NOT EXISTS `%p%users` (`uuid` CHAR(36) NOT NULL, `group` VARCHAR(64) NULL DEFAULT NULL, `force_group` BOOLEAN NULL DEFAULT NULL, `subgroup` VARCHAR(64) NULL DEFAULT NULL, `custom_prefix` VARCHAR(128) NULL DEFAULT NULL, `custom_suffix` VARCHAR(128) NULL DEFAULT NULL, `gender` VARCHAR(32) NULL DEFAULT NULL, `chat_color` CHAR(2) NULL DEFAULT NULL, `chat_formatting` CHAR(2) NULL DEFAULT NULL, PRIMARY KEY(`uuid`))ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update("CREATE TABLE IF NOT EXISTS `%p%groups` (`group` VARCHAR(64) not null, UNIQUE(`group`), prefix VARCHAR(128) default NULL null, suffix VARCHAR(128) default NULL null, chat_color CHAR(2) default NULL null, chat_formatting CHAR(2) default NULL null, join_msg VARCHAR(255) default NULL null, quit_msg VARCHAR(255) default NULL null)ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update("CREATE TABLE IF NOT EXISTS `%p%genders` ( `id` INT NOT NULL AUTO_INCREMENT , `type` INT(1) NOT NULL , `group_name` VARCHAR(64) NOT NULL , `gender` VARCHAR(32) NOT NULL , `prefix` VARCHAR(128) default NULL null , `suffix` VARCHAR(128) default NULL null , PRIMARY KEY (`id`)) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update("CREATE TABLE IF NOT EXISTS `%p%subgroups` ( `group` VARCHAR(64) NOT NULL , UNIQUE(`group`), `prefix` VARCHAR(128) default NULL null , `suffix` VARCHAR(128) default NULL null ) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
    }

    public PreparedStatement prepareStatement(String sql) {
        try {
            sql = sql.replace("%p%", getTablePrefix());
            return getConnection().prepareStatement(sql);
        } catch(SQLException e) {
            return null;
        }
    }

    public void uploadGroups() throws SQLException {
        FileConfiguration data = FileManager.getGroups().getFileData();
        Set<String> groups = data.getConfigurationSection("groups").getKeys(false);
        for (String groupName : groups) {
            try {
                String sql = "INSERT INTO `%p%groups`(`group`) VALUES (?)";
                PreparedStatement stmt = prepareStatement(sql);
                stmt.setString(1, groupName);
                stmt.executeUpdate();
                Messages.log("§7Uploaded group '" + groupName + "' to database!");
            } catch(SQLIntegrityConstraintViolationException ignored) {
            }

            String sql = "UPDATE `%p%groups` SET `prefix`= ?,`suffix`= ?,`chat_color`= ?,`chat_formatting`= ?," + "`join_msg`= ?,`quit_msg`= ? WHERE `group` = ?";
            PreparedStatement stmt = prepareStatement(sql);

            String prefix = data.getString("groups." + groupName + ".prefix");
            if (prefix != null) {
                stmt.setString(1, prefix);
            } else stmt.setNull(1, Types.VARCHAR);

            String suffix = data.getString("groups." + groupName + ".suffix");
            if (suffix != null) {
                stmt.setString(2, suffix);
            } else stmt.setNull(2, Types.VARCHAR);

            String chatcolor = data.getString("groups." + groupName + ".chat-color");

            if (chatcolor != null && chatcolor.length() >= 2) {
                stmt.setString(3, chatcolor.substring(1, 2));
            } else stmt.setNull(3, Types.VARCHAR);

            String chatformatting = data.getString("groups." + groupName + ".chat-formatting");
            if (chatformatting != null && chatformatting.length() >= 2) {
                stmt.setString(4, chatformatting.substring(1, 2));
            } else stmt.setNull(4, Types.VARCHAR);

            String joinMessage = data.getString("groups." + groupName + ".join-msg");
            if (joinMessage != null) {
                stmt.setString(5, joinMessage);
            } else stmt.setNull(5, Types.VARCHAR);

            String quitMessage = data.getString("groups." + groupName + ".quit-msg");
            if (quitMessage != null) {
                stmt.setString(6, quitMessage);
            } else stmt.setNull(6, Types.VARCHAR);

            ConfigurationSection section = data.getConfigurationSection("groups." + groupName + ".genders");
            if (section != null) {
                Set<String> genderTypes = section.getKeys(false);
                for (String gender : genderTypes) {
                    String path = "groups." + groupName + ".genders." + gender + ".";
                    String pref = data.getString(path + "prefix");
                    String suf = data.getString(path + "suffix");

                    String sql2 = "SELECT `id` FROM `%p%genders` WHERE `type` = ? AND `gender` = ? AND `group_name` = ?";
                    PreparedStatement stmt2 = prepareStatement(sql2);
                    stmt2.setInt(1, 0);
                    stmt2.setString(2, gender);
                    stmt2.setString(3, groupName);
                    ResultSet result = stmt2.executeQuery();
                    String sql3;
                    PreparedStatement stmt3;
                    if (!result.next()) {
                        sql3 = "INSERT INTO `%p%genders`(`gender`, `type`, `group_name`, `prefix`, `suffix`) VALUES (?," + " ?, ?, ?, ?)";
                        stmt3 = prepareStatement(sql3);
                        stmt3.setString(1, gender);
                        stmt3.setInt(2, 0);
                        stmt3.setString(3, groupName);
                        stmt3.setString(4, pref);
                        stmt3.setString(5, suf);
                        stmt3.executeUpdate();
                    } else {
                        sql3 = "UPDATE `%p%genders` SET `prefix`=?,`suffix`=? WHERE `type` = ? AND `gender` = ? AND " + "`group_name` = ?";
                        stmt3 = prepareStatement(sql3);
                        stmt3.setString(1, pref);
                        stmt3.setString(2, suf);
                        stmt3.setInt(3, 0);
                        stmt3.setString(4, gender);
                        stmt3.setString(5, groupName);
                        stmt3.executeUpdate();
                    }
                }
            } else {
                String sql2 = "DELETE FROM `%p%genders` WHERE `type` = 0 AND `group_name` = ?";
                PreparedStatement stmt2 = prepareStatement(sql2);
                stmt2.setString(1, groupName);
                stmt2.executeUpdate();
            }
            stmt.setString(7, groupName);
            stmt.executeUpdate();
        }

    }

    private void uploadSubgroups() throws SQLException {
        FileConfiguration data = FileManager.getGroups().getFileData();
        ConfigurationSection mainSection = data.getConfigurationSection("subgroups");
        if (mainSection == null) return;
        Set<String> groups = mainSection.getKeys(false);
        for (String groupName : groups) {
            try {
                PreparedStatement stmt = prepareStatement("INSERT INTO `%p%subgroups`(`group`) VALUES (?)");
                stmt.setString(1, groupName);
                stmt.executeUpdate();
                Messages.log("§7Uploaded subgroup '" + groupName + "' to database!");
            } catch(SQLIntegrityConstraintViolationException ignored) {
            }

            String sql = "UPDATE `%p%subgroups` SET `prefix`= ?,`suffix`= ? WHERE `group` = ?";
            PreparedStatement stmt = prepareStatement(sql);

            String prefix = data.getString("subgroups." + groupName + ".prefix");
            if (prefix != null) {
                stmt.setString(1, prefix);
            } else stmt.setNull(1, Types.VARCHAR);

            String suffix = data.getString("subgroups." + groupName + ".suffix");
            if (suffix != null) {
                stmt.setString(2, suffix);
            } else stmt.setNull(2, Types.VARCHAR);
            stmt.setString(3, groupName);
            stmt.executeUpdate();

            ConfigurationSection section = data.getConfigurationSection("subgroups." + groupName + ".genders");
            if (section != null) {
                Set<String> genderTypes = section.getKeys(false);
                for (String gender : genderTypes) {
                    String path = "subgroups." + groupName + ".genders." + gender + ".";
                    String pref = data.getString(path + "prefix");
                    String suf = data.getString(path + "suffix");

                    String sql2 = "SELECT `id` FROM `%p%genders` WHERE `type` = ? AND `gender` = ? AND `group_name` = ?";
                    PreparedStatement stmt2 = prepareStatement(sql2);
                    stmt2.setInt(1, 1);
                    stmt2.setString(2, gender);
                    stmt2.setString(3, groupName);
                    ResultSet result = stmt2.executeQuery();
                    String sql3;
                    PreparedStatement stmt3;
                    if (!result.next()) {
                        sql3 = "INSERT INTO `%p%genders`(`gender`, `type`, `group_name`, `prefix`, `suffix`) VALUES (?, ?, ?, ?, ?)";
                        stmt3 = prepareStatement(sql3);
                        stmt3.setString(1, gender);
                        stmt3.setInt(2, 1);
                        stmt3.setString(3, groupName);
                        stmt3.setString(4, pref);
                        stmt3.setString(5, suf);
                        stmt3.executeUpdate();
                    } else {
                        sql3 = "UPDATE `%p%genders` SET `prefix`=?,`suffix`=? WHERE `type` = ? AND `gender` = ? AND `group_name` = ?";
                        stmt3 = prepareStatement(sql3);
                        stmt3.setString(1, pref);
                        stmt3.setString(2, suf);
                        stmt3.setInt(3, 1);
                        stmt3.setString(4, gender);
                        stmt3.setString(5, groupName);
                        stmt3.executeUpdate();
                    }
                }
            } else {
                String sql2 = "DELETE FROM `%p%genders` WHERE `type` = 1 AND `group_name` = ?";
                PreparedStatement stmt2 = prepareStatement(sql2);
                stmt2.setString(1, groupName);
                stmt2.executeUpdate();
            }
        }

    }

    private void uploadUsers() throws SQLException {
        File dirUsers = new File(FileManager.getPluginFolder() + "/user");
        File[] listOfFiles = dirUsers.listFiles();
        if (listOfFiles != null) {
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    UUID uuid = UUID.fromString(listOfFile.getName().replace(".yml", ""));
                    UserData userData = new UserData(uuid);
                    String groupName = userData.getFileData().getString("group");
                    String subgroupName = userData.getFileData().getString("subgroup");
                    String chatColor = userData.getFileData().getString("chat-color");
                    String chatFormatting = userData.getFileData().getString("chat-formatting");
                    String cstmPrefix = userData.getFileData().getString("custom-prefix");
                    String cstmSuffix = userData.getFileData().getString("custom-suffix");
                    String gender = userData.getFileData().getString("gender");
                    boolean forceGroup = userData.getFileData().getBoolean("force-group");
                    String sql = "INSERT INTO `%p%users`(`uuid`, `group`, `force_group`, `subgroup`, `custom_prefix`, `custom_suffix`, `gender`, `chat_color`, `chat_formatting`) " + "VALUES (?,?,?,?,?,?,?,?,?)";
                    PreparedStatement stmt = prepareStatement(sql);
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, groupName);
                    stmt.setBoolean(3, forceGroup);
                    stmt.setString(4, subgroupName);
                    stmt.setString(5, cstmPrefix);
                    stmt.setString(6, cstmSuffix);
                    stmt.setString(7, gender);
                    stmt.setString(8, chatColor);
                    stmt.setString(9, chatFormatting);
                    try {
                        stmt.executeUpdate();
                    } catch(SQLIntegrityConstraintViolationException ignored) {
                    }
                }
            }
        }

    }

    public void migrateData() throws SQLException {
        long startTime = System.currentTimeMillis();
        Messages.log("§cMigrating data to SQL...");
        Messages.log("§7loading files...");
        FileManager.load();
        Messages.log("§7creating tables...");
        createTables();
        Messages.log("§7uploading groups...");
        uploadGroups();
        Messages.log("§7uploading subgroups...");
        uploadSubgroups();
        Messages.log("§7uploading users...");
        uploadUsers();
        long ms = System.currentTimeMillis() - startTime;
        Messages.log("§aMigration took " + ms + " ms!");
    }

}