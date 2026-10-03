package com.christian34.easyprefix.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;

import java.time.Duration;

/**
 * EasyPrefix 2026.
 * <p>
 * A chat message with a clickable confirm button, e.g. before a custom prefix is saved. The button works once and
 * only for the player it was sent to, and expires after 5 minutes.
 *
 * @author Christian34
 */
public class ChatButtonConfirm {
    private volatile ButtonClickEvent buttonClickEvent;

    public ChatButtonConfirm(Player player, String text, String buttonText) {
        ClickEvent click = ClickEvent.callback(audience -> {
            if (audience instanceof Player clicker && clicker.getUniqueId().equals(player.getUniqueId())
                    && buttonClickEvent != null) {
                buttonClickEvent.execute();
            }
        }, ClickCallback.Options.builder().uses(1).lifetime(Duration.ofMinutes(5)).build());

        Component button = TextUtils.getLegacySerializer().deserialize(buttonText).clickEvent(click);
        player.sendMessage(TextUtils.getLegacySerializer().deserialize(text).append(button));
    }

    public void onClick(ButtonClickEvent buttonClickEvent) {
        this.buttonClickEvent = buttonClickEvent;
    }

    public interface ButtonClickEvent {

        void execute();

    }

}
