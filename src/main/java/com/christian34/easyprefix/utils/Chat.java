package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public final class Chat {
    private static final MiniMessage miniMessage;
    private static final BukkitAudiences bukkitAudiences;

    static {
        bukkitAudiences = BukkitAudiences.create(EasyPrefix.getInstance());
        miniMessage = MiniMessage.miniMessage();
    }

    public static BukkitAudiences getBukkitAudiences() {
        return bukkitAudiences;
    }

    public static Component parse(@NotNull String text) {
        return miniMessage.deserialize(text);
    }

    public static void send(Player player, String text) {
        bukkitAudiences.player(player).sendMessage(parse(text));
    }

}
