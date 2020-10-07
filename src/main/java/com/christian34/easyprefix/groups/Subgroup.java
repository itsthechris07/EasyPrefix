package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.*;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.gender.GenderChat;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import org.bukkit.ChatColor;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Subgroup extends EasyGroup {
    private final String NAME;
    private final GroupHandler groupHandler;
    private final EasyPrefix instance;
    private String prefix, suffix;
    private ChatColor groupColor;
    private GroupsData groupsData;
    private GenderChat genderChat = null;

    public Subgroup(GroupHandler groupHandler, String name) {
        this.NAME = name;
        this.groupHandler = groupHandler;
        this.instance = groupHandler.getInstance();
        if (instance.getStorageType() == StorageType.SQL) {
            SelectQuery selectQuery = new SelectQuery("subgroups", "prefix", "suffix").addCondition("group", name);
            Data data = selectQuery.getData();
            this.prefix = data.getStringOr("prefix", "");
            this.suffix = data.getStringOr("suffix", "");
        } else {
            this.groupsData = groupHandler.getInstance().getFileManager().getGroupsData();
            this.prefix = groupsData.getOrDefault(getFilePath() + "prefix", "");
            this.suffix = groupsData.getOrDefault(getFilePath() + "suffix", "");
        }

        if (groupHandler.handleGenders()) {
            this.genderChat = new GenderChat(this);
        }

        this.prefix = prefix.replace("§", "&");
        this.suffix = suffix.replace("§", "&");

        if (prefix.contains("&")) {
            if (!prefix.startsWith("&")) {
                String temp = prefix;
                while (!temp.startsWith("&") && temp.length() > 0) {
                    temp = temp.substring(1);
                }
                groupColor = ChatColor.getByChar(temp.substring(1, 2));
            } else {
                groupColor = ChatColor.getByChar(prefix.substring(1, 2));
            }
        }
        if (getGroupColor() == null) groupColor = ChatColor.DARK_PURPLE;
    }

    private void saveData(String key, Object value) {
        if (value instanceof String) value = ((String) value).replace("§", "&");
        if (instance.getStorageType() == StorageType.LOCAL) {
            key = key.replace("_", "-");
            groupsData.setAndSave(getFilePath() + key, value);
        } else {
            key = key.replace("-", "_");
            String sql = "UPDATE `%p%groups` SET `" + key + "`=? WHERE `group`=?";
            DataStatement statement = new DataStatement(sql);
            statement.setObject(1, value);
            statement.setObject(2, getName());
            if (!statement.execute()) {
                Messages.log("§cCouldn't save data to database!");
                statement.getException().printStackTrace();
            }
        }
        instance.getGroupHandler().load();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getPrefix(User user, boolean translate) {
        String prefix;
        if (this.groupHandler.handleGenders() && user != null) {
            prefix = this.genderChat.getPrefix(user.getGenderType());
            if (prefix == null) prefix = this.prefix;
        } else {
            prefix = this.prefix;
        }
        if (translate) prefix = translate(prefix, user);
        return prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix.replace("§", "&");
        saveData("prefix", this.prefix);
    }

    @Override
    public String getSuffix(User user, boolean translate) {
        String suffix;
        if (this.groupHandler.handleGenders() && user != null) {
            suffix = this.genderChat.getSuffix(user.getGenderType());
            if (suffix == null) suffix = this.suffix;
        } else {
            suffix = this.suffix;
        }
        if (translate) suffix = translate(suffix, user);
        return suffix;
    }

    @Override
    public void setSuffix(String suffix) {
        this.suffix = suffix.replace("§", "&");
        saveData("suffix", this.suffix);
    }

    @Override
    public ChatColor getGroupColor() {
        return groupColor;
    }

    @Override
    public String getFilePath() {
        return "subgroups." + getName() + ".";
    }

    @Override
    public void delete() {
        if (instance.getStorageType() == StorageType.LOCAL) {
            groupsData.setAndSave("subgroups." + getName(), null);
        } else {
            SQLDatabase db = instance.getSqlDatabase();
            db.update("DELETE FROM `%p%subgroups` WHERE `group` = '" + getName() + "'");
        }
        instance.getGroupHandler().getSubgroups().remove(this);
        instance.getUsers().clear();
    }

}