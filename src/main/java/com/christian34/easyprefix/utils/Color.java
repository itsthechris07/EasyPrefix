package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2026.
 * <p>
 * A chat color from config.yml with its hex value, legacy code and permission - or an effect like a gradient
 * (a MiniMessage tag in "tag").
 *
 * @author Christian34
 */
public final class Color implements TextFormat {
    private final String name;
    private final String displayName;
    private final String colorCode;
    private final TextColor textColor;
    private final TagResolver tagResolver;
    private final Permission permission;
    private final String tagName;
    private final boolean effect;

    public Color(@NotNull String name) throws NullPointerException, IllegalArgumentException {
        this.name = name;
        EasyPrefix instance = EasyPrefix.getInstance();
        ConfigData data = instance.getConfigData();
        final String key = String.format("chat.colors.%s.", name);
        this.displayName = data.getString(key + "display-name");
        this.tagName = String.format("<%s>", name);

        String effect = data.getString(key + "tag");
        // older configs have rainbow without a tag
        if ((effect == null || effect.isBlank()) && name.equalsIgnoreCase("rainbow")) effect = "<rainbow>";
        String hex = data.getString(key + "hex");
        // older default configs shipped gold as "#xffaa00"
        if (hex != null) hex = hex.replaceFirst("^#x", "#");

        if (effect != null && !effect.isBlank()) {
            // a tag around the text (e.g. a gradient) - there is no legacy code, the hex value is only for icons
            this.effect = true;
            this.colorCode = null;
            this.tagResolver = EffectTag.resolver(name, effect);
            TextColor color = hex != null ? TextColor.fromHexString(hex) : EffectTag.firstColor(effect);
            this.textColor = color != null ? color : NamedTextColor.WHITE;
        } else {
            this.effect = false;
            this.colorCode = data.getString(key + "code");
            if (hex == null)
                throw new NullPointerException(String.format("Color %s does not have any value for \"hex\" or \"tag\"", name));
            this.textColor = TextColor.fromHexString(hex);
            if (this.textColor == null) {
                throw new IllegalArgumentException(String.format("Color %s does not have a valid hex color!", name));
            }
            Tag tag = Tag.styling(styling -> styling.color(this.textColor));
            this.tagResolver = TagResolver.builder().tag(this.name, tag).build();
        }

        if (!data.getBoolean(key + "default")) {
            this.permission = new Permission("easyprefix.color." + this.name, String.format("allows a player to use the color %s.", this.name));
        } else this.permission = null;
    }

    /**
     * @param name the name of the color, also accepts a tag (&lt;red&gt;) or color code (&amp;c) as older versions stored them
     */
    @Nullable
    public static Color of(@Nullable String name) {
        if (name == null) return null;
        String value = name.trim();
        if (value.startsWith("<") && value.endsWith(">")) value = value.substring(1, value.length() - 1);
        if (value.length() == 2 && (value.charAt(0) == '&' || value.charAt(0) == '§')) value = value.substring(1);
        String search = value;
        for (Color color : EasyPrefix.getInstance().getColors()) {
            if (color.getName().equalsIgnoreCase(search)) return color;
        }
        if (search.length() != 1) return null;
        return EasyPrefix.getInstance().getColors().stream()
                .filter(color -> search.equalsIgnoreCase(color.getColorCode())).findAny().orElse(null);
    }

    public String getTagName() {
        return tagName;
    }

    @Override
    @NotNull
    public TagResolver tagResolver() {
        return tagResolver;
    }

    @Nullable
    public Permission getPermission() {
        return permission;
    }

    @NotNull
    public TextColor getTextColor() {
        return textColor;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDisplayName() {
        return TextUtils.colorize(getTagName() + displayName);
    }

    /**
     * @return the legacy code (e.g. "c" for red), null for effects
     */
    @Nullable
    public String getColorCode() {
        return colorCode;
    }

    /**
     * @return true if this is a tag around the whole text (e.g. a gradient or rainbow) instead of a single color
     */
    public boolean isEffect() {
        return effect;
    }

}
