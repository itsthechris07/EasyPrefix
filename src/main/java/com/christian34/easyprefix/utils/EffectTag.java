package com.christian34.easyprefix.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Locale;

/**
 * EasyPrefix 2026.
 * <p>
 * A color or formatting of config.yml that is a MiniMessage tag (e.g. "&lt;gradient:#ff5555:#ffaa00&gt;" or
 * "&lt;shadow:gold&gt;"), usable with its own name (&lt;sunset&gt;). Players only get this tag, not every gradient.
 *
 * @author Christian34
 */
final class EffectTag {
    private static final TagResolver STANDARD = StandardTags.defaults();

    private EffectTag() {
    }

    /**
     * @param name the name of the color or formatting, the tag is used as &lt;name&gt;
     * @param tag  a single MiniMessage tag, e.g. "&lt;gradient:red:gold&gt;" (the brackets are optional)
     * @throws IllegalArgumentException if it is no valid tag
     */
    @NotNull
    static TagResolver resolver(@NotNull String name, @NotNull String tag) {
        String open = open(tag);
        if (create(open) == null) {
            throw new IllegalArgumentException(String.format("'%s' is not a valid MiniMessage tag", tag));
        }
        String key = name.toLowerCase(Locale.ROOT);
        return new TagResolver() {
            @Override
            public @Nullable Tag resolve(@NotNull String tagName, @NotNull ArgumentQueue arguments, @NotNull Context ctx) {
                return has(tagName) ? create(open) : null;
            }

            @Override
            public boolean has(@NotNull String tagName) {
                return key.equals(tagName);
            }
        };
    }

    /**
     * @return the color of the first letter, e.g. for the icon in the color menu
     */
    @Nullable
    static TextColor firstColor(@NotNull String tag) {
        Component text = MiniMessage.builder().tags(STANDARD).strict(false).build().deserialize(open(tag) + "X");
        Iterator<Component> components = text.iterator(ComponentIteratorType.DEPTH_FIRST);
        while (components.hasNext()) {
            TextColor color = components.next().color();
            if (color != null) return color;
        }
        return null;
    }

    private static String open(String tag) {
        String trimmed = tag.trim();
        return trimmed.startsWith("<") ? trimmed : "<" + trimmed + ">";
    }

    /**
     * Gradients and rainbows keep state while they color a text, so every use needs a new tag. There is no api to
     * create the arguments of a tag, so the tag is parsed and taken from the standard resolver.
     */
    @Nullable
    private static Tag create(String open) {
        Tag[] created = new Tag[1];
        TagResolver capture = new TagResolver() {
            @Override
            public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
                Tag tag = STANDARD.resolve(name, arguments, ctx);
                if (created[0] == null) created[0] = tag;
                return tag;
            }

            @Override
            public boolean has(@NotNull String name) {
                return STANDARD.has(name);
            }
        };
        try {
            MiniMessage.builder().tags(capture).strict(false).build().deserialize(open + "x");
        } catch (ParsingException ex) {
            return null;
        }
        return created[0];
    }

}
