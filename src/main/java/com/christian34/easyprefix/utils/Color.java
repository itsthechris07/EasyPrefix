package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2023.
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

    public Color(@NotNull String name) throws NullPointerException, IllegalArgumentException {
        this.name = name;
        EasyPrefix instance = EasyPrefix.getInstance();
        ConfigData data = instance.getConfigData();
        final String key = String.format("chat.colors.%s.", name);
        this.displayName = data.getString(key + "display-name");
        this.tagName = String.format("<%s>", name);

        if (name.equalsIgnoreCase("rainbow")) {
            this.colorCode = "x";
            this.textColor = TextColor.fromHexString("#7188cf");
            this.tagResolver = StandardTags.rainbow();
        } else {
            this.colorCode = data.getString(key + "code");
            String hex = data.getString(key + "hex");
            if (hex == null)
                throw new NullPointerException(String.format("Color %s does not have any value for \"hex\"", name));
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

    @Nullable
    public static Color of(@Nullable String name) {
        if (name == null) return null;
        return EasyPrefix.getInstance().getColors().stream()
                .filter(color -> color.getName().equalsIgnoreCase(name)).findAny().orElse(null);
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
        if (this.name.equalsIgnoreCase("rainbow")) {
            return displayName;
        }
        return TextUtils.colorize(getTagName() + displayName);
    }

    @NotNull
    public String getColorCode() {
        return colorCode;
    }

}
