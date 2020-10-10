package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.sql.UpdateStatement;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class User {
    private final Player player;
    private final UUID uniqueId;
    private final EasyPrefix instance;
    private final GroupHandler groupHandler;
    private ArrayList<Color> colors;
    private ArrayList<ChatFormatting> chatFormattings;
    private Group group;
    private Subgroup subgroup;
    private Color chatColor;
    private Gender gender;
    private String customPrefix;
    private String customSuffix;
    private boolean isGroupForced;
    private long lastPrefixUpdate, lastSuffixUpdate;
    private ChatFormatting chatFormatting = null;
    private UserData userData;

    public User(Player player) {
        this.player = player;
        this.uniqueId = player.getUniqueId();
        this.instance = EasyPrefix.getInstance();
        this.groupHandler = this.instance.getGroupHandler();
    }

    public long getLastPrefixUpdate() {
        return lastPrefixUpdate;
    }

    public long getLastSuffixUpdate() {
        return lastSuffixUpdate;
    }

    public void login() {
        this.userData = new UserData(uniqueId);
        userData.loadData();
        this.colors = new ArrayList<>();
        this.chatFormattings = new ArrayList<>();

        if (!hasPermission("Color.all") && ConfigKeys.HANDLE_COLORS.toBoolean()) {
            for (Color color : Color.values()) {
                if (hasPermission("Color." + color.name())) colors.add(color);
            }
            for (ChatFormatting formatting : ChatFormatting.values()) {
                if (formatting.equals(ChatFormatting.RAINBOW)) continue;
                if (hasPermission("Color." + formatting.name())) chatFormattings.add(formatting);
            }
        }

        this.isGroupForced = userData.getBoolean("force_group");

        String groupName = userData.getString("group");
        if (groupName == null || groupName.equals("")) {
            this.group = getGroupPerPerms();
        } else {
            if (groupHandler.isGroup(groupName) && (hasPermission("group." + groupName) || isGroupForced || groupName.equals("default"))) {
                this.group = groupHandler.getGroup(groupName);
            } else {
                this.group = getGroupPerPerms();
                saveData("group", null);
            }
        }

        if (ConfigKeys.USE_SUBGROUPS.toBoolean()) {
            String subgroupName = userData.getString("subgroup");
            if (subgroupName != null) {
                if (groupHandler.isSubgroup(subgroupName) && hasPermission("subgroup." + subgroupName)) {
                    this.subgroup = groupHandler.getSubgroup(subgroupName);
                } else {
                    this.subgroup = getSubgroupPerPerms();
                    saveData("subgroup", null);
                }
            }
        }

        String chatColor = userData.getString("chat_color");
        if (chatColor != null && !(chatColor.length() < 2)) {
            this.chatColor = Color.getByCode(chatColor.substring(1, 2));
        }

        String chatFormatting = userData.getString("chat_formatting");
        if (chatFormatting != null && chatFormatting.length() > 1) {
            if (chatFormatting.equals("%r")) {
                this.chatFormatting = ChatFormatting.RAINBOW;
            } else {
                this.chatFormatting = ChatFormatting.getByCode(chatFormatting.substring(1, 2));
            }
        }

        String customPrefix = userData.getString("custom_prefix");
        if (hasPermission("custom.prefix") && ConfigKeys.CUSTOM_LAYOUT.toBoolean()) {
            if (customPrefix != null) {
                this.customPrefix = customPrefix.replace("&", "§");
            }
        }

        String customSuffix = userData.getString("custom_suffix");
        if (hasPermission("custom.suffix") && ConfigKeys.CUSTOM_LAYOUT.toBoolean()) {
            if (customSuffix != null) {
                this.customSuffix = customSuffix.replace("&", "§");
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        if (userData.getString("custom_prefix_update") != null) {
            try {
                this.lastPrefixUpdate = new Timestamp(dateFormat.parse(userData.getString("custom_prefix_update")).getTime()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (userData.getString("custom_suffix_update") != null) {
            try {
                this.lastSuffixUpdate = new Timestamp(dateFormat.parse(userData.getString("custom_suffix_update")).getTime()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        String gender = userData.getString("gender");
        if (gender != null && groupHandler.handleGenders()) {
            this.gender = groupHandler.getGender(gender);
        }
    }

    public boolean hasPermission(String permission) {
        if (player != null) {
            return player.hasPermission("EasyPrefix." + permission);
        }
        return true;
    }

    public String getPrefix() {
        if (hasPermission("custom.prefix") && customPrefix != null) {
            return customPrefix;
        }
        return group.getPrefix(this, true);
    }

    public void setPrefix(String prefix) {
        saveData("custom_prefix", prefix);
        if (prefix != null) {
            prefix = prefix.replace("&", "§");
        }
        this.customPrefix = prefix;
        this.instance.unloadUser(getPlayer());
    }

    public boolean hasCustomPrefix() {
        return customPrefix != null;
    }

    public boolean hasCustomSuffix() {
        return customSuffix != null;
    }

    public String getSuffix() {
        if (hasPermission("custom.suffix") && customSuffix != null) {
            return customSuffix;
        }
        return group.getSuffix(this, true);
    }

    public void setSuffix(String suffix) {
        saveData("custom_suffix", suffix);
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
            if (!(chatFormatting != null && chatFormatting.equals(ChatFormatting.RAINBOW))) {
                setChatFormatting(null);
            }
        }
        saveData("chat_color", value);
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
        saveData("chat_formatting", value);
        this.instance.unloadUser(getPlayer());
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group, Boolean force) {
        this.group = group;
        saveData("group", group.getName());
        this.customPrefix = null;
        saveData("custom_prefix", null);
        this.customSuffix = null;
        saveData("custom_suffix", null);
        this.chatColor = null;
        saveData("chat_color", null);
        this.chatFormatting = null;
        saveData("group", group.getName());
        saveData("force_group", force);
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

    public Gender getGenderType() {
        return gender;
    }

    public void setGenderType(Gender gender) {
        this.gender = gender;
        saveData("gender", gender.getName());
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

    public void sendMessage(String message) {
        player.sendMessage(Messages.getPrefix() + message);
    }

    public void saveData(String key, Object value) {
        UpdateStatement updateStatement = new UpdateStatement("users");
        updateStatement.addCondition("uuid", getPlayer().getUniqueId().toString());
        updateStatement.setValue(key, value);
        if (!updateStatement.execute()) {
            Messages.log("Couldn't save data to database! Error UDB1");
        }
    }

}
