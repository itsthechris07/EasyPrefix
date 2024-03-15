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

/**
 * EasyPrefix 2023.
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
        if (data.getBoolean(key + "default")) {
            this.permission = new Permission("easyprefix.color." + this.name, String.format("allows a player to use the decorator %s.", this.name));
        } else this.permission = null;

        this.textDecoration = Arrays.stream(TextDecoration.values()).filter(textDecoration1 -> textDecoration1.name().equalsIgnoreCase(name)).findAny().orElse(null);
        if (textDecoration == null) {
            throw new IllegalArgumentException(String.format("Decorator %s is not a valid text decorator! Allowed names: " + TextDecoration.values(), name));
        }
        this.tagResolver = TagResolver.resolver(StandardTags.decorations(textDecoration));
    }

    @Nullable
    public static Decoration of(String name) {
        return EasyPrefix.getInstance().getDecorations().stream()
                .filter(decoration -> decoration.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public TextDecoration getTextDecoration() {
        return textDecoration;
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
