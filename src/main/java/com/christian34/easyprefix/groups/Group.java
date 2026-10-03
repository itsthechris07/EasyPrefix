package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.sql.Data;
import com.christian34.easyprefix.sql.DeleteStatement;
import com.christian34.easyprefix.sql.SelectQuery;
import com.christian34.easyprefix.sql.UpdateStatement;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.Decoration;
import com.christian34.easyprefix.utils.TextUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EasyPrefix 2026.
 * <p>
 * A group of players with prefix, suffix, chat color, formatting, join and quit message and a priority.
 *
 * @author Christian34
 */
public class Group extends EasyGroup {
    /**
     * the hover text if not even the default group has one
     */
    public static final List<String> DEFAULT_HOVER = List.of(
            "%ep_user_group_color%&l%ep_user_display_name%",
            "&7Name: &f%ep_user_real_name%",
            "&7Rank: %ep_user_group_color%%ep_user_group%",
            "&7Tag: %ep_user_tag_color%%ep_user_tag%",
            "&7Playtime: &f%ep_user_playtime%",
            "&7Member since: &f%ep_user_first_join%",
            "",
            "&8» Click to write a message");
    private final String NAME;
    private final GroupsData groupsData;
    private final GroupHandler groupHandler;
    private final EasyPrefix instance;
    private Decoration decoration;
    private String prefix, suffix, joinMessage, quitMessage;
    private Color color;
    private int priority;
    /**
     * own hover text of the name in the chat, null = the one of the default group
     */
    @Nullable
    private List<String> hover;

    public Group(GroupHandler groupHandler, @NotNull String name) {
        this.NAME = name;
        this.instance = groupHandler.getInstance();
        this.groupsData = instance.getFileManager().getGroupsData();
        this.groupHandler = groupHandler;

        Data data;
        List<String> keys = Arrays.asList("prefix", "suffix", "chat_color", "chat_formatting", "join_msg", "quit_msg");
        if (instance.getStorageType() == StorageType.SQL) {
            List<String> columns = new ArrayList<>(keys);
            columns.add("priority");
            columns.add("hover");
            SelectQuery selectQuery = new SelectQuery("groups").setColumns(columns).addCondition("group", name);
            data = selectQuery.getData();
            this.priority = parsePriority(data.getString("priority"));
            String hover = data.getString("hover");
            this.hover = hover == null ? null : List.of(hover.split("\n", -1));
        } else {
            Map<String, Object> storage = new HashMap<>();
            FileConfiguration fileData = getGroupsData().getData();
            for (String key : keys) {
                Object val = fileData.getString(getFileKey() + key.replace("_", "-"));
                if (val != null) storage.put(key, val);
            }
            data = new Data(storage);
            this.priority = fileData.getInt(getFileKey() + "priority", defaultPriority());
            this.hover = fileData.isList(getFileKey() + "hover") ? List.copyOf(fileData.getStringList(getFileKey() + "hover")) : null;
        }

        this.prefix = data.getStringOr("prefix", "");
        if (this.prefix.contains("&") || this.prefix.contains("§")) {
            setPrefix(TextUtils.escapeLegacyColors(this.prefix));
        }
        this.suffix = data.getStringOr("suffix", "");
        if (this.suffix.contains("&") || this.suffix.contains("§")) {
            setSuffix(TextUtils.escapeLegacyColors(this.suffix));
        }

        String formatting = TextUtils.escapeLegacyColors(data.getString("chat_formatting"));
        String colorName = TextUtils.escapeLegacyColors(data.getString("chat_color"));
        if ("%r".equals(formatting)) {
            // older versions stored rainbow as a formatting, it is a color now
            formatting = null;
            colorName = "rainbow";
            saveData("chat-formatting", null);
            saveData("chat-color", colorName);
        }
        if (formatting != null && !formatting.isBlank()) {
            this.decoration = Decoration.of(formatting);
            if (this.decoration == null)
                Debug.warn(String.format("Couldn't find chat formatting '%s'! (group: %s)", formatting, name));
        } else this.decoration = null;

        if (colorName == null || colorName.isBlank()) {
            setColor(Color.of("gray"));
        } else {
            this.color = Color.of(colorName);
            if (this.color == null) {
                Debug.warn(String.format("Couldn't find chat color '%s'! (group: %s)", colorName, name));
                this.color = Color.of("gray");
            }
        }

        this.joinMessage = TextUtils.escapeLegacyColors(data.getString("join_msg"));
        this.quitMessage = TextUtils.escapeLegacyColors(data.getString("quit_msg"));
    }

    @NotNull
    public Color getColor() {
        return color;
    }

    /**
     * players with the permissions of several groups get the one with the highest priority, the tab list is sorted by it
     */
    public int getPriority() {
        return priority;
    }

