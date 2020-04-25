package com.christian34.easyprefix.bungeecord;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.placeholderapi.PlaceholderAPI;
import com.christian34.easyprefix.user.User;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class MessageListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        if (!channel.startsWith("easyprefix:")) return;
        if (channel.endsWith("logger")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(data);
            Bukkit.getConsoleSender().sendMessage(in.readUTF());
        } else if (channel.endsWith("joinlistener")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(data);
            String playerName = in.readUTF();
            Player targetPlayer = Bukkit.getPlayer(playerName);
            User user = User.getUser(targetPlayer);
            String msg = user.getGroup().getJoinMessage();

            if (PlaceholderAPI.isEnabled()) msg = PlaceholderAPI.setPlaceholder(user.getPlayer(), msg);
            msg = setPlaceholder(user, msg.replace("%player%", user.getPlayer().getDisplayName()));
            Bukkit.broadcastMessage(msg);
        }
    }

    private String setPlaceholder(User user, String text) {
        if (!PlaceholderAPI.isEnabled()) {
            String sgPrefix = (user.getSubgroup() != null) ? user.getSubgroup().getPrefix(user.getGender()) : "";
            String sgSuffix = (user.getSubgroup() != null) ? user.getSubgroup().getSuffix(user.getGender()) : "";
            text = text.replace("%ep_user_prefix%", user.getPrefix()).replace("%ep_user_suffix%", user.getSuffix()).replace("%ep_user_group%", user.getGroup().getName()).replace("%ep_user_subgroup_prefix%", sgPrefix).replace("%ep_user_subgroup_suffix%", sgSuffix);
            return text;
        }
        return text;
    }

}