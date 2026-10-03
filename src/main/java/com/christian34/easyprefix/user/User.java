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
import java.util.*;

/**
 * EasyPrefix 2026.
 * <p>
 * A player with their group, tag, custom prefix and suffix, chat color and formatting.
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
    private boolean mentionsDisabled;
    private long lastPrefixUpdate, lastSuffixUpdate;
    private TagResolver.Builder tagResvBuilder;
    private TagResolver tagResolver = TagResolver.empty();
    private Collection<Color> colors;
    private Collection<Decoration> decorations;
    private MiniMessage miniMsg;
    private boolean preloaded;

    public User(@NotNull OfflinePlayer player) {
        this.player = null;
        this.offlinePlayer = player;
        this.instance = EasyPrefix.getInstance();
        this.groupHandler = this.instance.getGroupHandler();
        this.userData = new UserData(player.getUniqueId());
        this.tagResvBuilder = TagResolver.builder();
    }

    public User(@NotNull Player player) {
        this(player, new UserData(player.getUniqueId()), false);
    }

    /**
     * @param userData already loaded while the player was logging in, so the first {@link #login()} does not query
     *                 the database on the main thread
     */
    public User(@NotNull Player player, @NotNull UserData userData) {
        this(player, userData, true);
    }

    private User(@NotNull Player player, @NotNull UserData userData, boolean preloaded) {
        this.player = player;
        this.instance = EasyPrefix.getInstance();
        this.groupHandler = this.instance.getGroupHandler();
        this.userData = userData;
        this.tagResvBuilder = TagResolver.builder();
        this.preloaded = preloaded;
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
        if (preloaded) {
            this.preloaded = false;
        } else {
            userData.loadData();
        }
        // a new builder, otherwise colors the player lost the permission for would still be resolved
        this.tagResvBuilder = TagResolver.builder();

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
            if (c.getPermission() == null || hasPermission(c.getPermission())) {
                this.colors.add(c);
                this.tagResvBuilder.resolver(c.tagResolver());
            }
        }

        this.decorations = new HashSet<>();
        for (Decoration d : instance.getDecorations()) {
            if (d.getPermission() == null || hasPermission(d.getPermission())) {
                this.decorations.add(d);
                this.tagResvBuilder.resolver(d.tagResolver());
            }
        }

        this.tagResolver = this.tagResvBuilder.build();
        this.miniMsg = MiniMessage.builder().tags(this.tagResolver).build();

        String formatting = userData.getString("chat_formatting");
        if ("%r".equals(formatting)) {
            // older versions stored rainbow as a formatting, it is a color now
            formatting = null;
            this.color = Color.of("rainbow");
            saveData("chat_formatting", null);
            saveData("chat_color", "rainbow");
        }
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

        this.lastPrefixUpdate = parseTimestamp(userData.getString("custom_prefix_update"));
        this.lastSuffixUpdate = parseTimestamp(userData.getString("custom_suffix_update"));
        this.mentionsDisabled = userData.getBoolean("mentions_disabled");
    }

    /**
     * @return the time in milliseconds, 0 if there is none (timestamps are stored as "yyyy-mm-dd hh:mm:ss[.f]")
     */
    private static long parseTimestamp(@Nullable String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return Timestamp.valueOf(value.trim()).getTime();
        } catch (IllegalArgumentException ex) {
            return 0;
        }
    }

    /**
     * @return the tags of the colors and formattings the player has the permission for
     */
    public TagResolver getTagResolver() {
        return this.tagResolver;
    }

    public Component deserialize(String text) {
        return this.miniMsg.deserialize(text);
    }

    /**
     * @param extra more tags, e.g. placeholders that are not part of the user's tags
     */
    public Component deserialize(String text, TagResolver extra) {
        return this.miniMsg.deserialize(text, extra);
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
        if (permission == null || player == null) return true;
        // the permission object already contains the full name (e.g. easyprefix.color.red)
        return player.hasPermission(permission);
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
        refreshDisplay();
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
        refreshDisplay();
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

    /**
     * @return false if the player turned off being pinged by @mentions (they can also be off in config.yml)
     */
    public boolean isMentionable() {
        return !mentionsDisabled;
    }

    public void setMentionable(boolean mentionable) {
        this.mentionsDisabled = !mentionable;
        saveData("mentions_disabled", mentionable ? null : true);
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
        refreshDisplay();
    }

    public Subgroup getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(Subgroup subgroup) {
        if (subgroup != null && subgroup.getName().equals("null")) subgroup = null;
        this.subgroup = subgroup;
        String name = (subgroup != null) ? subgroup.getName() : null;
        saveData("subgroup", name);
        refreshDisplay();
    }

    /**
     * updates tab list and name tag after a change, if the player is online
     */
    private void refreshDisplay() {
        if (player != null && player.isOnline()) instance.getDisplayManager().update(this);
    }

    public Player getPlayer() {
        return player;
    }

    public List<Group> getAvailableGroups() {
        List<Group> availableGroups = new ArrayList<>();
        for (Group targetGroup : this.instance.getGroupHandler().getGroups()) {
            if (hasPermission("group." + targetGroup.getName())) {
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
            if (hasPermission("subgroup." + targetGroup.getName()) || hasPermission("tag." + targetGroup.getName())) {
                availableGroups.add(targetGroup);
            }
        }
        return availableGroups;
    }

    /**
     * @return the permitted group with the highest priority (groups are stored in a set, so the name decides on a tie)
     */
    private Group getGroupPerPerms() {
        if (player == null) return groupHandler.getGroup("default");
        return groupHandler.getGroups().stream()
                .filter(group -> !group.getName().equals("default"))
                .filter(group -> player.hasPermission("EasyPrefix.group." + group.getName()))
                .max(Comparator.comparingInt(Group::getPriority).thenComparing(Group::getName, Comparator.reverseOrder()))
                .orElse(groupHandler.getGroup("default"));
    }

    public boolean hasPermission(UserPermission userPermission) {
        if (player == null) return true;
        return player.hasPermission(userPermission.toString());
    }

    public void sendMessage(@NotNull String message) {
        if (player == null) return;
        player.sendMessage(Message.setPlaceholders(message));
    }

    public void sendAdminMessage(@NotNull String message) {
        if (player == null) return;
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

    /**
     * @return the player, also if they are offline
     */
    @NotNull
    public OfflinePlayer getOfflinePlayer() {
        return (this.player != null) ? this.player : this.offlinePlayer;
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
