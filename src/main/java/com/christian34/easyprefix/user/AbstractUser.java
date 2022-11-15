package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.tables.TableUser;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.group.Group;
import com.christian34.easyprefix.groups.subgroup.Subgroup;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public abstract class AbstractUser {
    private final Data data;
    private final boolean groupForced;
    protected Group group;
    protected Subgroup subgroup;
    private String customPrefix;
    private String customSuffix;
    private Color chatColor;
    private ChatFormatting chatFormatting;
    private long lastPrefixUpdate, lastSuffixUpdate;

    protected AbstractUser(UUID uniqueId) {
        this.data = new Data(uniqueId);
        EasyPrefix instance = EasyPrefix.getInstance();
        GroupHandler groupHandler = instance.getGroupHandler();

        String groupName = this.data.getDatabase().getGroup();
        this.group = groupHandler.getGroups().stream().filter(group -> group.getName().equals(groupName)).findAny().orElse(null);
        this.groupForced = this.data.getDatabase().isForceGroup();

        if (instance.getConfigData().getBoolean(ConfigData.Keys.USE_TAGS)) {
            String subgroupName = this.data.getDatabase().getSubgroup();
            if (subgroupName != null) {
                this.subgroup = groupHandler.getSubgroup(subgroupName);
            } else this.subgroup = null;
        }

        this.chatColor = Color.getByCode(this.data.getDatabase().getChatColor());

        String formatting = this.data.getDatabase().getChatFormatting();
        if (formatting != null && formatting.length() > 1) {
            if (formatting.equals("&@")) {
                this.chatFormatting = ChatFormatting.UNDEFINED;
            } else if (formatting.equals("%r")) {
                this.chatFormatting = ChatFormatting.RAINBOW;
            } else {
                this.chatFormatting = ChatFormatting.getByCode(formatting);
            }
        } else {
            this.chatFormatting = null;
        }

        if (instance.getConfigData().getBoolean(ConfigData.Keys.CUSTOM_LAYOUT)) {
            this.customPrefix = this.data.getDatabase().getCustomPrefix();
            if (this.customPrefix != null) {
                this.customPrefix = this.customPrefix.replace("&", "§");
            }

            this.customSuffix = this.data.getDatabase().getCustomSuffix();
            if (this.customSuffix != null) {
                this.customSuffix = this.customSuffix.replace("&", "§");
            }
        } else {
            this.customPrefix = null;
            this.customSuffix = null;
        }

        Date prefUpdate = this.data.getDatabase().getCustomPrefixUpdate();
        if (prefUpdate != null) {
            this.lastPrefixUpdate = new Timestamp(prefUpdate.getTime()).getTime();
        }
        Date suffUpdate = this.data.getDatabase().getCustomSuffixUpdate();
        if (suffUpdate != null) {
            this.lastSuffixUpdate = new Timestamp(suffUpdate.getTime()).getTime();
        }
    }

    public Data getData() {
        return data;
    }

    public String getPrefix() {
        if (customPrefix != null) {
            return customPrefix;
        }
        return getGroup().getPrefix();
    }

    public void setPrefix(@Nullable String prefix) {
        this.getData().getDatabase().setCustomPrefix(prefix);
        this.getData().save();

        if (prefix != null) {
            prefix = prefix.replace("&", "§");
        }
        this.customPrefix = prefix;
    }

    public String getSuffix() {
        if (customSuffix != null) {
            return customSuffix;
        }
        return group.getSuffix();
    }

    public void setSuffix(@Nullable String suffix) {
        this.getData().getDatabase().setCustomSuffix(suffix);
        this.getData().save();

        if (suffix != null) {
            suffix = suffix.replace("&", "§");
        }
        this.customSuffix = suffix;
    }

    @NotNull
    public Group getGroup() {
        return group;
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
            value = color.getCode();
            if (getChatFormatting() != null && getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                setChatFormatting(ChatFormatting.UNDEFINED);
            }
        }
        this.getData().getDatabase().setChatColor(value);
        this.getData().save();
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
                value = "r";
            } else {
                value = chatFormatting.getCode();
            }
        }
        this.getData().getDatabase().setChatFormatting(value);
        this.getData().save();
    }

    public void setGroup(@Nullable Group group, Boolean force) {
        this.group = group;

        TableUser tableUser = this.getData().getDatabase();
        String name = (group != null) ? group.getName() : null;
        tableUser.setGroup(name);
        tableUser.setForceGroup(force);
        tableUser.setCustomPrefix(null);
        tableUser.setCustomSuffix(null);
        tableUser.setChatColor(null);
        tableUser.setChatFormatting(null);
        this.getData().save();
    }

    @Nullable
    public Subgroup getSubgroup() {
        return subgroup;
    }

    public void setSubgroup(Subgroup subgroup) {
        this.subgroup = subgroup;
        String name = (subgroup != null) ? subgroup.getName() : null;
        this.getData().getDatabase().setSubgroup(name);
        this.getData().save();
    }

    public long getLastPrefixUpdate() {
        return lastPrefixUpdate;
    }

    public void setLastPrefixUpdate(long lastPrefixUpdate) {
        this.lastPrefixUpdate = lastPrefixUpdate;
        this.getData().getDatabase().setCustomPrefixUpdate(new Date(lastPrefixUpdate));
        this.getData().save();
    }

    public long getLastSuffixUpdate() {
        return lastSuffixUpdate;
    }

    public void setLastSuffixUpdate(long lastSuffixUpdate) {
        this.lastSuffixUpdate = lastSuffixUpdate;
        this.getData().getDatabase().setCustomSuffixUpdate(new Date(lastSuffixUpdate));
        this.getData().save();
    }

    public boolean hasCustomPrefix() {
        return customPrefix != null;
    }

    public boolean hasCustomSuffix() {
        return customSuffix != null;
    }

    public boolean isGroupForced() {
        return groupForced;
    }

    public abstract String getDisplayName();

}
