package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Decoration;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TextUtils;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    /**
     * http(s) and www. links - punctuation at the end belongs to the sentence ("see https://x.org.")
     */
    private static final Pattern URL = Pattern.compile("(?i)(?<![\\w/])(?:https?://|www\\.)[^\\s<>]*[^\\s<>.,;:!?)\"']");
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
        Mentions mentions = Mentions.find(e.getPlayer(), text);
        if (mentions == null) {
            e.message(message);
            Component line = formatLine(user, message);
            e.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, msg) -> line));
            return;
        }

        e.message(mentions.format(message, null));
        Component line = formatLine(user, e.message());
        // mentioned players see their mention highlighted and a mark in front of the line
        Map<Player, Component> highlighted = new HashMap<>();
        for (Player player : mentions.getMentioned()) {
            highlighted.put(player, mentions.linePrefix().append(formatLine(user, mentions.format(message, player))));
        }
        e.renderer((source, sourceDisplayName, msg, viewer) ->
                viewer instanceof Player player ? highlighted.getOrDefault(player, line) : line);
        mentions.playSound(e.viewers());
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
        // links are inserted as placeholders: color codes and tags must not change them ("?a=1&b=2" is no aqua)
        StringBuilder msg = new StringBuilder();
        TagResolver.Builder links = TagResolver.builder();
        Matcher matcher = URL.matcher(message);
        int end = 0;
        for (int i = 0; matcher.find(); i++) {
            msg.append(TextUtils.escapeLegacyColors(message.substring(end, matcher.start()))).append("<ep_link_").append(i).append('>');
            links.resolver(Placeholder.component("ep_link_" + i, link(matcher.group())));
            end = matcher.end();
        }
        msg.append(TextUtils.escapeLegacyColors(message.substring(end)));

        Component componentMsg = Component.text("");
        // effects (e.g. gradients, shadows) are tags around the text, their hex value is only a fallback (e.g. for icons)
        StringBuilder effects = new StringBuilder();
        TagResolver.Builder effectTags = TagResolver.builder();
        Color color = user.getColor();
        if (color != null) {
            if (color.isEffect()) {
                effects.append(color.getTagName());
                effectTags.resolver(color.tagResolver());
            } else {
                componentMsg = componentMsg.color(color.getTextColor());
            }
        }
        Decoration decoration = user.getDecoration();
        if (decoration != null) {
            if (decoration.isEffect()) {
                effects.append(decoration.getTagName());
                effectTags.resolver(decoration.tagResolver());
            } else {
                componentMsg = componentMsg.decorate(decoration.getTextDecoration());
            }
        }

        Component content;
        if (effects.isEmpty()) {
            content = user.deserialize(msg.toString(), links.build());
        } else {
            // the effects are also set by admins without the permission of the player, so they are added here
            MiniMessage miniMessage = MiniMessage.builder().tags(TagResolver.resolver(user.getTagResolver(), effectTags.build())).build();
            content = miniMessage.deserialize(effects.toString() + msg, links.build());
        }
        return componentMsg.append(content);
    }

    /**
     * @return the link as written, opens the url on click ("www." links get https://)
     */
    private static Component link(String url) {
        String target = url.regionMatches(true, 0, "http", 0, 4) ? url : "https://" + url;
        return Component.text(url).clickEvent(ClickEvent.openUrl(target));
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
        return formatName(user, user.getPrefix(), user.getSuffix());
    }

    /**
     * @return the name with another prefix and suffix, e.g. to preview a custom prefix before it is saved
     */
    public static Component formatName(User user, @Nullable String prefix, @Nullable String suffix) {
        Component componentPrefix = Component.text("").append(miniMessage().deserialize(resolve(user, prefix) + displayName(user.getPlayer())));
        return Component.text("")
                .append(componentPrefix)
                .append(Component.text("").append(miniMessage().deserialize(resolve(user, suffix))));
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
