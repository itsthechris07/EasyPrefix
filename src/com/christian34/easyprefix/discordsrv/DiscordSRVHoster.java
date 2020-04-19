package com.christian34.easyprefix.discordsrv;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.FileManager;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DiscordSRVHoster {
    private EasyPrefix instance;
    private String channelName;
    private String channelId;
    private boolean sendChat, sendJoinQuit;

    public DiscordSRVHoster(EasyPrefix instance) {
        this.instance = instance;
        FileConfiguration config = FileManager.getConfig().getFileData();
        this.channelId = config.getString("config.discordsrv.channel.id");
        this.channelName = config.getString("config.discordsrv.channel.name");
        this.sendChat = config.getBoolean("config.discordsrv.log-chat");
        this.sendJoinQuit = config.getBoolean("config.discordsrv.log-joinquit");
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChannelId() {
        return channelId;
    }

    public boolean isSendChat() {
        return sendChat;
    }

    public boolean isSendJoinQuit() {
        return sendJoinQuit;
    }

    public void sendChat(Player player, String message) {
        if (isSendChat()) DiscordSRV.getPlugin().processChatMessage(player, message, this.channelName, false);
    }

    public void sendJoinQuitMessage(String message) {
        if (isSendJoinQuit()) {
            DiscordUtil.sendMessage(DiscordUtil.getTextChannelById(this.channelId), message);
        }
    }

}
