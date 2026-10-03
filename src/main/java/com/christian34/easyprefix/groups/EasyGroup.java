package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2026.
 * <p>
 * Common base of groups and tags (subgroups): a name, prefix, suffix and a color.
 *
 * @author Christian34
 */
public abstract class EasyGroup {

    /**
     * @return String returns the name/id
     */
    public abstract String getName();

    public abstract String getPrefix();

    /**
     * @param prefix unformatted prefix
     */
    public abstract void setPrefix(@Nullable String prefix);

    public abstract String getSuffix();

    /**
     * @param suffix unformatted suffix
     */
    public abstract void setSuffix(@Nullable String suffix);

    /**
     * @return String returns the key for group in FileConfiguration
     */
    public abstract String getFileKey();

    /**
     * deletes the group recursively
     */
    public abstract void delete();

    /**
     * @return the legacy color code (e.g. §c) of the first color in the prefix, used to show the name in menus
     */
    @NotNull
    public String getGroupColor() {
        TextColor color = null;
        String prefix = getPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            try {
                color = firstColor(TextUtils.miniMessage().deserialize(TextUtils.escapeLegacyColors(prefix)));
            } catch (RuntimeException ignored) {
                // an invalid prefix just has no color
            }
        }
        NamedTextColor named = color == null ? NamedTextColor.DARK_PURPLE : NamedTextColor.nearestTo(color);
        return LegacyComponentSerializer.legacySection().serialize(Component.text("-", named)).replace("-", "");
    }

    @Nullable
    private static TextColor firstColor(@NotNull Component component) {
        if (component.color() != null) return component.color();
        for (Component child : component.children()) {
            TextColor color = firstColor(child);
            if (color != null) return color;
        }
        return null;
    }

}
