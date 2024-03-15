package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.sql.UpdateStatement;
import com.christian34.easyprefix.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * EasyPrefix 2023.
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
    private Color color;
    private Decoration decoration;
    private String customPrefix;
    private String customSuffix;
    private boolean isGroupForced;
    private long lastPrefixUpdate, lastSuffixUpdate;
    private TagResolver.Builder tagResvBuilder;
    private Collection<Color> colors;
    private Collection<Decoration> decorations;
    private MiniMessage miniMsg;

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
        this.tagResvBuilder = TagResolver.builder();
    }

    public Collection<Decoration> getDecorations() {
        return decorations;
    }

    public Color getColor() {
        if (color != null) {
            return color;
        }
        return getGroup().getColor();
    }

    public void setColor(@Nullable Color color) {
        this.color = color;
        String name = (color != null) ? color.getName() : null;
        saveData("chat_color", name);
    }

    public Collection<Color> getColors() {
        return colors;
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

        if (this.group == null) this.group = groupHandler.getGroup("default");

        if (instance.getConfigData().getBoolean(ConfigData.Keys.USE_TAGS)) {
            String subgroupName = userData.getString("subgroup");
            if (subgroupName != null) {
                this.subgroup = groupHandler.getSubgroup(subgroupName);
            }
        }

        String color = userData.getString("chat_color");
        this.color = Color.of(color);

        this.colors = new HashSet<>();
        for (Color c : instance.getColors()) {
            if (c.getPermission() == null || getPlayer().hasPermission(c.getPermission())) {
                this.colors.add(c);
                this.tagResvBuilder.resolver(c.tagResolver());
            }
        }

        this.decorations = new HashSet<>();
        for (Decoration d : instance.getDecorations()) {
            if (d.getPermission() == null || getPlayer().hasPermission(d.getPermission())) {
                this.decorations.add(d);
                this.tagResvBuilder.resolver(d.tagResolver());
            }
        }

        this.miniMsg = MiniMessage.builder().tags(this.tagResvBuilder.build()).build();

        String formatting = userData.getString("chat_formatting");
        this.decoration = Decoration.of(formatting);

        if (instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT)) {
            if (hasPermission("custom.prefix")) {
                this.customPrefix = TextUtils.escapeLegacyColors(userData.getString("custom_prefix"));

            }
            if (hasPermission("custom.suffix")) {
                this.customSuffix = TextUtils.escapeLegacyColors(userData.getString("custom_suffix"));
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
    }

    public Component deserialize(String text) {
        return this.miniMsg.deserialize(text);
    }

    public String deserializeToText(String text) {
        return TextUtils.deserialize(text, this);
    }


    /**
     * checks if the player has the permission
     *
     * @param permission
     * @return true if the player has the permission, returns true if @param permission is null
     */
    public boolean hasPermission(@Nullable Permission permission) {
        if (permission == null) return true;
        if (player != null) {
            return player.hasPermission("EasyPrefix." + permission);
        }
        return true;
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
        return getGroup().getPrefix();
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
        return getGroup().getSuffix();
    }

    public void setSuffix(String suffix) {
        saveData("custom_suffix", suffix);
        if (suffix != null) {
            suffix = suffix.replace("&", "§");
        }
        this.customSuffix = suffix;
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
    public Decoration getDecoration() {
        if (decoration != null) {
            return decoration;
        }
        //return getGroup().getDecoration();
        return null;
    }

    public void setDecoration(@Nullable Decoration decoration) {
        this.decoration = decoration;
        saveData("chat_formatting", (decoration != null) ? decoration.getName() : null);
    }

    @NotNull
    public Group getGroup() {
        if (group == null) return groupHandler.getGroup("default");
        return group;
    }

    public void setGroup(Group group, Boolean force) {
        this.group = group;
        saveData("group", group.getName());
        saveData("force_group", force);
        setPrefix(null);
        setSuffix(null);
        setColor(null);
        setDecoration(null);
    }

    public Subgroup getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(Subgroup subgroup) {
        if (subgroup != null && subgroup.getName().equals("null")) subgroup = null;
        this.subgroup = subgroup;
        String name = (subgroup != null) ? subgroup.getName() : null;
        saveData("subgroup", name);
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
            if (player.hasPermission("EasyPrefix.subgroup." + targetGroup.getName()) || player.hasPermission("EasyPrefix.tag." + targetGroup.getName())) {
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
            message = message.replace("%prefix%", Message.PREFIX).replace("  ", " ");
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
        UpdateStatement updateStatement = new UpdateStatement("users").addCondition("uuid", getUniqueId()).setValue(key, value);
        if (!updateStatement.execute()) {
            Debug.log("Couldn't save data to database! Error UDB1");
        }
    }

}
