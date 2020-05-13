package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.Database;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.gender.GenderChat;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Group extends EasyGroup {
    private final String NAME;
    private final GroupsData groupsData;
    private final GroupHandler groupHandler;
    private String prefix, suffix, joinMessage, quitMessage;
    private ChatColor groupColor;
    private Color chatColor;
    private ChatFormatting chatFormatting;
    private GenderChat genderChat = null;

    public Group(GroupHandler groupHandler, String name) {
        this.NAME = name;
        this.groupsData = groupHandler.getInstance().getFileManager().getGroupsData();
        this.groupHandler = groupHandler;

        String prefix = "", suffix = "", chatColor = "", chatFormatting = "", joinMsg = "", quitMsg = "";

        Database db = groupHandler.getInstance().getSqlDatabase();
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
            } catch(SQLException e) {
                e.printStackTrace();
            }
        } else {
            FileConfiguration data = getGroupsData().getData();
            prefix = data.getString(getFilePath() + "prefix");
            suffix = data.getString(getFilePath() + "suffix");
            chatColor = data.getString(getFilePath() + "chat-color");
            chatFormatting = data.getString(getFilePath() + "chat-formatting");
            joinMsg = data.getString(getFilePath() + "join-msg");
            quitMsg = data.getString(getFilePath() + "quit-msg");
        }

        if (groupHandler.handleGenders()) {
            this.genderChat = new GenderChat(this);
        }

        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";

        try {
            applyData(prefix, suffix, chatColor, chatFormatting, joinMsg, quitMsg);
        } catch(Exception e) {
            Messages.log("§cCouldn't load group " + name + "!");
            e.printStackTrace();
        }
    }

    private void applyData(String prefix, String suffix, String chatColor, String chatFormatting, String joinMessage, String quitMessage) throws Exception {
        this.prefix = prefix.replace("§", "&");
        this.suffix = suffix.replace("§", "&");

        if (chatFormatting != null && chatFormatting.length() == 2) {
            this.chatFormatting = ChatFormatting.getByCode(chatFormatting.substring(1, 2));
            if (this.chatFormatting == null) setChatFormatting(null);
        }

        if (chatColor == null || chatColor.isEmpty() || chatColor.length() < 2 && (this.chatFormatting != null && this.chatFormatting != ChatFormatting.RAINBOW)) {
            setChatColor(Color.GRAY);
            chatColor = "&7";
        }

        this.chatColor = Color.getByCode(chatColor.substring(1, 2));
        if (this.chatColor == null) setChatColor(Color.GRAY);

        this.groupColor = getGroupColor(prefix);
        this.joinMessage = joinMessage;
        this.quitMessage = quitMessage;
    }

    public String getJoinMessage(User user) {
        if (this.joinMessage == null || this.joinMessage.isEmpty()) {
            this.joinMessage = this.groupHandler.getGroup("default").getJoinMessageText();
        }
        return translate(joinMessage, user);
    }

    public String getJoinMessageText() {
        if (this.joinMessage == null || this.joinMessage.isEmpty()) {
            this.joinMessage = this.groupHandler.getGroup("default").getJoinMessageText();
        }
        return joinMessage;
    }

    public void setJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage.replace("§", "&");
        saveData("join-msg", this.joinMessage);
    }

    public String getQuitMessage(User user) {
        if (this.quitMessage == null || this.quitMessage.isEmpty()) {
            this.quitMessage = this.groupHandler.getGroup("default").getQuitMessageText();
        }
        return translate(quitMessage, user);
    }

    public String getQuitMessageText() {
        if (this.quitMessage == null || this.quitMessage.isEmpty()) {
            this.quitMessage = this.groupHandler.getGroup("default").getQuitMessageText();
        }
        return quitMessage;
    }

    public void setQuitMessage(String quitMessage) {
        this.quitMessage = quitMessage.replace("§", "&");
        saveData("quit-msg", this.quitMessage);
    }

    private GroupsData getGroupsData() {
        return groupsData;
    }

    private void saveData(String key, Object value) {
        Database db = this.groupHandler.getInstance().getSqlDatabase();
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
                stmt.setString(2, NAME);
                stmt.executeUpdate();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
        /* todo reload current group, to improve performance */
        this.groupHandler.getInstance().getGroupHandler().load();
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
    public String getFilePath() {
        return "groups." + NAME + ".";
    }

    @Override
    public void delete() {
        EasyPrefix instance = this.groupHandler.getInstance();
        if (instance.getSqlDatabase() == null) {
            groupsData.setAndSave("groups." + getName(), null);
        } else {
            Database db = instance.getSqlDatabase();
            db.update("DELETE FROM `%p%groups` WHERE `group` = '" + getName() + "'");
        }
        instance.getGroupHandler().getGroups().remove(this);
        instance.getUsers().clear();
    }

    public Color getChatColor() {
        return chatColor;
    }

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

    public ChatFormatting getChatFormatting() {
        return chatFormatting;
    }

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

}