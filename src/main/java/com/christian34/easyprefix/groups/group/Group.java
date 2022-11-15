package com.christian34.easyprefix.groups.group;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.DatabaseType;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class Group extends EasyGroup {
    private final String NAME;
    private final GroupHandler groupHandler;
    private final ChatColor groupColor;
    private final EasyPrefix instance;

    private final GroupData groupData;

    private String prefix, suffix, joinMessage, quitMessage;
    private Color chatColor;
    private ChatFormatting chatFormatting;

    public Group(GroupHandler groupHandler, @NotNull String name) {
        this.NAME = name;
        this.instance = groupHandler.getInstance();
        this.groupHandler = groupHandler;

        if (instance.getDatabaseManager().getDatabaseType().equals(DatabaseType.MYSQL)) {
            this.groupData = new GroupSqlData(this);
        } else {
            this.groupData = new GroupFileData(this);
        }

        String prefix = groupData.getPrefix();
        if (prefix != null) {
            this.prefix = prefix.replace("§", "&");
        }

        String suffix = groupData.getSuffix();
        if (suffix != null) {
            this.suffix = suffix.replace("§", "&");
        }

        Character formatting = groupData.getFormatting();
        if (formatting != null) {
            this.chatFormatting = ChatFormatting.getByCode(String.valueOf(formatting));
            if (this.chatFormatting == null) {
                Debug.warn("Couldn't find chat formatting '" + formatting + "'! (group: " + name + ")");
            }
        }

        Character color = groupData.getColor();
        if (color != null) {
            this.chatColor = Color.getByCode(String.valueOf(color));
            if (this.chatColor == null) {
                Debug.warn("Couldn't find chat color '" + color + "'! (group: " + name + ")");
                this.chatColor = Color.GRAY;
            }
        } else {
            if (this.chatFormatting != null && this.chatFormatting.equals(ChatFormatting.RAINBOW)) {
                this.chatColor = Color.GRAY;
            } else {
                setChatColor(Color.GRAY);
            }
        }

        this.groupColor = getGroupColor(prefix);
        this.joinMessage = groupData.getJoinMessage();
        this.quitMessage = groupData.getQuitMessage();
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
        this.groupData.setJoinMessage(this.joinMessage);
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
        this.groupData.setQuitMessage(this.quitMessage);
    }

    @Override
    public void setPrefix(@Nullable String prefix) {
        if (prefix != null) {
            prefix = prefix.replace("§", "&");
        }
        this.prefix = prefix;
        this.groupData.setPrefix(prefix);
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
    public void setSuffix(@Nullable String suffix) {
        if (suffix != null) {
            suffix = suffix.replace("§", "&");
        }
        this.suffix = suffix;
        this.groupData.setSuffix(suffix);
    }

    @Override
    @Nullable
    public String getSuffix() {
        return suffix;
    }

    @Override
    public void delete() {
        this.groupData.delete();
        instance.getGroupHandler().getGroups().remove(this);
        instance.reloadUsers();
    }

    @Override
    @NotNull
    public ChatColor getGroupColor() {
        return groupColor;
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
        this.groupData.setColor(color.getChar());
    }

    @Nullable
    public ChatFormatting getChatFormatting() {
        return chatFormatting;
    }

    public void setChatFormatting(@Nullable ChatFormatting chatFormatting) {
        this.chatFormatting = chatFormatting;
        Character val = (chatFormatting != null) ? chatFormatting.getChar() : null;
        this.groupData.setFormatting(val);
    }

    public GroupData getGroupData() {
        return groupData;
    }

}
