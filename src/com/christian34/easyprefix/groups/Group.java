package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.Database;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.Gender;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.sun.istack.internal.Nullable;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

public class Group extends EasyGroup {
    private final String NAME;
    private String prefix;
    private String suffix;
    private String rawPrefix;
    private String rawSuffix;
    private String joinMessage;
    private String quitMessage;
    private ChatColor groupColor;
    private GroupsData groupsData;
    private Color chatColor;
    private ChatFormatting chatFormatting;
    private HashMap<Gender, String> prefixes;
    private HashMap<Gender, String> suffixes;

    Group(String name) {
        this.NAME = name;
        this.groupsData = FileManager.getGroups();
        this.prefixes = new HashMap<>();
        this.suffixes = new HashMap<>();
        String prefix = "", suffix = "", chatColor = "", chatFormatting = "", joinMsg = "", quitMsg = "";

        Database db = EasyPrefix.getInstance().getDatabase();
        if (db != null) {
            try {
                String sql = "SELECT `prefix`,`suffix`,`chat_color`,`chat_formatting`,`join_msg`,`quit_msg` FROM `%p%groups` WHERE `group` = '" + name + "'";
                ResultSet result = db.getValue(sql);
                while (result.next()) {
                    prefix = result.getString("prefix");
                    suffix = result.getString("suffix");
                    chatColor = result.getString("chat_color");
                    chatFormatting = result.getString("chat_formatting");
                    joinMsg = result.getString("join_msg");
                    quitMsg = result.getString("quit_msg");
                }

                String sql2 = "SELECT `gender`, `prefix`, `suffix` FROM `%p%genders` WHERE `type` = 0 AND `group_name` = '" + name + "'";
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
            FileConfiguration data = getGroupsData().getFileData();
            prefix = data.getString(getFilePath() + "prefix");
            suffix = data.getString(getFilePath() + "suffix");
            chatColor = data.getString(getFilePath() + "chat-color");
            chatFormatting = data.getString(getFilePath() + "chat-formatting");
            joinMsg = data.getString(getFilePath() + "join-msg");
            quitMsg = data.getString(getFilePath() + "quit-msg");
            Set<String> childs = getGroupsData().getFileData().getConfigurationSection("groups." + getName()).getKeys(false);
            for (String target : childs) {
                if (Gender.getTypes().contains(target)) {
                    String genderPrefix = data.getString(getFilePath() + target + ".prefix");
                    if (genderPrefix != null) getGroupsData().set(getFilePath() + ".genders." + target + ".prefix", genderPrefix);
                    String genderSuffix = data.getString(getFilePath() + target + ".suffix");
                    if (genderSuffix != null) getGroupsData().set(getFilePath() + ".genders." + target + ".suffix", genderSuffix);
                    data.set(getFilePath() + target, null);
                }
            }
            for (String target : childs) {
                if (Gender.getTypes().contains(target)) {
                    String genderPrefix = data.getString(getFilePath() + target + ".genders.prefix");
                    if (genderPrefix != null) prefixes.put(Gender.get(target), genderPrefix);
                    String genderSuffix = data.getString(getFilePath() + target + ".genders.suffix");
                    if (genderSuffix != null) suffixes.put(Gender.get(target), genderSuffix);
                }
            }
        }

        prefix = (prefix == null) ? "" : prefix;
        suffix = (suffix == null) ? "" : suffix;

        try {
            applyData(prefix, suffix, chatColor, chatFormatting, joinMsg, quitMsg);
        } catch(Exception e) {
            Messages.log("§cCouldn't load group " + name + "!");
            e.printStackTrace();
        }
    }

    private void applyData(String prefix, String suffix, String chatColor, String chatFormatting, String joinMessage, String quitMessage) throws Exception {
        this.prefix = translate(prefix);
        this.rawPrefix = prefix.replace("§", "&");
        this.suffix = translate( suffix);
        this.rawSuffix = suffix.replace("§", "&");

        if (chatColor == null || chatColor.length() < 2) {
            setChatColor(Color.GRAY);
            chatColor = "&7";
        } else if (chatColor.length() == 4) {
            setChatColor(Color.getByCode(chatColor.substring(1, 2)));
            setChatFormatting(ChatFormatting.getByCode(chatColor.substring(3, 4)));
        }

        chatColor = chatColor.substring(1, 2);
        if (chatColor.equals("r")) setChatFormatting(ChatFormatting.RAINBOW);

        this.chatColor = Color.getByCode(chatColor);
        if (this.chatColor == null) setChatColor(Color.GRAY);

        if (chatFormatting != null && chatFormatting.length() == 2) {
            this.chatFormatting = ChatFormatting.getByCode(chatFormatting.substring(1, 2));
            if (this.chatFormatting == null) setChatFormatting(null);
        }

        if (prefix.contains("§")) {
            if (!prefix.startsWith("§")) {
                String temp = prefix;
                while (!temp.startsWith("§") && temp.length() > 0) {
                    temp = temp.substring(1);
                }
                this.groupColor = ChatColor.getByChar(temp.substring(1, 2));
            } else {
                this.groupColor = ChatColor.getByChar(getPrefix().substring(1, 2));
            }
        }
        if (getGroupColor() == null) groupColor = ChatColor.DARK_PURPLE;

        Group defaultGroup = GroupHandler.getGroup("default");

        this.joinMessage = (joinMessage == null) ? translate(defaultGroup.getJoinMessage()) : translate(joinMessage);
        this.quitMessage = (quitMessage == null) ? translate(defaultGroup.getQuitMessage()) : translate(quitMessage);
    }

    public String getJoinMessage() {
        return joinMessage;
    }

    public String getQuitMessage() {
        return quitMessage;
    }

    private GroupsData getGroupsData() {
        return groupsData;
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
                stmt.setString(2, NAME);
                stmt.executeUpdate();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
        GroupHandler.load();
    }

    public String getName() {
        return NAME;
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
        this.prefix = prefix;
        rawPrefix = suffix.replace("§", "&");
        saveData("prefix", prefix);
    }

    @Override
    public String getPrefix(Gender gender) {
        if (gender != null) {
            if (prefixes.containsKey(gender)) {
                return prefixes.get(gender);
            }
        }
        return prefix;
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
        this.suffix = suffix;
        rawSuffix = suffix.replace("§", "&");
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
    public Color getChatColor() {
        return chatColor;
    }

    @Override
    public void setChatColor(Color color) {
        this.chatColor = color;
        String value = null;
        if (color != null) {
            value = color.getCode().replace("§", "&");
            if (ChatFormatting.RAINBOW.equals(chatFormatting)) {
                setChatFormatting(null);
            }
        } else {
            if (chatFormatting != null && !chatFormatting.equals(ChatFormatting.RAINBOW)) {
                setChatFormatting(null);
            }
        }
        saveData("chat-color", value);
    }

    @Override
    @Nullable
    public ChatFormatting getChatFormatting() {
        return chatFormatting;
    }

    @Override
    public void setChatFormatting(ChatFormatting chatFormatting) {
        this.chatFormatting = chatFormatting;
        String value = null;
        if (chatFormatting != null) {
            if (chatFormatting.equals(ChatFormatting.RAINBOW)) {
                setChatColor(null);
                value = "%r";
            } else value = chatFormatting.getCode().replace("§", "&");
        }
        saveData("chat-formatting", value);
    }

    @Override
    public String getFilePath() {
        return "groups." + NAME + ".";
    }

    @Override
    public void delete() {
        if (EasyPrefix.getInstance().getDatabase() == null) {
            groupsData.set("groups." + getName(), null);
        } else {
            Database db = EasyPrefix.getInstance().getDatabase();
            db.update("DELETE FROM `%p%groups` WHERE `group` = '" + getName() + "'");
        }
        GroupHandler.getGroups().remove(NAME.toLowerCase());
        User.getUsers().clear();
    }

    private String translate(String text) {
        return (text != null) ? ChatColor.translateAlternateColorCodes('&', text) : text;
    }

}