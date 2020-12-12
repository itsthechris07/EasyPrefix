package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.sql.UpdateStatement;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private List<Color> colors;
    private List<ChatFormatting> chatFormattings;
    private Group group;
    private Subgroup subgroup;
    private Color chatColor;
    private Gender gender;
    private String customPrefix;
    private String customSuffix;
    private boolean isGroupForced;
    private long lastPrefixUpdate, lastSuffixUpdate;
    private ChatFormatting chatFormatting = null;

    public User(@NotNull Player player) {
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
        UserData userData = new UserData(uniqueId);
        userData.loadData();
        this.colors = new ArrayList<>();
        this.chatFormattings = new ArrayList<>();

        if (ConfigKeys.HANDLE_COLORS.toBoolean()) {
            boolean hasAll = hasPermission("color.all");

            for (Color color : Color.getValues()) {
                if (hasAll || hasPermission("Color." + color.name())) {
                    colors.add(color);
                }
            }
            for (ChatFormatting formatting : ChatFormatting.getValues()) {
                if (formatting.equals(ChatFormatting.RAINBOW)) continue;
                if (hasAll || hasPermission("Color." + formatting.name())) {
                    chatFormattings.add(formatting);
                }
            }
        }

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

        if (ConfigKeys.USE_TAGS.toBoolean()) {
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

        String cstmPrefix = userData.getString("custom_prefix");
        if (hasPermission("custom.prefix") && ConfigKeys.CUSTOM_LAYOUT.toBoolean()) {
            if (cstmPrefix != null) {
                this.customPrefix = cstmPrefix.replace("&", "§");
            }
        }

        String cstmSuffix = userData.getString("custom_suffix");
        if (hasPermission("custom.suffix") && ConfigKeys.CUSTOM_LAYOUT.toBoolean()) {
            if (cstmSuffix != null) {
                this.customSuffix = cstmSuffix.replace("&", "§");
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

    @NotNull
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

    @NotNull
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

    @NotNull
    public List<Color> getColors() {
        return colors;
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
        this.instance.unloadUser(getPlayer());
    }

    public List<ChatFormatting> getChatFormattings() {
        return chatFormattings;
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
            if (player.hasPermission("EasyPrefix.group." + group.getName())) {
                return group;
            }
        }
        return groupHandler.getGroup("default");
    }

    public boolean hasPermission(UserPermission userPermission) {
        return player.hasPermission(userPermission.toString());
    }

    public void sendMessage(@NotNull String message) {
        player.sendMessage(Objects.requireNonNull(Message.setPlaceholders(message)));
    }

    public void sendAdminMessage(@NotNull String message) {
        if (!message.contains("%prefix%")) {
            message = Message.PREFIX + message;
        } else {
            message = message.replace("%prefix%", Message.PREFIX);
        }
        player.sendMessage(Objects.requireNonNull(Message.setPlaceholders(message)));
    }

    public void sendAdminMessage(Message message) {
        sendAdminMessage(message.getText(false));
    }

    public void saveData(String key, Object value) {
        UpdateStatement updateStatement = new UpdateStatement("users")
                .addCondition("uuid", getPlayer().getUniqueId().toString())
                .setValue(key, value);
        if (!updateStatement.execute()) {
            Debug.log("Couldn't save data to database! Error UDB1");
        }
    }

}
