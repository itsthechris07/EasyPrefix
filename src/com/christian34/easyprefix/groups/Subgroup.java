package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.Database;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.user.Gender;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Subgroup extends EasyGroup {
    private String name, prefix, suffix, rawPrefix, rawSuffix;
    private ChatColor groupColor;
    private GroupsData groupsData;
    private ConcurrentHashMap<Gender, String> prefixes;
    private ConcurrentHashMap<Gender, String> suffixes;

    Subgroup(String name) {
        this.prefixes = new ConcurrentHashMap<>();
        this.suffixes = new ConcurrentHashMap<>();
        this.name = name;
        Database db = EasyPrefix.getInstance().getDatabase();
        if (db != null) {
            try {
                ResultSet result = db.getValue("SELECT `prefix`, `suffix` FROM `%p%subgroups` WHERE `group` = '" + name + "'");
                while (result.next()) {
                    this.prefix = result.getString("prefix");
                    this.suffix = result.getString("suffix");
                }
                String sql2 = "SELECT `gender`, `prefix`, `suffix` FROM `%p%genders` WHERE `type` = 1 AND `group_name` = '" + name + "'";
                ResultSet result2 = db.getValue(sql2);
                while (result2.next()) {
                    String genderName = result2.getString("gender");
                    if (Gender.getTypes().contains(genderName)) {
                        String gPref = result2.getString("prefix");
                        if (gPref != null) prefixes.put(Gender.get(genderName), gPref);
                        String gSufi = result2.getString("suffix");
                        if (gSufi != null) suffixes.put(Gender.get(genderName), gSufi);
                    }
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
        } else {
            this.groupsData = FileManager.getGroups();
            this.prefix = groupsData.getFileData().getString(getFilePath() + "prefix");
            this.suffix = groupsData.getFileData().getString(getFilePath() + "suffix");
            ConfigurationSection section = groupsData.getFileData().getConfigurationSection(getFilePath() + "genders");
            if (section != null) {
                Set<String> genders = section.getKeys(false);
                for (String target : genders) {
                    if (Gender.getTypes().contains(target)) {
                        String prefix = groupsData.getFileData().getString(getFilePath() + "genders." + target + ".prefix");
                        if (prefix != null) prefixes.put(Gender.get(target), prefix);
                        String suffix = groupsData.getFileData().getString(getFilePath() + "genders." + target + ".suffix");
                        if (suffix != null) suffixes.put(Gender.get(target), suffix);
                    }
                }

            }
        }
        if (prefix == null) {
            this.rawPrefix = "";
            this.prefix = "";
            saveData("prefix", "");
        } else {
            this.rawPrefix = prefix.replace("§", "&");
            this.prefix = translate(prefix);
        }
        if (suffix == null) {
            this.rawSuffix = "";
            this.suffix = "";
            saveData("suffix", "");
        } else {
            this.rawSuffix = suffix.replace("§", "&");
            this.suffix = translate(suffix);
        }
        if (getRawPrefix().contains("&")) {
            if (!getRawPrefix().startsWith("&")) {
                String temp = getRawPrefix();
                while (!temp.startsWith("&") && temp.length() > 0) {
                    temp = temp.substring(1);
                }
                groupColor = ChatColor.getByChar(temp.substring(1, 2));
            } else {
                groupColor = ChatColor.getByChar(getPrefix().substring(1, 2));
            }
        }
        if (getGroupColor() == null) groupColor = ChatColor.DARK_PURPLE;
    }

    private void saveData(String key, Object value) {
        Database db = EasyPrefix.getInstance().getDatabase();
        if (value instanceof String) value = ((String) value).replace("§", "&");
        if (db == null) {
            key = key.replace("_", "-");
            groupsData.set(getFilePath() + key, value);
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
        GroupHandler.load();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRawPrefix() {
        return rawPrefix;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.rawPrefix = prefix.replace("§", "&");
        this.prefix = translate(prefix);
        saveData("prefix", prefix);
    }

    @Override
    public String getPrefix(Gender gender) {
        if (gender != null) {
            if (prefixes.containsKey(gender)) {
                return prefixes.get(gender);
            }
        }
        return getPrefix();
    }

    @Override
    public String getRawSuffix() {
        return rawSuffix;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public void setSuffix(String suffix) {
        this.rawSuffix = suffix.replace("§", "&");
        this.suffix = translate(suffix);
        saveData("suffix", suffix);
    }

    @Override
    public String getSuffix(Gender gender) {
        if (gender != null) {
            if (suffixes.containsKey(gender)) {
                return suffixes.get(gender);
            }
        }
        return getSuffix();
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
        Bukkit.broadcastMessage("112sadj");
    }

    @Override
    public String getFilePath() {
        return "subgroups." + getName() + ".";
    }

    @Override
    public void delete() {
        if (EasyPrefix.getInstance().getDatabase() == null) {
            groupsData.set("subgroups." + getName(), null);
        } else {
            Database db = EasyPrefix.getInstance().getDatabase();
            db.update("DELETE FROM `%p%subgroups` WHERE `group` = '" + getName() + "'");
        }
        GroupHandler.getSubgroups().remove(getName().toLowerCase());
        User.getUsers().clear();
    }

    private String translate(String text) {
        return (text != null) ? ChatColor.translateAlternateColorCodes('&', text) : text;
    }

}