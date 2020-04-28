package com.christian34.easyprefix.vault;

import com.christian34.easyprefix.EasyPrefix;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import static org.bukkit.Bukkit.getServer;

public class VaultManager {
    private EasyPrefix instance;
    private Chat chat = null;

    public VaultManager(EasyPrefix instance) {
        this.instance = instance;
    }

    public Chat getChat() {
        return chat;
    }

    public boolean hook() {
        chat = new ChatProvider(null);
        Bukkit.getServicesManager().register(Chat.class, chat, this.instance, ServicePriority.Highest);


        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null) {
            chat = rsp.getProvider();
        }
        return chat != null;
    }

}