    /**
     * the default group is the fallback and therefore always below every other group
     */
    private int defaultPriority() {
        return NAME.equals("default") ? 0 : 1;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        saveData("priority", priority);
    }

    private int parsePriority(@Nullable String value) {
        if (value == null) return defaultPriority();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultPriority();
        }
    }

    /**
     * @return the own hover text of the name in the chat, null if the group uses the one of the default group
     */
    @Nullable
    public List<String> getOwnHover() {
        return hover;
    }

    /**
     * @return the hover text of the name in the chat: the own one, otherwise the one of the default group (like the
     * join and quit messages), otherwise {@link #DEFAULT_HOVER}
     */
    @NotNull
    public List<String> getHover() {
        if (hover != null) return hover;
        if (!getName().equals("default")) {
            List<String> defaultHover = groupHandler.getGroup("default").getOwnHover();
            if (defaultHover != null) return defaultHover;
        }
        return DEFAULT_HOVER;
    }

    /**
     * @param hover the lines, null or empty to use the hover of the default group (or {@link #DEFAULT_HOVER})
     */
    public void setHover(@Nullable List<String> hover) {
        this.hover = hover == null || hover.isEmpty() ? null : List.copyOf(hover);
        if (instance.getStorageType() == StorageType.SQL) {
            // mysql stores the lines in one column
            saveData("hover", this.hover == null ? null : String.join("\n", this.hover));
        } else {
            saveData("hover", this.hover);
        }
    }

    public void setColor(Color color) {
        this.color = color;
        saveData("chat-color", (color != null) ? color.getName() : null);
    }

    @Nullable
    public String getJoinMessage() {
        if ((this.joinMessage == null || this.joinMessage.isEmpty()) && !getName().equals("default")) {
            return this.groupHandler.getGroup("default").getJoinMessage();
        }
        return joinMessage;
    }

    public void setJoinMessage(@Nullable String joinMessage) {
        if (joinMessage != null) {
            joinMessage = joinMessage.replace("§", "&");
        }
        this.joinMessage = joinMessage;
        saveData("join-msg", this.joinMessage);
    }

    @Nullable
    public String getQuitMessage() {
        if ((this.quitMessage == null || this.quitMessage.isEmpty()) && !getName().equals("default")) {
            return this.groupHandler.getGroup("default").getQuitMessage();
        }
        return quitMessage;
    }

    public void setQuitMessage(@Nullable String quitMessage) {
        if (quitMessage != null) {
            quitMessage = quitMessage.replace("§", "&");
        }
        this.quitMessage = quitMessage;
        saveData("quit-msg", this.quitMessage);
    }

    @NotNull
    private GroupsData getGroupsData() {
        return groupsData;
    }

    @Override
    public void delete() {
        if (instance.getStorageType() == StorageType.LOCAL) {
            groupsData.save("groups." + getName(), null);
        } else {
            DeleteStatement deleteStatement = new DeleteStatement("groups").addCondition("group", getName());
            if (!deleteStatement.execute()) {
                Debug.log(String.format("§cCouldn't delete group '%s'!", getName()));
            }
        }
        instance.getGroupHandler().getGroups().remove(this);
        instance.reloadUsers();
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    @Nullable
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(@Nullable String prefix) {
        if (prefix != null) {
            prefix = prefix.replace("§", "&");
        }
        this.prefix = prefix;
        saveData("prefix", this.prefix);
    }

    @Override
    @Nullable
    public String getSuffix() {
        return suffix;
    }

    @Override
    public void setSuffix(@Nullable String suffix) {
        if (suffix != null) {
            suffix = suffix.replace("§", "&");
        }
        this.suffix = suffix;
        saveData("suffix", this.suffix);
    }

    @Override
    public String getFileKey() {
        return "groups." + NAME + ".";
    }

    private void saveData(@NotNull String key, @Nullable Object value) {
        Debug.recordAction(String.format("Saving group '%s'", getName()));
        if (instance.getStorageType() == StorageType.SQL) {
            UpdateStatement updateStatement = new UpdateStatement("groups")
                    .addCondition("group", this.NAME)
                    .setValue(key.replace("-", "_"), value);
            if (!updateStatement.execute()) {
                Debug.log("Couldn't save data to database! Error GDB1");
            }
        } else {
            groupsData.save(getFileKey() + key.replace("_", "-"), value);
        }
        instance.getDisplayManager().updateAll();
    }

    public Decoration getDecoration() {
        return decoration;
    }

    public void setDecoration(@Nullable Decoration decoration) {
        this.decoration = decoration;
        saveData("chat-formatting", (decoration != null) ? decoration.getName() : null);
    }

}
