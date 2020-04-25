package com.christian34.easyprefix.bungeecord;

import com.christian34.easyprefix.EasyPrefix;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@Deprecated
public class MessageSender {

    public void sendChat(Player sender, ArrayList<Player> blockedPlayers, String text) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Player player : blockedPlayers) {
            stringBuilder.append(player.getName() + ", ");
        }
        String blockedPlayersList = "";
        if (stringBuilder.length() >= 2) {
            blockedPlayersList = stringBuilder.toString().substring(0, stringBuilder.length() - 2);
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(sender.getName());
        out.writeUTF(blockedPlayersList);
        out.writeUTF(text);

        sender.sendPluginMessage(EasyPrefix.getInstance(), "easyprefix:chatlistener", out.toByteArray());
    }

    public void sendQuitMessage(Player player, String quitMessage) {
        Bukkit.broadcastMessage("1");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(quitMessage);
        out.writeUTF(player.getName());
        out.writeUTF("quit");
        Bukkit.getScheduler().runTaskLater(EasyPrefix.getInstance(), () -> {
            player.sendPluginMessage(EasyPrefix.getInstance(), "easyprefix:joinquit", out.toByteArray());
        }, 1L);
    }

    public void sendJoinMessage(Player player, String joinMessage) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(joinMessage);
        out.writeUTF(player.getName());
        out.writeUTF("join");
        Bukkit.getScheduler().runTaskLater(EasyPrefix.getInstance(), () -> {
            player.sendPluginMessage(EasyPrefix.getInstance(), "easyprefix:joinquit", out.toByteArray());
        }, 1L);
    }

}