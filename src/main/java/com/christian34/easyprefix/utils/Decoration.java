package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;

/**
 * EasyPrefix 2026.
 * <p>
 * A chat formatting (e.g. bold) from config.yml with its permission - or an effect like a shadow (a MiniMessage tag
 * in "tag").
 *
 * @author Christian34
 */
public class Decoration implements TextFormat {
    private final String name;
    private final Permission permission;
    private final String displayName;
    private final TagResolver tagResolver;
    private final TextDecoration textDecoration;

    public Decoration(@NotNull String name) {
        this.name = name;
        EasyPrefix instance = EasyPrefix.getInstance();
        ConfigData data = instance.getConfigData();
        final String key = String.format("chat.decorations.%s.", name);

        this.displayName = data.getString(key + "display-name");
        // like colors: 'default: true' allows the decoration for everyone, otherwise it needs a permission
        if (!data.getBoolean(key + "default")) {
            this.permission = new Permission("easyprefix.color." + this.name, String.format("allows a player to use the decorator %s.", this.name));
        } else this.permission = null;

        String effect = data.getString(key + "tag");
        if (effect != null && !effect.isBlank()) {
            // a tag around the text (e.g. a colored shadow) instead of a text decoration
            this.textDecoration = null;
            this.tagResolver = EffectTag.resolver(name, effect);
            return;
        }
        this.textDecoration = Arrays.stream(TextDecoration.values()).filter(textDecoration1 -> textDecoration1.name().equalsIgnoreCase(name)).findAny().orElse(null);
        if (textDecoration == null) {
            throw new IllegalArgumentException(String.format("Decorator %s is not a valid text decorator and has no \"tag\"! Allowed names: %s",
                    name, Arrays.toString(TextDecoration.values()).toLowerCase(Locale.ROOT)));
        }
        this.tagResolver = TagResolver.resolver(StandardTags.decorations(textDecoration));
    }

    /**
     * @param name the name of the formatting, also accepts a tag (&lt;bold&gt;) or code (&amp;l) as older versions stored them
     */
    @Nullable
    public static Decoration of(@Nullable String name) {
        if (name == null) return null;
        String value = name.trim();
        if (value.startsWith("<") && value.endsWith(">")) value = value.substring(1, value.length() - 1);
        if (value.length() == 2 && (value.charAt(0) == '&' || value.charAt(0) == '§')) value = value.substring(1);
        String search = switch (value.toLowerCase(Locale.ROOT)) {
            case "l" -> "bold";
            case "o" -> "italic";
            case "n", "underline" -> "underlined";
            case "m" -> "strikethrough";
            case "k" -> "obfuscated";
            default -> value;
        };
        return EasyPrefix.getInstance().getDecorations().stream()
                .filter(decoration -> decoration.getName().equalsIgnoreCase(search)).findAny().orElse(null);
    }

    /**
     * @return the text decoration, null for effects ({@link #isEffect()})
     */
    @Nullable
    public TextDecoration getTextDecoration() {
        return textDecoration;
    }

    /**
     * @return true if this is a tag around the whole text (e.g. a shadow) instead of a text decoration
     */
    public boolean isEffect() {
        return textDecoration == null;
    }

    public String getTagName() {
        return "<" + name + ">";
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getDisplayName() {
        return displayName;
    }

    @Override
    public @Nullable Permission getPermission() {
        return permission;
    }

    @Override
    public @NotNull TagResolver tagResolver() {
        return tagResolver;
    }

}
