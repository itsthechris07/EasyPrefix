package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.Database;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.gender.GenderChat;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.sun.istack.internal.Nullable;
import org.bukkit.ChatColor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Subgroup extends EasyGroup {
    private final String NAME;
    private String prefix, suffix;
    private ChatColor groupColor;
    private GroupsData groupsData;
    private GroupHandler groupHandler;
    private GenderChat genderChat = null;

    public Subgroup(GroupHandler groupHandler, String name) {
        this.NAME = name;
        this.groupHandler = groupHandler;
        Database db = EasyPrefix.getInstance().getDatabase();
        if (db != null) {
            try {
                ResultSet result = db.getValue("SELECT `prefix`, `suffix` FROM `%p%subgroups` WHERE `group` = '" + name + "'");
                while (result.next()) {
                    this.prefix = result.getString("prefix");
                    this.suffix = result.getString("suffix");
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
        } else {
            this.groupsData = FileManager.getGroups();
            this.prefix = groupsData.getFileData().getString(getFilePath() + "prefix");
            this.suffix = groupsData.getFileData().getString(getFilePath() + "suffix");
        }

        if (groupHandler.handleGenders()) {
            this.genderChat = new GenderChat(this);
        }

        if (prefix == null) {
            this.prefix = "";
            saveData("prefix", "");
        } else {
            this.prefix = prefix.replace("§", "&");
        }
        if (suffix == null) {
            this.suffix = "";
            saveData("suffix", "");
        } else {
            this.suffix = suffix.replace("§", "&");
        }

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
        Database db = EasyPrefix.getInstance().getDatabase();
        if (value instanceof String) value = ((String) value).replace("§", "&");
        if (db == null) {
            key = key.replace("_", "-");
            groupsData.setAndSave(getFilePath() + key, value);
        } else {
            key = key.replace("-", "_");
            String sql = "UPDATE `%p%groups` SET `" + key + "`=? WHERE `group`=?";
            PreparedStatement stmt = db.prepareStatement(sql);
            try {
                stmt.setObject(1, value);
                stmt.setString(2, getName());
                stmt.executeUpdate();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
        EasyPrefix.getInstance().getGroupHandler().load();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getPrefix(User user, boolean translate) {
        String prefix = "";
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
        String suffix = "";
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
    @Nullable
    public Color getChatColor() {
        return null;
    }

    @Override
    public void setChatColor(Color color) {
    }

    @Override
    @Nullable
    public ChatFormatting getChatFormatting() {
        return null;
    }

    @Override
    public void setChatFormatting(ChatFormatting chatFormatting) {
    }

    @Override
    public String getFilePath() {
        return "subgroups." + getName() + ".";
    }

    @Override
    public void delete() {
        EasyPrefix instance = EasyPrefix.getInstance();
        if (instance.getDatabase() == null) {
            groupsData.setAndSave("subgroups." + getName(), null);
        } else {
            Database db = instance.getDatabase();
            db.update("DELETE FROM `%p%subgroups` WHERE `group` = '" + getName() + "'");
        }
        /* todo grouphandler unregister function */
        instance.getGroupHandler().getSubgroups().remove(this);
        instance.getUsers().clear();
    }

}