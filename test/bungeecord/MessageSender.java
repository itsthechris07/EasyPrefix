package com.christian34.easyprefix.bungeecord;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MessageSender {
    private Player sender;
    private EasyPrefix instance;

    public MessageSender(Player sender) {
        this.sender = sender;
        this.instance = EasyPrefix.getInstance();
    }

    public void sendMessage(String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(sender.getName());
        out.writeUTF(message);
        sender.sendPluginMessage(this.instance, "easyprefix:chathandler", out.toByteArray());
    }

    public void sendQuitMessage() {
        ByteArrayDataOutput quitListenerOut = ByteStreams.newDataOutput();
        User user = EasyPrefix.getInstance().getUser(sender);
        quitListenerOut.writeUTF(sender.getName());
        quitListenerOut.writeUTF(Messages.setPlaceholder(user,user.getGroup().getQuitMessage()));
        sender.sendPluginMessage(EasyPrefix.getInstance(), ChannelHandler.Channel.QUIT.toString(), quitListenerOut.toByteArray());
    }

    public void sendJoinMessage(String joinMessage) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(joinMessage);
        out.writeUTF(sender.getName());
        out.writeUTF("join");
        Bukkit.getScheduler().runTaskLater(this.instance, () -> {
            sender.sendPluginMessage(this.instance, "easyprefix:joinquit", out.toByteArray());
        }, 1L);
    }

}