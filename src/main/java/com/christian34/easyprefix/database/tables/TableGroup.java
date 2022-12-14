package com.christian34.easyprefix.database.tables;

import com.christian34.easyprefix.EasyPrefix;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
@DatabaseTable(tableName = "groups")
public class TableGroup {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(dataType = DataType.STRING, width = 64, unique = true)
    private String name;

    @DatabaseField(dataType = DataType.STRING, width = 128)
    private String prefix;

    @DatabaseField(dataType = DataType.STRING, width = 128)
    private String suffix;

    /* @DatabaseField(dataType = DataType.CHAR_OBJ)
    private Character chat_color; */
    @DatabaseField(dataType = DataType.STRING, width = 2, columnName = "chat_color")
    private String chatColor;

    /* @DatabaseField(dataType = DataType.CHAR_OBJ)
    private Character chat_formatting; */
    @DatabaseField(dataType = DataType.STRING, width = 2, columnName = "chat_formatting")
    private String chatFormatting;

    @DatabaseField(dataType = DataType.STRING, width = 255, columnName = "join_msg")
    private String joinMessage;

    @DatabaseField(dataType = DataType.STRING, width = 255, columnName = "quit_msg")
    private String quitMessage;

    @SuppressWarnings("unused")
    public TableGroup() {
    }

    public TableGroup(String name) {
        this.name = name;
    }

    public static Dao<TableGroup, String> getDao() {
        return EasyPrefix.getInstance().getDatabaseManager().getTableGroupDao();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Character getChatColor() {
        return (chatColor != null) ? chatColor.charAt(0) : null;
    }

    public void setChatColor(Character chatColor) {
        this.chatColor = (chatColor != null) ? chatColor.toString() : null;
    }

    public Character getChatFormatting() {
        return (chatFormatting != null) ? chatFormatting.charAt(0) : null;
    }

    public void setChatFormatting(@Nullable Character chatFormatting) {
        this.chatFormatting = (chatFormatting != null) ? chatFormatting.toString() : null;
    }

    public String getJoinMessage() {
        return joinMessage;
    }

    public void setJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage;
    }

    public String getQuitMessage() {
        return quitMessage;
    }

    public void setQuitMessage(String quitMessage) {
        this.quitMessage = quitMessage;
    }

}
