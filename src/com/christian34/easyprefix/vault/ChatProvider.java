package com.christian34.easyprefix.vault;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.sun.istack.internal.Nullable;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatProvider extends Chat {

    public ChatProvider(Permission perms) {
        super(perms);
    }

    @Override
    public String getName() {
        return "EasyPrefix";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getPlayerPrefix(String world, String player) {
        User user = getUser(player);
        if (user == null) return "";
        return user.getPrefix();
    }

    @Override
    public void setPlayerPrefix(String world, String player, String prefix) {

    }

    @Override
    public String getPlayerSuffix(String world, String player) {
        User user = getUser(player);
        if (user == null) return "";
        ChatFormatting chatFormatting = user.getChatFormatting();
        String chatColor = user.getChatColor().getCode();
        if (chatFormatting != null) chatColor += chatFormatting.getCode();

        return user.getSuffix() + chatColor;
    }

    @Override
    public void setPlayerSuffix(String world, String player, String suffix) {

    }

    @Override
    public String getGroupPrefix(String world, String group) {
        return "";
    }

    @Override
    public void setGroupPrefix(String world, String group, String prefix) {

    }

    @Override
    public String getGroupSuffix(String world, String group) {
        return "";
    }

    @Override
    public void setGroupSuffix(String world, String group, String suffix) {

    }

    @Override
    public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
        return 0;
    }

    @Override
    public void setPlayerInfoInteger(String world, String player, String node, int value) {

    }

    @Override
    public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
        return 0;
    }

    @Override
    public void setGroupInfoInteger(String world, String group, String node, int value) {

    }

    @Override
    public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
        return 0;
    }

    @Override
    public void setPlayerInfoDouble(String world, String player, String node, double value) {

    }

    @Override
    public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
        return 0;
    }

    @Override
    public void setGroupInfoDouble(String world, String group, String node, double value) {

    }

    @Override
    public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
        return false;
    }

    @Override
    public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {

    }

    @Override
    public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
        return false;
    }

    @Override
    public void setGroupInfoBoolean(String world, String group, String node, boolean value) {

    }

    @Override
    public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
        return null;
    }

    @Override
    public void setPlayerInfoString(String world, String player, String node, String value) {

    }

    @Override
    public String getGroupInfoString(String world, String group, String node, String defaultValue) {
        return null;
    }

    @Override
    public void setGroupInfoString(String world, String group, String node, String value) {

    }

    @Nullable
    private User getUser(String username) {
        Player player = Bukkit.getPlayer(username);
        if (player == null) return null;
        return EasyPrefix.getInstance().getUser(player);
    }

}
