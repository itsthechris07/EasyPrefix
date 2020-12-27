package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.groups.gender.GenderedLayout;
import com.christian34.easyprefix.sql.*;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Group extends EasyGroup {
    private final String NAME;
    private final GroupsData groupsData;
    private final GroupHandler groupHandler;
    private final ChatColor groupColor;
    private final EasyPrefix instance;
    private String prefix, suffix, joinMessage, quitMessage;
    private Color chatColor;
    private ChatFormatting chatFormatting;
    private GenderedLayout genderedLayout = null;

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
                storage.put(key, fileData.getString(getFileKey() + key.replace("_", "-")));
            }
            data = new Data(storage);
        }

        if (groupHandler.handleGenders()) {
            this.genderedLayout = new GenderedLayout(this);
        }

        this.prefix = data.getStringOr("prefix", "");
        this.prefix = prefix.replace("§", "&");

        this.suffix = data.getStringOr("suffix", "");
        this.suffix = suffix.replace("§", "&");

        String formatting = data.getString("chat_formatting");
        if (formatting != null && formatting.length() == 2) {
            this.chatFormatting = ChatFormatting.getByCode(formatting.substring(1, 2));
            if (this.chatFormatting == null) {
                Debug.warn("Couldn't find chat formatting '" + formatting + "'! (group: " + name + ")");
                this.chatFormatting = null;
            }
        }

        String color = data.getString("chat_color");
        if (color == null || color.length() < 2) {
            if (this.chatFormatting != null && this.chatFormatting.equals(ChatFormatting.RAINBOW)) {
                this.chatColor = Color.GRAY;
            } else {
                setChatColor(Color.GRAY);
            }
        } else {
            this.chatColor = Color.getByCode(color.substring(1, 2));
            if (chatColor == null) {
                Debug.warn("Couldn't find chat color '" + color + "'! (group: " + name + ")");
                this.chatColor = Color.GRAY;
            }
        }

        this.groupColor = getGroupColor(prefix);
        this.joinMessage = data.getString("join_msg");
        this.quitMessage = data.getString("quit_msg");
    }

    @Override
    @Nullable
    public GenderedLayout getGenderedLayout() {
        return genderedLayout;
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

    private GroupsData getGroupsData() {
        return groupsData;
    }

    private void saveData(@NotNull String key, @Nullable Object value) {
        Debug.recordAction("Saving group '" + getName() + "'");
        if (instance.getStorageType() == StorageType.SQL) {
            UpdateStatement updateStatement = new UpdateStatement("groups")
                    .addCondition("group", this.NAME)
                    .setValue(key.replace("-", "_"), value);
            if (!updateStatement.execute()) {
                Debug.log("Couldn't save data to database! Error GDB1");
            }

            instance.getSqlDatabase().getSqlSynchronizer().sendSyncInstruction();
        } else {
            groupsData.setAndSave(getFileKey() + key.replace("_", "-"), value);
        }
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

    @Nullable
    @Override
    public String getPrefix(Gender gender) {
        if (this.groupHandler.handleGenders() && gender != null) {
            String text = this.genderedLayout.getPrefix(gender);
            if (text != null) return text;
        }
        return getPrefix();
    }

    @Override
    public void setPrefix(@Nullable String prefix, @NotNull Gender gender) {
        if (prefix != null) {
            prefix = prefix.replace("§", "&");
        }

        if (instance.getStorageType() == StorageType.SQL) {
            SelectQuery select = new SelectQuery("groups_gendered", "id")
                    .addCondition("group", getName())
                    .addCondition("gender", gender.getName());
            if (select.getData().isEmpty()) {
                InsertStatement insert = new InsertStatement("groups_gendered");
                insert
                        .setValue("group", getName())
                        .setValue("gender", gender.getName());
                insert.execute();
            }

            UpdateStatement update = new UpdateStatement("groups_gendered");
            update.setValue("prefix", prefix);
            update
                    .addCondition("group", getName())
                    .addCondition("gender", gender.getName());
            update.execute();
        } else {
            GroupsData data = getGroupsData();
            data.setAndSave(getFileKey() + "genders." + gender.getName() + ".prefix", prefix);
        }
        groupHandler.reloadGroup(this);
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

    @Nullable
    @Override
    public String getSuffix(Gender gender) {
        if (this.groupHandler.handleGenders() && gender != null) {
            String text = this.genderedLayout.getSuffix(gender);
            if (text != null) return text;
        }
        return getSuffix();
    }

    @Override
    public void setSuffix(@Nullable String groupSuffix, @NotNull Gender gender) {
        if (groupSuffix != null) {
            groupSuffix = groupSuffix.replace("§", "&");
        }

        if (instance.getStorageType() == StorageType.SQL) {
            SelectQuery select = new SelectQuery("groups_gendered", "id")
                    .addCondition("group", getName())
                    .addCondition("gender", gender.getName());
            if (select.getData().isEmpty()) {
                InsertStatement insert = new InsertStatement("groups_gendered");
                insert
                        .setValue("group", getName())
                        .setValue("gender", gender.getName());
                insert.execute();
            }

            UpdateStatement update = new UpdateStatement("groups_gendered");
            update.setValue("suffix", groupSuffix);
            update
                    .addCondition("group", getName())
                    .addCondition("gender", gender.getName());
            update.execute();
        } else {
            GroupsData data = getGroupsData();
            data.setAndSave(getFileKey() + "genders." + gender.getName() + ".suffix", groupSuffix);
        }
        groupHandler.reloadGroup(this);
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

    @Override
    public void delete() {
        if (instance.getStorageType() == StorageType.LOCAL) {
            groupsData.setAndSave("groups." + getName(), null);
        } else {
            DeleteStatement deleteStatement = new DeleteStatement("groups").addCondition("group", getName());
            if (!deleteStatement.execute()) {
                Debug.log("§cCouldn't delete group '" + getName() + "'!");
            }
        }
        instance.getGroupHandler().getGroups().remove(this);
        instance.reloadUsers();
    }

    @NotNull
    public Color getChatColor() {
        return chatColor;
    }

    public void setChatColor(@NotNull Color color) {
        this.chatColor = color;

        if (getChatFormatting() != null && getChatFormatting().equals(ChatFormatting.RAINBOW)) {
            setChatFormatting(null);
        }
        saveData("chat-color", color.getCode().replace("§", "&"));
    }

    @Nullable
    public ChatFormatting getChatFormatting() {
        return chatFormatting;
    }

    public void setChatFormatting(@Nullable ChatFormatting chatFormatting) {
        this.chatFormatting = chatFormatting;
        String value = null;
        if (chatFormatting != null) {
            if (chatFormatting.equals(ChatFormatting.RAINBOW)) {
                value = "%r";
            } else {
                value = chatFormatting.getCode().replace("§", "&");
            }
        }
        saveData("chat-formatting", value);
    }

}
