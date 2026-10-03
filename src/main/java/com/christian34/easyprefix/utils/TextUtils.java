package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;

/**
 * EasyPrefix 2026.
 * <p>
 * Converts texts between MiniMessage, legacy colors (&amp; and §) and components.
 *
 * @author Christian34
 */
public class TextUtils {
    private static final Map<String, String> LEGACY_FORMATTINGS = Map.of(
            "&l", "<bold>", "&o", "<italic>", "&n", "<underlined>", "&m", "<strikethrough>",
            "&k", "<obfuscated>", "&r", "<reset>");
    private static final LegacyComponentSerializer legacySerializer;

    static {
        legacySerializer = LegacyComponentSerializer.builder().hexColors().hexCharacter('#').build();
    }

    public static LegacyComponentSerializer getLegacySerializer() {
        return legacySerializer;
    }

    public static String colorize(String text) {
        Component parsed = instance().getMiniMessage().deserialize(text);
        return legacySerializer.serialize(parsed);
    }

    /**
     * Like {@link #colorize(String)}, but keeps the formatting at the end of the text: a prefix like "&lt;gray&gt;"
     * must color the name that other plugins append. Adventure drops styles without text, so a marker character
     * takes the style of the end and is removed again afterwards.
     */
    public static String colorizeOpenEnd(String text) {
        String marker = "";
        return colorize(text + marker).replace(marker, "");
    }

    public static MiniMessage miniMessage() {
        return instance().getMiniMessage();
    }

    private static EasyPrefix instance() {
        return EasyPrefix.getInstance();
    }

    public static String deserialize(String text) {
        return serialize(miniMessage().deserialize(text));
    }

    public static String deserialize(String text, User user) {
        return serialize(user.deserialize(text));
    }

    public static String serialize(Component component) {
        return legacySerializer.serialize(component);
    }


    /**
     * Removes all legacy colors such as "&5" and replaces them with valid tags {@link net.kyori.adventure.text.minimessage.tag.Tag}.
     *
     * @param text to modify
     * @return the input message, with potential tags
     */
    public static String escapeLegacyColors(String text) {
        if (text == null) return null;
        if (!text.contains("§") && !text.contains("&")) return text;
        text = text.replace("§", "&");
        for (Color color : instance().getColors()) {
            if (color.getColorCode() != null) text = text.replace("&" + color.getColorCode(), color.getTagName());
        }
        for (Map.Entry<String, String> formatting : LEGACY_FORMATTINGS.entrySet()) {
            text = text.replace(formatting.getKey(), formatting.getValue());
        }
        return text;
    }

}
