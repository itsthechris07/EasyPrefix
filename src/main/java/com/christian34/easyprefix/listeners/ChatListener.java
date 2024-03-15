package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.TextUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

import static com.christian34.easyprefix.utils.TextUtils.miniMessage;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class ChatListener implements Listener {
    private final EasyPrefix instance;

    public ChatListener(EasyPrefix instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!this.instance.formatChat()) return;
        User user = instance.getUser(e.getPlayer());

        String prefix = Optional.ofNullable(instance.setPlaceholders(user, user.getPrefix())).orElse("");
        String suffix = Optional.ofNullable(instance.setPlaceholders(user, user.getSuffix())).orElse("");

        String msg = TextUtils.escapeLegacyColors(e.getMessage());

        Component componentMsg = Component.text("").color(user.getColor().getTextColor());
        if (user.getDecoration() != null) {
            componentMsg = componentMsg.decorate(user.getDecoration().getTextDecoration());
        }
        componentMsg = componentMsg.append(user.deserialize(msg));

        e.setMessage(TextUtils.serialize(componentMsg));

        Component componentPrefix = Component.text("").append(miniMessage().deserialize(prefix + user.getPlayer().getDisplayName()));
        Component componentSuffix = Component.text("").append(miniMessage().deserialize(suffix));

        Component componentFormat = Component.text("")
                .append(componentPrefix)
                .append(componentSuffix)
                .appendSpace()
                .append(componentMsg);

        e.setFormat(TextUtils.serialize(componentFormat));
    }

}
