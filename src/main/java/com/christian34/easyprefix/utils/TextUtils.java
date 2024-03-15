package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class TextUtils {
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
            text = text.replace(String.format("&%s", color.getColorCode()), color.getTagName());
        }
        return text;
    }

}
