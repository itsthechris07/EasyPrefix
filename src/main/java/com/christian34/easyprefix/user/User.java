package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.sql.UpdateStatement;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * EasyPrefix 2021.
 *
 * @author Christian34
 */
public class User {
    private final Player player;
    private final EasyPrefix instance;
    private final GroupHandler groupHandler;
    private final UserData userData;
    private OfflinePlayer offlinePlayer;
    private Group group;
    private Subgroup subgroup;
    private Color chatColor;
    private Gender gender;
    private String customPrefix;
    private String customSuffix;
    private boolean isGroupForced;
    private long lastPrefixUpdate, lastSuffixUpdate;
    private ChatFormatting chatFormatting = null;

    public User(@NotNull OfflinePlayer player) {
        this.player = null;
        this.offlinePlayer = player;
        this.instance = EasyPrefix.getInstance();
        this.groupHandler = this.instance.getGroupHandler();
        this.userData = new UserData(player.getUniqueId());
    }

    public User(@NotNull Player player) {
        this.player = player;
        this.instance = EasyPrefix.getInstance();
        this.groupHandler = this.instance.getGroupHandler();
        this.userData = new UserData(player.getUniqueId());
    }

    public String getChatColorName() {
        Color color = getChatColor();
        ChatFormatting chatFormatting = getChatFormatting();
        if (chatFormatting == null) chatFormatting = ChatFormatting.UNDEFINED;
        String name;
        if (chatFormatting.equals(ChatFormatting.RAINBOW)) {
            name = chatFormatting.toString();
        } else {
            if (chatFormatting.equals(ChatFormatting.UNDEFINED)) {
                name = color.toString();
            } else {
                name = color.getCode() + chatFormatting.getCode() + color.getName() + " " + chatFormatting.getName();
            }
        }
        return name;
    }

    public long getLastPrefixUpdate() {
        return lastPrefixUpdate;
    }

    public long getLastSuffixUpdate() {
        return lastSuffixUpdate;
    }

    public void login() {
        userData.loadData();

        this.isGroupForced = userData.getBoolean("force_group");

        String groupName = userData.getString("group");
        if (groupName == null || groupName.isEmpty()) {
            this.group = getGroupPerPerms();
        } else {
            if (groupHandler.isGroup(groupName) && (hasPermission("group." + groupName) || isGroupForced || groupName.equals("default"))) {
                this.group = groupHandler.getGroup(groupName);
            } else {
                this.group = getGroupPerPerms();
                saveData("group", null);
            }
        }

        if (instance.getConfigData().getBoolean(ConfigData.Keys.USE_TAGS)) {
            String subgroupName = userData.getString("subgroup");
            if (subgroupName != null) {
                this.subgroup = groupHandler.getSubgroup(subgroupName);
            }
        }

        String color = userData.getString("chat_color");
        if (color != null && color.length() > 1) {
            this.chatColor = Color.getByCode(color.substring(1, 2));
        }

        String formatting = userData.getString("chat_formatting");
        if (formatting != null && formatting.length() > 1) {
            if (formatting.equals("&@")) {
                this.chatFormatting = ChatFormatting.UNDEFINED;
            } else if (formatting.equals("%r")) {
                this.chatFormatting = ChatFormatting.RAINBOW;
            } else {
                this.chatFormatting = ChatFormatting.getByCode(formatting.substring(1, 2));
            }
        }

        if (instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT)) {
            if (hasPermission("custom.prefix")) {
                this.customPrefix = userData.getString("custom_prefix");
            }
            if (hasPermission("custom.suffix")) {
                this.customSuffix = userData.getString("custom_suffix");
            }
        } else {
            this.customPrefix = null;
            this.customSuffix = null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        if (userData.getString("custom_prefix_update") != null) {
            try {
                this.lastPrefixUpdate = new Timestamp(dateFormat.parse(userData.getString("custom_prefix_update")).getTime()).getTime();
            } catch (ParseException ignored) {
            }
        }
        if (userData.getString("custom_suffix_update") != null) {
            try {
                this.lastSuffixUpdate = new Timestamp(dateFormat.parse(userData.getString("custom_suffix_update")).getTime()).getTime();
            } catch (ParseException ignored) {
            }
        }

        String genderName = userData.getString("gender");
        if (genderName != null && groupHandler.handleGenders()) {
            this.gender = groupHandler.getGender(genderName);
        }
    }

    public boolean hasPermission(@NotNull String permission) {
        if (player != null) {
            return player.hasPermission("EasyPrefix." + permission);
        }
        return true;
    }

    @Nullable
    public String getPrefix() {
        if (hasPermission("custom.prefix") && customPrefix != null) {
            return customPrefix;
        }
        return group.getPrefix(gender);
    }

    public void setPrefix(String prefix) {
        saveData("custom_prefix", prefix);
        if (prefix != null) {
            prefix = prefix.replace("&", "§");
        }
        this.customPrefix = prefix;
    }

    public boolean hasCustomPrefix() {
        return customPrefix != null;
    }

    public boolean hasCustomSuffix() {
        return customSuffix != null;
    }

    @Nullable
    public String getSuffix() {
        if (hasPermission("custom.suffix") && customSuffix != null) {
            return customSuffix;
        }
        return group.getSuffix(gender);
    }

