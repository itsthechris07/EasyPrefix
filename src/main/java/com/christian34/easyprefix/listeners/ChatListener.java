package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TextUtils;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Optional;

import static com.christian34.easyprefix.utils.TextUtils.miniMessage;

/**
 * EasyPrefix 2026.
 * <p>
 * Formats the chat with prefix, name, suffix and the chat color of players; the formatting methods are also used for
 * previews and the tab list. The name in the chat shows information on hover and can be clicked.
 *
 * @author Christian34
 */
public class ChatListener implements Listener {
    private final EasyPrefix instance;

    public ChatListener(EasyPrefix instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        if (!this.instance.formatChat()) return;
        User user = instance.getUser(e.getPlayer());

        // the text as other plugins (e.g. chat filters) left it, colored by the permissions of the player
        String text = PlainTextComponentSerializer.plainText().serialize(e.message());
        Component message = formatMessage(user, text);
        e.message(message);
        Component line = formatLine(user, message);
        e.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, msg) -> line));
    }

    /**
     * sends a sample chat message of the user, formatted exactly like the real chat
     */
    public static void sendPreview(CommandSender receiver, User user) {
        Component message = formatMessage(user, Message.COLOR_PREVIEW_TEXT.getText());
        receiver.sendMessage(Message.COLOR_PREVIEW.get("player", user.getName()));
        receiver.sendMessage(formatLine(user, message));
    }

    /**
     * @return the message in the user's chat color and formatting
     */
    public static Component formatMessage(User user, String message) {
        String msg = TextUtils.escapeLegacyColors(message);

        Component componentMsg = Component.text("");
        if (user.getColor() != null) {
            componentMsg = componentMsg.color(user.getColor().getTextColor());
        }
        if (user.getDecoration() != null) {
            componentMsg = componentMsg.decorate(user.getDecoration().getTextDecoration());
        }
        return componentMsg.append(user.deserialize(msg));
    }

    /**
     * @return the whole chat line as other players see it: prefix, name, suffix and message
     */
    public static Component formatLine(User user, Component message) {
        // the name is a separate child, otherwise the message would inherit hover and click
        return Component.text().append(withNameActions(user, formatName(user))).appendSpace().append(message).build();
    }

    /**
     * adds the hover text and click action of config.yml to the name in the chat
     */
    private static Component withNameActions(User user, Component name) {
        EasyPrefix instance = EasyPrefix.getInstance();
        ConfigData config = instance.getConfigData();
        if (config.getBoolean(ConfigData.Keys.NAME_HOVER)) {
            Component hover = NameHover.create(user);
            if (hover != null) name = name.hoverEvent(HoverEvent.showText(hover));
        }
        String click = config.getString(ConfigData.Keys.NAME_CLICK);
        if (click != null && !click.isBlank()) {
            name = name.clickEvent(ClickEvent.suggestCommand(click.replace("%player%", user.getName())));
        }
        return name;
    }

    /**
     * @return prefix, name and suffix as shown in the chat
     */
    public static Component formatName(User user) {
        Component componentPrefix = Component.text("").append(miniMessage().deserialize(resolve(user, user.getPrefix()) + displayName(user.getPlayer())));
        return Component.text("")
                .append(componentPrefix)
                .append(formatSuffix(user));
    }

    /**
     * @param layout a layout of config.yml: {prefix}, {name} and {suffix} of the player, placeholders and colors
     * @return the layout filled with the user, e.g. for the tab list or name tags
     */
    public static Component formatLayout(User user, String layout) {
        String name = user.getPlayer() != null ? displayName(user.getPlayer()) : miniMessage().escapeTags(user.getName());
        String text = resolve(user, layout)
                .replace("{prefix}", resolve(user, user.getPrefix()))
                .replace("{suffix}", resolve(user, user.getSuffix()))
                .replace("{name}", name);
        return Component.text("").append(miniMessage().deserialize(text));
    }

    /**
     * @return the display name (e.g. of nick plugins) as MiniMessage - legacy colors are kept, tags are escaped
     */
    private static String displayName(Player player) {
        String legacy = LegacyComponentSerializer.legacySection().serialize(player.displayName());
        return TextUtils.escapeLegacyColors(miniMessage().escapeTags(legacy));
    }

    /**
     * @return the prefix as shown in the chat (custom prefix or the one of the group, placeholders resolved)
     */
    public static Component formatPrefix(User user) {
        return Component.text("").append(miniMessage().deserialize(resolve(user, user.getPrefix())));
    }

    public static Component formatSuffix(User user) {
        return Component.text("").append(miniMessage().deserialize(resolve(user, user.getSuffix())));
    }

    /**
     * replaces placeholders - tags (e.g. %ep_tag_prefix%) and custom prefixes may still contain legacy colors (&amp;6)
     */
    private static String resolve(User user, String text) {
        String resolved = Optional.ofNullable(EasyPrefix.getInstance().setPlaceholders(user, text)).orElse("");
        return TextUtils.escapeLegacyColors(resolved);
    }

}
