package com.christian34.easyprefix.bungeecord;

import com.christian34.easyprefix.EasyPrefix;
import org.bukkit.plugin.messaging.Messenger;

public class ChannelHandler {
    private EasyPrefix instance;

    public ChannelHandler(EasyPrefix instance) {
        this.instance = instance;
        enable();
    }

    public void enable() {
        Messenger messenger = this.instance.getServer().getMessenger();
        for (Channel channel : Channel.values()) {
            messenger.registerOutgoingPluginChannel(this.instance, channel.toString());
            messenger.registerIncomingPluginChannel(this.instance, channel.toString(), new MessageListener());
        }
    }

    public void disable() {
        Messenger messenger = this.instance.getServer().getMessenger();
        messenger.unregisterIncomingPluginChannel(this.instance);
        messenger.unregisterOutgoingPluginChannel(this.instance);
    }

    public enum Channel {
        LOGGER("logger"), QUIT("quitlistener"), JOIN("joinlistener"), CHAT("chathandler"), JQ_NOTIFIER("jqnotifier");

        private final String channel;

        Channel(String channel) {
            this.channel = channel;
        }

        @Override
        public String toString() {
            return "easyprefix:" + channel;
        }
    }

}
