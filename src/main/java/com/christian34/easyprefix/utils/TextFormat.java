package com.christian34.easyprefix.utils;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2026.
 * <p>
 * A chat color or formatting players can select.
 *
 * @author Christian34
 */
public interface TextFormat {

    @NotNull
    String getName();

    @NotNull
    String getDisplayName();

    @Nullable
    Permission getPermission();

    @NotNull
    TagResolver tagResolver();

}