    public void setSuffix(String suffix) {
        saveData("custom_suffix", suffix);
        if (suffix != null) {
            suffix = suffix.replace("&", "§");
        }
        this.customSuffix = suffix;
    }

    @NotNull
    public Set<Color> getColors() {
        if (hasPermission("color.all")) {
            return new HashSet<>(Arrays.asList(Color.getValues()));
        } else {
            Set<Color> colors = new HashSet<>();
            for (Color color : Color.getValues()) {
                if (hasPermission("color." + color.name())) {
                    colors.add(color);
                }
            }
            return Collections.unmodifiableSet(colors);
        }
    }

    @NotNull
    public Color getChatColor() {
        if (chatColor != null) {
            return chatColor;
        }
        return getGroup().getChatColor();
    }

    public void setChatColor(@Nullable Color color) {
        this.chatColor = color;
        String value = null;
        if (color != null) {
            value = color.getCode().replace("§", "&");
            if (getChatFormatting() != null && getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                setChatFormatting(ChatFormatting.UNDEFINED);
            }
        }
        saveData("chat_color", value);
    }

    @NotNull
    public Set<ChatFormatting> getChatFormattings() {
        if (hasPermission("color.all")) {
            return new HashSet<>(Arrays.asList(ChatFormatting.getValues()));
        } else {
            Set<ChatFormatting> formattings = new HashSet<>();
            for (ChatFormatting formatting : ChatFormatting.getValues()) {
                if (hasPermission("color." + formatting.name())) {
                    formattings.add(formatting);
                }
            }
            return Collections.unmodifiableSet(formattings);
        }
    }

    @Nullable
    public ChatFormatting getChatFormatting() {
        if (chatFormatting != null) {
            return chatFormatting;
        }
        return getGroup().getChatFormatting();
    }

    public void setChatFormatting(@Nullable ChatFormatting chatFormatting) {
        this.chatFormatting = chatFormatting;
        String value = null;
        if (chatFormatting != null) {
            if (chatFormatting.equals(ChatFormatting.RAINBOW)) {
                setChatColor(null);
                value = "%r";
            } else {
                value = chatFormatting.getCode().replace("§", "&");
            }
        }
        saveData("chat_formatting", value);
    }

    @NotNull
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group, Boolean force) {
        this.group = group;
        saveData("group", group.getName());
        saveData("force_group", force);
        setPrefix(null);
        setSuffix(null);
        setChatColor(null);
        setChatFormatting(null);
    }

    public Subgroup getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(Subgroup subgroup) {
        this.subgroup = subgroup;
        String name = (subgroup != null) ? subgroup.getName() : null;
        saveData("subgroup", name);
    }

    public Gender getGenderType() {
        return gender;
    }

    public void setGenderType(Gender gender) {
        this.gender = gender;
        saveData("gender", gender.getName());
    }

    public Player getPlayer() {
        return player;
    }

    public List<Group> getAvailableGroups() {
        List<Group> availableGroups = new ArrayList<>();
        for (Group targetGroup : this.instance.getGroupHandler().getGroups()) {
            if (player.hasPermission("EasyPrefix.group." + targetGroup.getName())) {
                availableGroups.add(targetGroup);
            }
        }
        if (this.isGroupForced) {
            Group currentGroup = getGroup();
            if (!availableGroups.contains(currentGroup)) availableGroups.add(currentGroup);
        }
        return availableGroups;
    }

    @NotNull
    public List<Subgroup> getAvailableSubgroups() {
        List<Subgroup> availableGroups = new ArrayList<>();
        for (Subgroup targetGroup : this.instance.getGroupHandler().getSubgroups()) {
            if (player.hasPermission("EasyPrefix.subgroup." + targetGroup.getName())
                    || player.hasPermission("EasyPrefix.tag." + targetGroup.getName())) {
                availableGroups.add(targetGroup);
            }
        }
        return availableGroups;
    }

    private Group getGroupPerPerms() {
        for (Group group : groupHandler.getGroups()) {
            if (group.getName().equals("default")) continue;
            if (player != null && player.hasPermission("EasyPrefix.group." + group.getName())) {
                return group;
            }
        }
        return groupHandler.getGroup("default");
    }

    public boolean hasPermission(UserPermission userPermission) {
        return player.hasPermission(userPermission.toString());
    }

    public void sendMessage(@NotNull String message) {
        player.sendMessage(Message.setPlaceholders(message));
    }

    public void sendAdminMessage(@NotNull String message) {
        if (!message.contains("%prefix%")) {
            message = Message.PREFIX + message;
        } else {
            message = message
                    .replace("%prefix%", Message.PREFIX)
                    .replace("  ", " ");
        }
        player.sendMessage(Message.setPlaceholders(message));
    }

    public void sendAdminMessage(Message message) {
        sendAdminMessage(message.getText(false));
    }

    public String getUniqueId() {
        if (this.player != null) {
            return this.player.getUniqueId().toString();
        } else {
            return this.offlinePlayer.getUniqueId().toString();
        }
    }

    public String getName() {
        return (this.player != null) ? this.player.getName() : this.offlinePlayer.getName();
    }

    public void saveData(String key, Object value) {
        UpdateStatement updateStatement = new UpdateStatement("users")
                .addCondition("uuid", getUniqueId())
                .setValue(key, value);
        if (!updateStatement.execute()) {
            Debug.log("Couldn't save data to database! Error UDB1");
        }
    }

}
