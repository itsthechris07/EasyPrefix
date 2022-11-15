package com.christian34.easyprefix.database.tables;

import com.christian34.easyprefix.EasyPrefix;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Date;

@DatabaseTable(tableName = "users")
public class TableUser {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(dataType = DataType.STRING, width = 20)
    private String username;

    @DatabaseField(dataType = DataType.STRING, width = 64, columnName = "group_name")
    private String groupName;

    @DatabaseField(dataType = DataType.BOOLEAN, columnName = "force_group")
    private boolean forceGroup;

    @DatabaseField(dataType = DataType.STRING, width = 64, columnName = "subgroup_name")
    private String subgroupName;

    @DatabaseField(dataType = DataType.STRING, width = 128, columnName = "custom_prefix")
    private String customPrefix;

    @DatabaseField(dataType = DataType.DATE_LONG, defaultValue = "0", columnName = "custom_prefix_update")
    private Date customPrefixUpdate;

    @DatabaseField(dataType = DataType.STRING, width = 128, columnName = "custom_suffix")
    private String customSuffix;

    @DatabaseField(dataType = DataType.DATE_LONG, defaultValue = "0", columnName = "custom_suffix_update")
    private Date customSuffixUpdate;

    @DatabaseField(dataType = DataType.STRING, width = 2, columnName = "chat_color")
    private String chatColor;

    @DatabaseField(dataType = DataType.STRING, width = 2, columnName = "chat_formatting")
    private String chatFormatting;

    @DatabaseField(unique = true, dataType = DataType.STRING, width = 36, columnName = "uuid")
    private String uniqueId;

    @SuppressWarnings("unused")
    public TableUser() {
    }

    public TableUser(String uuid) {
        this.uniqueId = uuid;

        long id = getId(uuid);
        if (id > -1) {
            this.id = id;
        }
    }

    public static long getId(String uniqueId) {
        try {
            TableUser table = EasyPrefix.getInstance().getDatabaseManager().getTableUserDao().queryBuilder().where().eq("uuid", uniqueId).queryForFirst();
            if (table == null) return -1;
            return table.getId();
        } catch (SQLException e) {
            return -1;
        }
    }

    public long getId() {
        return id;
    }

    @NotNull
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(@NotNull String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    public boolean isForceGroup() {
        return forceGroup;
    }

    public void setForceGroup(boolean forceGroup) {
        this.forceGroup = forceGroup;
    }

    @Nullable
    public String getGroup() {
        return groupName;
    }

    public void setGroup(String groupName) {
        this.groupName = groupName;
    }

    @Nullable
    public String getSubgroup() {
        return subgroupName;
    }

    public void setSubgroup(String subgroupName) {
        this.subgroupName = subgroupName;
    }

    @Nullable
    public String getCustomPrefix() {
        return customPrefix;
    }

    public void setCustomPrefix(String customPrefix) {
        this.customPrefix = customPrefix;
    }

    @Nullable
    public Date getCustomPrefixUpdate() {
        return customPrefixUpdate;
    }

    public void setCustomPrefixUpdate(Date customPrefixUpdate) {
        this.customPrefixUpdate = customPrefixUpdate;
    }

    @Nullable
    public String getCustomSuffix() {
        return customSuffix;
    }

    public void setCustomSuffix(String customSuffix) {
        this.customSuffix = customSuffix;
    }

    public Date getCustomSuffixUpdate() {
        return customSuffixUpdate;
    }

    public void setCustomSuffixUpdate(Date customSuffixUpdate) {
        this.customSuffixUpdate = customSuffixUpdate;
    }

    @Nullable
    public String getChatColor() {
        return chatColor;
    }

    public void setChatColor(String chatColor) {
        this.chatColor = chatColor;
    }

    @Nullable
    public String getChatFormatting() {
        return chatFormatting;
    }

    public void setChatFormatting(String chatFormatting) {
        this.chatFormatting = chatFormatting;
    }

}
