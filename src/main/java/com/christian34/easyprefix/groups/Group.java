package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.groups.gender.GenderedLayout;
import com.christian34.easyprefix.sql.*;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
            HashMap<String, Object> storage = new HashMap<>();
            for (String key : keys) {
                storage.put(key, getGroupsData().getData().getString(getFileKey() + key.replace("_", "-")));
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
            if (this.chatFormatting == null) setChatFormatting(null);
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
    public String getJoinMessage(User user) {
        String message = getJoinMessageText();
        if (message != null) {
            return translate(message, user);
        }
        return null;
    }

    @Nullable
    public String getJoinMessageText() {
        if ((this.joinMessage == null || this.joinMessage.isEmpty()) && !getName().equals("default")) {
            this.joinMessage = this.groupHandler.getGroup("default").getJoinMessageText();
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
    public String getQuitMessage(@NotNull User user) {
        String message = getQuitMessageText();
        if (message != null) {
            return translate(message, user);
        }
        return null;
    }

    @Nullable
    public String getQuitMessageText() {
        if ((this.quitMessage == null || this.quitMessage.isEmpty()) && !getName().equals("default")) {
            this.quitMessage = this.groupHandler.getGroup("default").getQuitMessageText();
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
        if (instance.getStorageType() == StorageType.SQL) {
            UpdateStatement updateStatement = new UpdateStatement("groups")
                    .addCondition("group", this.NAME)
                    .setValue(key.replace("-", "_"), value);
            if (!updateStatement.execute()) {
                Debug.log("Couldn't save data to database! Error GDB1");
            }

            instance.getSqlSynchronizer().sendSyncInstruction();
        } else {
            groupsData.setAndSave(getFileKey() + key.replace("_", "-"), value);
        }
        this.groupHandler.getInstance().getGroupHandler().load();
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    @NotNull
    public String getPrefix(@Nullable User user, boolean translate) {
        String prefix;
        if (this.groupHandler.handleGenders() && user != null) {
            prefix = this.genderedLayout.getPrefix(user.getGenderType());
            if (prefix == null) prefix = this.prefix;
        } else {
            prefix = this.prefix;
        }
        if (translate) prefix = translate(prefix, user);
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
    @NotNull
    public String getSuffix(@Nullable User user, boolean translate) {
        String groupSuffix;
        if (this.groupHandler.handleGenders() && user != null) {
            groupSuffix = this.genderedLayout.getSuffix(user.getGenderType());
            if (groupSuffix == null) groupSuffix = this.suffix;
        } else {
            groupSuffix = this.suffix;
        }
        if (translate) groupSuffix = translate(groupSuffix, user);
        return groupSuffix;
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
        instance.getUsers().clear();
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
