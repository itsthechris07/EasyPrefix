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
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class Group extends EasyGroup {
    private final String NAME;
    private final GroupsData groupsData;
    private final GroupHandler groupHandler;
    private final ChatColor groupColor;
    private final EasyPrefix instance;
    private Decoration decoration;
    private String prefix, suffix, joinMessage, quitMessage;
    private Color color;

    public Group(GroupHandler groupHandler, @NotNull String name) {
        this.NAME = name;
        this.instance = groupHandler.getInstance();
        this.groupsData = instance.getFileManager().getGroupsData();
        this.groupHandler = groupHandler;

        Data data;
        List<String> keys = Arrays.asList("prefix", "suffix", "chat_color", "chat_formatting", "join_msg", "quit_msg");
        if (instance.getStorageType() == StorageType.SQL) {
            SelectQuery selectQuery = new SelectQuery("groups").setColumns(keys).addCondition("group", name);
            data = selectQuery.getData();
        } else {
            Map<String, Object> storage = new HashMap<>();
            FileConfiguration fileData = getGroupsData().getData();
            for (String key : keys) {
                Object val = fileData.getString(getFileKey() + key.replace("_", "-"));
                if (val != null) storage.put(key, val);
            }
            data = new Data(storage);
        }

        this.prefix = data.getStringOr("prefix", "");
        if (this.prefix.contains("&") || this.prefix.contains("§")) {
            setPrefix(TextUtils.escapeLegacyColors(this.prefix));
        }
        this.suffix = data.getStringOr("suffix", "");
        if (this.suffix.contains("&") || this.suffix.contains("§")) {
            setPrefix(TextUtils.escapeLegacyColors(this.suffix));
        }

        String formatting = TextUtils.escapeLegacyColors(data.getString("chat_formatting"));
        if (formatting != null) {
            this.decoration = Decoration.of(formatting);
            if (this.decoration == null)
                Debug.warn(String.format("Couldn't find chat formatting '%s'! (group: %s)", formatting, name));
        } else this.decoration = null;

        String colorName = TextUtils.escapeLegacyColors(data.getString("chat_color"));
        if (colorName == null) {
            setColor(Color.of("light_gray"));
        } else {
            this.color = Color.of(colorName);
            if (this.color == null) {
                Debug.warn(String.format("Couldn't find chat color '%s'! (group: %s)", colorName, name));
                this.color = Color.of("light_gray");
            }
        }

        this.groupColor = getGroupColor(prefix);
        this.joinMessage = TextUtils.escapeLegacyColors(data.getString("join_msg"));
        this.quitMessage = TextUtils.escapeLegacyColors(data.getString("quit_msg"));
    }

    @NotNull
    public Color getColor() {
        return color;
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
    @NotNull
    public ChatColor getGroupColor() {
        return groupColor;
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

            instance.getSqlDatabase().getSqlSynchronizer().sendSyncInstruction();
        } else {
            groupsData.save(getFileKey() + key.replace("_", "-"), value);
        }
    }

    public Decoration getDecoration() {
        return decoration;
    }

    public void setDecoration(@Nullable Decoration decoration) {
        this.decoration = decoration;
        saveData("chat-formatting", (decoration != null) ? decoration.getName() : null);
    }

}
