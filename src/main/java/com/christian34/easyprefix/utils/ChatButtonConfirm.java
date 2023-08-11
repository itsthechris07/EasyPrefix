package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class ChatButtonConfirm implements Listener {
    private final Player player;
    private final TextComponent text;
    private final UUID uniqueId;
    private ButtonClickEvent buttonClickEvent;

    public ChatButtonConfirm(Player player, String text, String buttonText) {
        this.player = player;
        this.uniqueId = UUID.randomUUID();

        TextComponent button = new TextComponent(TextComponent.fromLegacyText(buttonText));
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ep confirm " + uniqueId));
        this.text = new TextComponent(TextComponent.fromLegacyText(text));
        this.text.addExtra(button);

        Bukkit.getPluginManager().registerEvents(new CommandListener(), EasyPrefix.getInstance());

        sendMessage();
    }

    private void sendMessage() {
        player.spigot().sendMessage(text);
    }

    public void onClick(ButtonClickEvent buttonClickEvent) {
        this.buttonClickEvent = buttonClickEvent;
    }

    public interface ButtonClickEvent {

        void execute();

    }

    private class CommandListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
            if (!e.getPlayer().equals(player)
                    || !e.getMessage().startsWith("/ep confirm")
                    || !e.getMessage().endsWith(uniqueId.toString()))
                return;

            e.setCancelled(true);
            buttonClickEvent.execute();
            HandlerList.unregisterAll(this);
        }

    }

}
