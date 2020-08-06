package com.christian34.easyprefix.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.utils.Debug;

import java.sql.*;
import java.util.HashMap;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class SQLDatabase implements Database {
    private final EasyPrefix instance;
    private final String host;
    private final String database;
    private final String username;
    private final String tablePrefix;
    private final String password;
    private final int port;
    private Connection connection;

    public SQLDatabase(EasyPrefix instance) {
        this.instance = instance;
        this.host = ConfigKeys.SQL_HOST.toString();
        this.database = ConfigKeys.SQL_DATABASE.toString();
        this.username = ConfigKeys.SQL_USERNAME.toString();
        this.password = ConfigKeys.SQL_PASSWORD.toString();
        this.port = ConfigKeys.SQL_PORT.toInt();
        String tPrefix = ConfigKeys.SQL_TABLEPREFIX.toString();
        if (tPrefix == null || tPrefix.isEmpty()) {
            tPrefix = "";
        } else if (!tPrefix.endsWith("_")) tPrefix += "_";
        this.tablePrefix = tPrefix;
    }

    @Override
    public void connect() {
        synchronized (this) {
            try {
                if (connection != null && !connection.isClosed()) return;
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&useUnicode=true&characterEncoding=utf-8", username, password);
                createTables();
            } catch (SQLSyntaxErrorException e) {
                Messages.log("§cDatabase '" + database + "' does not exist!");
            } catch (SQLException e) {
                Messages.log("§cAccess denied for user '" + this.username + "'@'" + this.host + "'");
                Messages.log("§cPlease check if the sql server is running and you entered the right username and password.");
            } catch (ClassNotFoundException e) {
                Messages.log("§cYour installation does not support sql!");
            }
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            try {
                if (getConnection() != null && !getConnection().isClosed()) {
                    getConnection().close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public ResultSet getValue(Query query) {
        try {
            if (connection.isClosed()) connect();
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(query.getStatement());
        } catch (SQLException e) {
            Debug.captureException(e);
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getValue(String statement) {
        try {
            if (connection.isClosed()) connect();
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(statement.replace("%p%", getTablePrefix()));
        } catch (SQLException e) {
            Debug.captureException(e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public HashMap<String, String> getData(Query query) {
        HashMap<String, String> data = new HashMap<>();
        ResultSet result = getValue(query);
        try {
            if (result.next()) {
                for (String key : query.getRows()) {
                    data.put(key, result.getString(key));
                }
            } else {
                return null;
            }
        } catch (SQLException ex) {
            Debug.captureException(ex);
            ex.printStackTrace();
        }
        return data;
    }

    @Override
    public void update(String statement) {
        try {
            if (connection.isClosed()) connect();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(statement.replace("%p%", getTablePrefix()));
            stmt.close();
        } catch (SQLException e) {
            Debug.captureException(e);
            e.printStackTrace();
        }
    }

    @Override
    public boolean exists(String statement) {
        try {
            if (connection.isClosed()) connect();
            Statement stmt = connection.createStatement();
            statement = statement.replace("%p%", getTablePrefix());
            ResultSet result = stmt.executeQuery(statement);
            return result.next();
        } catch (SQLException e) {
            Messages.log("§cCouldn't get value from statement '" + statement + "'!");
            Messages.log("§c" + e.getMessage());
            Debug.captureException(e);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getTablePrefix() {
        return tablePrefix;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    public void alterTable(String statement) {
        try {
            if (connection.isClosed()) connect();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(statement.replace("%p%", getTablePrefix()));
            stmt.close();
        } catch (SQLException ignored) {
        }
    }

    private void createTables() {
        update("CREATE TABLE IF NOT EXISTS `%p%users` (`uuid` CHAR(36) NOT NULL, `username` VARCHAR(20) NULL DEFAULT NULL, `group` VARCHAR(64) NULL DEFAULT NULL, `force_group` BOOLEAN NULL DEFAULT NULL, `subgroup` VARCHAR(64) NULL DEFAULT NULL, `custom_prefix` VARCHAR(128) NULL DEFAULT NULL, `custom_suffix` VARCHAR(128) NULL DEFAULT NULL, `gender` VARCHAR(32) NULL DEFAULT NULL, `chat_color` CHAR(2) NULL DEFAULT NULL, `chat_formatting` CHAR(2) NULL DEFAULT NULL, PRIMARY KEY(`uuid`))ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update("CREATE TABLE IF NOT EXISTS `%p%groups` (`group` VARCHAR(64) not null, UNIQUE(`group`), prefix VARCHAR(128) default NULL null, suffix VARCHAR(128) default NULL null, chat_color CHAR(2) default NULL null, chat_formatting CHAR(2) default NULL null, join_msg VARCHAR(255) default NULL null, quit_msg VARCHAR(255) default NULL null)ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update("CREATE TABLE IF NOT EXISTS `%p%genders` ( `id` INT NOT NULL AUTO_INCREMENT , `type` INT(1) NOT NULL , `group_name` VARCHAR(64) NOT NULL , `gender` VARCHAR(32) NOT NULL , `prefix` VARCHAR(128) default NULL null , `suffix` VARCHAR(128) default NULL null , PRIMARY KEY (`id`)) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");
        update("CREATE TABLE IF NOT EXISTS `%p%subgroups` ( `group` VARCHAR(64) NOT NULL , UNIQUE(`group`), `prefix` VARCHAR(128) default NULL null , `suffix` VARCHAR(128) default NULL null ) ENGINE = InnoDB CHARSET = utf8 COLLATE utf8_bin;");

        alterTable("ALTER TABLE `%p%users` ADD `custom_prefix_update` TIMESTAMP NULL DEFAULT NULL AFTER `custom_prefix`;");
        alterTable("ALTER TABLE `%p%users` ADD `username` VARCHAR(20) NULL DEFAULT NULL AFTER `uuid`;");
        alterTable("ALTER TABLE `%p%users` ADD `custom_suffix_update` TIMESTAMP NULL DEFAULT NULL AFTER `custom_suffix`;");
        alterTable("ALTER TABLE `%p%users` ADD CONSTRAINT `group` FOREIGN KEY (`group`) REFERENCES `%p%groups`(`group`) ON DELETE SET NULL ON UPDATE CASCADE;");
        alterTable("ALTER TABLE `%p%users` ADD CONSTRAINT `subgroup` FOREIGN KEY (`subgroup`) REFERENCES `%p%subgroups`(`group`) ON DELETE SET NULL ON UPDATE CASCADE;");
    }

}
