package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.tables.TableUser;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.group.Group;
import com.christian34.easyprefix.groups.subgroup.Subgroup;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * EasyPrefix 2021.
 *
 * @author Christian34
 */
public class User extends AbstractUser {
    private final Player player;
    private final EasyPrefix instance;
    private final GroupHandler groupHandler;

    public User(@NotNull Player player) {
        super(player.getUniqueId());
        this.player = player;
        this.instance = EasyPrefix.getInstance();
        this.groupHandler = this.instance.getGroupHandler();

        TableUser database = getData().getDatabase();
        if (database.getUsername() == null || !database.getUsername().equals(player.getName())) {
            database.setUsername(player.getName());
            getData().save();
        }
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

    public void login() {
        Group groupName = super.group;
        if (groupName == null) {
            super.group = getGroupPerPerms();
        } else {
            if (!(hasPermission("group." + groupName) || super.isGroupForced() || groupName.getName().equals("default"))) {
                super.setGroup(null, false);
            }
        }
    }

    public boolean hasPermission(@NotNull String permission) {
        return player.hasPermission("EasyPrefix." + permission);
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

    @NotNull
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
        if (super.isGroupForced()) {
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

    public boolean hasPermission(UserPermission userPermission) {
        return player.hasPermission(userPermission.toString());
    }

    public void sendMessage(@NotNull String message) {
        player.sendMessage(Message.setPlaceholders(message));
    }

    public void sendMessage(Message message) {
        sendMessage(message.getText(false));
    }

    @Override
    public String getDisplayName() {
        return getPlayer().getDisplayName();
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

}
