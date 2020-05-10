package com.christian34.easyprefix.user;

import com.christian34.easyprefix.Database;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.GenderType;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.sun.istack.internal.Nullable;
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
    private Player player;
    private UserData userData;
    private ArrayList<Color> colors;
    private ArrayList<ChatFormatting> chatFormattings;
    private Group group;
    private String cPrefix;
    private String cSuffix;
    private String name;
    private Subgroup subgroup;
    private Color chatColor;
    private ChatFormatting chatFormatting;
    private GenderType genderType;
    private UUID uniqueId;
    private boolean forceGroup;

    public User(Player player) {
        this.player = player;
        this.name = player.getName();
        this.uniqueId = player.getUniqueId();
        if (EasyPrefix.getInstance().getSqlDatabase() == null) this.userData = new UserData(player.getUniqueId());
        load();
    }

    public void load() {
        this.colors = new ArrayList<>();
        this.chatFormattings = new ArrayList<>();
        if (!hasPermission("EasyPrefix.Color.all") && FileManager.getConfig().getBoolean(ConfigData.ConfigKeys.HANDLE_COLORS)) {
            for (Color color : Color.values()) {
                if (player.hasPermission("EasyPrefix.Color." + color.name())) colors.add(color);
            }
            for (ChatFormatting formatting : ChatFormatting.values()) {
                if (formatting.equals(ChatFormatting.RAINBOW)) continue;
                if (player.hasPermission("EasyPrefix.Color." + formatting.name())) chatFormattings.add(formatting);
            }
        }
        String groupName = null, subgroupName = null, chatColor = null, chatFormatting = null, cstmPrefix = null, cstmSuffix = null, gender = null;
        boolean forceGroup = false;
        Database db = EasyPrefix.getInstance().getSqlDatabase();
        if (db != null) {
            String stmt = "SELECT `group`,`force_group`,`subgroup`,`custom_prefix`,`custom_suffix`,`gender`," + "`chat_color`,`chat_formatting` FROM `" + db.getTablePrefix() + "users` WHERE `uuid` = '" + player.getUniqueId().toString() + "'";
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

        this.forceGroup = forceGroup;
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        if (groupName == null || groupName.equals("")) {
            this.group = getGroupPerPerms();
        } else {
            if (groupHandler.isGroup(groupName) && (hasPermission("EasyPrefix.group." + groupName) || forceGroup || groupName.equals("default"))) {
                this.group = groupHandler.getGroup(groupName);
            } else {
                this.group = getGroupPerPerms();
                saveData("group", null);
            }
        }
        if (FileManager.getConfig().getBoolean(ConfigData.ConfigKeys.USE_SUBGROUPS)) {
            if (subgroupName != null) {
                if (groupHandler.isSubgroup(subgroupName) && hasPermission("EasyPrefix.subgroup." + subgroupName)) {
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

        if (chatFormatting == null || chatFormatting.length() < 2) {
            setChatFormatting(null);
        } else {
            if (chatFormatting.equals("%r")) {
                this.chatFormatting = ChatFormatting.RAINBOW;
                setChatColor(null);
            } else {
                this.chatFormatting = ChatFormatting.getByCode(chatFormatting.substring(1, 2));
            }
        }

        if (hasPermission("EasyPrefix.settings.custom")) {
            if (cstmPrefix != null) {
                this.cPrefix = cstmPrefix.replace("&", "§");
            }
            if (cstmSuffix != null) {
                this.cSuffix = cstmSuffix.replace("&", "§");
            }
        }
        if (gender != null && groupHandler.handleGenders()) {
            this.genderType = groupHandler.getGender(gender);
        }
    }

    public boolean hasPermission(String permission) {
        if (player != null) {
            return player.hasPermission(permission);
        }
        return true;
    }

    public String getPrefix() {
        if (hasPermission("EasyPrefix.settings.custom") && cPrefix != null) {
            return cPrefix;
        }
        return group.getPrefix(this, true);
    }

    public void setPrefix(String prefix) {
        saveData("custom-prefix", prefix);
        if (prefix != null) {
            prefix = prefix.replace("&", "§");
        }
        this.cPrefix = prefix;
    }

    public String getSuffix() {
        if (hasPermission("EasyPrefix.settings.custom") && cSuffix != null) {
            return cSuffix;
        }
        return group.getSuffix(this, true);
    }

    public void setSuffix(String suffix) {
        saveData("custom-suffix", suffix);
        if (suffix != null) {
            suffix = suffix.replace("&", "§");
        }
        this.cSuffix = suffix;
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
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group, Boolean force) {
        this.group = group;
        saveData("group", group.getName());
        this.cPrefix = null;
        saveData("custom-prefix", null);
        this.cSuffix = null;
        saveData("custom-suffix", null);
        this.chatColor = null;
        saveData("chat-color", null);
        this.chatFormatting = null;
        saveData("group", group.getName());
        saveData("force-group", force);
    }

    @Nullable
    public Subgroup getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(Subgroup subgroup) {
        this.subgroup = subgroup;
        String name = (subgroup != null) ? subgroup.getName() : null;
        saveData("subgroup", name);
    }

    public GenderType getGenderType() {
        return genderType;
    }

    public void setGenderType(GenderType genderType) {
        this.genderType = genderType;
        saveData("gender", genderType.getName());
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Group> getAvailableGroups() {
        ArrayList<Group> availableGroups = new ArrayList<>();
        for (Group targetGroup : EasyPrefix.getInstance().getGroupHandler().getGroups()) {
            if (player.hasPermission("EasyPrefix.group." + targetGroup.getName())) {
                availableGroups.add(targetGroup);
            }
        }
        if (forceGroup) {
            Group group = getGroup();
            if (!availableGroups.contains(group)) availableGroups.add(group);
        }
        return availableGroups;
    }

    public ArrayList<Subgroup> getAvailableSubgroups() {
        ArrayList<Subgroup> availableGroups = new ArrayList<>();
        for (Subgroup targetGroup : EasyPrefix.getInstance().getGroupHandler().getSubgroups()) {
            if (player.hasPermission("EasyPrefix.subgroup." + targetGroup.getName())) {
                availableGroups.add(targetGroup);
            }
        }
        return availableGroups;
    }

    private Group getGroupPerPerms() {
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        for (Group group : groupHandler.getGroups()) {
            if (group.getName().equals("default")) continue;
            if (player.hasPermission("EasyPrefix.group." + group.getName())) {
                return group;
            }
        }
        return groupHandler.getGroup("default");
    }

    private Subgroup getSubgroupPerPerms() {
        for (Subgroup subgroup : EasyPrefix.getInstance().getGroupHandler().getSubgroups()) {
            if (player.hasPermission("EasyPrefix.subgroup." + subgroup.getName())) {
                return subgroup;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    private UserData getUserData() {
        return userData;
    }

    public void sendMessage(String message) {
        player.sendMessage(Messages.getPrefix() + message);
    }

    private void saveData(String key, Object value) {
        Database db = EasyPrefix.getInstance().getSqlDatabase();
        if (db == null) {
            key = key.replace("_", "-");
            getUserData().setAndSave(key, value);
        } else {
            key = key.replace("-", "_");
            String sql = "UPDATE `%p%users` SET `" + key + "`=? WHERE `uuid`=?";
            PreparedStatement stmt = db.prepareStatement(sql);
            try {
                stmt.setObject(1, value);
                stmt.setString(2, getPlayer().getUniqueId().toString());
                stmt.executeUpdate();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

}