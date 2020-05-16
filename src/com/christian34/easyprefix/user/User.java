package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.DataStatement;
import com.christian34.easyprefix.database.Database;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.GenderType;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class User {
    private final Player player;
    private UserData userData;
    private ArrayList<Color> colors;
    private ArrayList<ChatFormatting> chatFormattings;
    private Group group;
    private final UUID uniqueId;
    private final EasyPrefix instance;
    private Subgroup subgroup;
    private Color chatColor;
    private ChatFormatting chatFormatting;
    private GenderType genderType;
    private String customPrefix;
    private String customSuffix;
    private boolean isGroupForced;

    public User(Player player) {
        this.player = player;
        this.uniqueId = player.getUniqueId();
        this.instance = EasyPrefix.getInstance();
        if (this.instance.getSqlDatabase() == null) this.userData = new UserData(player.getUniqueId());
    }

    public void login() {
        this.colors = new ArrayList<>();
        this.chatFormattings = new ArrayList<>();
        ConfigData configData = this.instance.getFileManager().getConfig();
        if (!hasPermission("Color.all") && configData.getBoolean(ConfigData.ConfigKeys.HANDLE_COLORS)) {
            for (Color color : Color.values()) {
                if (player.hasPermission("Color." + color.name())) colors.add(color);
            }
            for (ChatFormatting formatting : ChatFormatting.values()) {
                if (formatting.equals(ChatFormatting.RAINBOW)) continue;
                if (player.hasPermission("Color." + formatting.name())) chatFormattings.add(formatting);
            }
        }
        String groupName = null, subgroupName = null, chatColor = null, chatFormatting = null, cstmPrefix = null, cstmSuffix = null, gender = null;
        boolean forceGroup = false;
        Database db = this.instance.getSqlDatabase();
        if (db != null) {
            String stmt = "SELECT `group`,`force_group`,`subgroup`,`custom_prefix`,`custom_suffix`,`gender`," + "`chat_color`,`chat_formatting` FROM `%p%users` WHERE `uuid` = '" + player.getUniqueId().toString() + "'";
            try {
                ResultSet result = db.getValue(stmt);
                if (result.next()) {
                    groupName = result.getString("group");
                    subgroupName = result.getString("subgroup");
                    chatColor = result.getString("chat_color");
                    chatFormatting = result.getString("chat_formatting");
                    cstmPrefix = result.getString("custom_prefix");
                    cstmSuffix = result.getString("custom_suffix");
                    gender = result.getString("gender");
                    forceGroup = result.getBoolean("force_group");
                } else {
                    String sql = "INSERT INTO `%p%users`(`uuid`) VALUES (?)";
                    PreparedStatement st = db.prepareStatement(sql);
                    st.setString(1, uniqueId.toString());
                    st.executeUpdate();
                }
            } catch(SQLException e) {
                e.printStackTrace();
                return;
            }
        } else {
            FileConfiguration data = userData.getFileData();
            groupName = data.getString("group");
            subgroupName = data.getString("subgroup");
            chatColor = data.getString("chat-color");
            chatFormatting = data.getString("chat-formatting");
            cstmPrefix = data.getString("custom-prefix");
            cstmSuffix = data.getString("custom-suffix");
            gender = data.getString("gender");
            forceGroup = data.getBoolean("force-group");
        }

        this.isGroupForced = forceGroup;
        GroupHandler groupHandler = this.instance.getGroupHandler();
        if (groupName == null || groupName.equals("")) {
            this.group = getGroupPerPerms();
        } else {
            if (groupHandler.isGroup(groupName) && (hasPermission("group." + groupName) || forceGroup || groupName.equals("default"))) {
                this.group = groupHandler.getGroup(groupName);
            } else {
                this.group = getGroupPerPerms();
                saveData("group", null);
            }
        }
        if (configData.getBoolean(ConfigData.ConfigKeys.USE_SUBGROUPS)) {
            if (subgroupName != null) {
                if (groupHandler.isSubgroup(subgroupName) && hasPermission("subgroup." + subgroupName)) {
                    this.subgroup = groupHandler.getSubgroup(subgroupName);
                } else {
                    this.subgroup = getSubgroupPerPerms();
                    saveData("subgroup", null);
                }
            }
        }

        if (chatColor != null && !(chatColor.length() < 2)) {
            this.chatColor = Color.getByCode(chatColor.substring(1, 2));
        }

        if (chatFormatting != null && chatFormatting.length() > 1) {
            if (chatFormatting.equals("%r")) {
                this.chatFormatting = ChatFormatting.RAINBOW;
                setChatColor(null);
            } else {
                this.chatFormatting = ChatFormatting.getByCode(chatFormatting.substring(1, 2));
            }
        }

        if (hasPermission("settings.custom")) {
            if (cstmPrefix != null) {
                this.customPrefix = cstmPrefix.replace("&", "§");
            }
            if (cstmSuffix != null) {
                this.customSuffix = cstmSuffix.replace("&", "§");
            }
        }
        if (gender != null && groupHandler.handleGenders()) {
            this.genderType = groupHandler.getGender(gender);
        }
    }

    public boolean hasPermission(String permission) {
        if (player != null) {
            return player.hasPermission("EasyPrefix." + permission);
        }
        return true;
    }

    public String getPrefix() {
        if (hasPermission("settings.custom") && customPrefix != null) {
            return customPrefix;
        }
        return group.getPrefix(this, true);
    }

    public void setPrefix(String prefix) {
        Bukkit.broadcastMessage("");
        saveData("custom-prefix", prefix);
        if (prefix != null) {
            prefix = prefix.replace("&", "§");
        }
        this.customPrefix = prefix;
        this.instance.unloadUser(getPlayer());
    }

    public String getSuffix() {
        if (hasPermission("settings.custom") && customSuffix != null) {
            return customSuffix;
        }
        return group.getSuffix(this, true);
    }

    public void setSuffix(String suffix) {
        saveData("custom-suffix", suffix);
        if (suffix != null) {
            suffix = suffix.replace("&", "§");
        }
        this.customSuffix = suffix;
        this.instance.unloadUser(getPlayer());
    }

    public ArrayList<Color> getColors() {
        return colors;
    }

    public Color getChatColor() {
        if (chatColor != null) {
            return chatColor;
        }
        return getGroup().getChatColor();
    }

    public void setChatColor(Color color) {
        this.chatColor = color;
        String value = null;
        if (color != null) {
            value = color.getCode().replace("§", "&");
            if (chatFormatting != null && chatFormatting.equals(ChatFormatting.RAINBOW)) {
                setChatFormatting(null);
            }
        } else {
            if (chatFormatting != null && !chatFormatting.equals(ChatFormatting.RAINBOW)) {
                setChatFormatting(null);
            }
        }
        saveData("chat-color", value);
        this.instance.unloadUser(getPlayer());
    }

    public ArrayList<ChatFormatting> getChatFormattings() {
        return chatFormattings;
    }

    public ChatFormatting getChatFormatting() {
        if (chatFormatting != null) {
            return chatFormatting;
        }
        return getGroup().getChatFormatting();
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
        this.instance.unloadUser(getPlayer());
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group, Boolean force) {
        this.group = group;
        saveData("group", group.getName());
        this.customPrefix = null;
        saveData("custom-prefix", null);
        this.customSuffix = null;
        saveData("custom-suffix", null);
        this.chatColor = null;
        saveData("chat-color", null);
        this.chatFormatting = null;
        saveData("group", group.getName());
        saveData("force-group", force);
        this.instance.unloadUser(getPlayer());
    }

    public Subgroup getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(Subgroup subgroup) {
        this.subgroup = subgroup;
        String name = (subgroup != null) ? subgroup.getName() : null;
        saveData("subgroup", name);
        this.instance.unloadUser(getPlayer());
    }

    public GenderType getGenderType() {
        return genderType;
    }

    public void setGenderType(GenderType genderType) {
        this.genderType = genderType;
        saveData("gender", genderType.getName());
        this.instance.unloadUser(getPlayer());
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Group> getAvailableGroups() {
        ArrayList<Group> availableGroups = new ArrayList<>();
        for (Group targetGroup : this.instance.getGroupHandler().getGroups()) {
            if (player.hasPermission("EasyPrefix.group." + targetGroup.getName())) {
                availableGroups.add(targetGroup);
            }
        }
        if (this.isGroupForced) {
            Group group = getGroup();
            if (!availableGroups.contains(group)) availableGroups.add(group);
        }
        return availableGroups;
    }

    public ArrayList<Subgroup> getAvailableSubgroups() {
        ArrayList<Subgroup> availableGroups = new ArrayList<>();
        for (Subgroup targetGroup : this.instance.getGroupHandler().getSubgroups()) {
            if (player.hasPermission("EasyPrefix.subgroup." + targetGroup.getName())) {
                availableGroups.add(targetGroup);
            }
        }
        return availableGroups;
    }

    private Group getGroupPerPerms() {
        GroupHandler groupHandler = this.instance.getGroupHandler();
        for (Group group : groupHandler.getGroups()) {
            if (group.getName().equals("default")) continue;
            if (player.hasPermission("EasyPrefix.group." + group.getName())) {
                return group;
            }
        }
        return groupHandler.getGroup("default");
    }

    private Subgroup getSubgroupPerPerms() {
        for (Subgroup subgroup : this.instance.getGroupHandler().getSubgroups()) {
            if (player.hasPermission("EasyPrefix.subgroup." + subgroup.getName())) {
                return subgroup;
            }
        }
        return null;
    }

    private UserData getUserData() {
        return userData;
    }

    public void sendMessage(String message) {
        player.sendMessage(Messages.getPrefix() + message);
    }

    private void saveData(String key, Object value) {
        Database db = this.instance.getSqlDatabase();
        if (db == null) {
            key = key.replace("_", "-");
            getUserData().setAndSave(key, value);
        } else {
            key = key.replace("-", "_");
            String sql = "UPDATE `%p%users` SET `" + key + "`=? WHERE `uuid`=?";
            DataStatement statement = new DataStatement(sql);
            statement.setObject(1, value);
            statement.setObject(2, getPlayer().getUniqueId().toString());
            if (!statement.execute()) {
                Messages.log("Couldn't send data to database!");
                statement.getException().printStackTrace();
            }
        }
    }

}