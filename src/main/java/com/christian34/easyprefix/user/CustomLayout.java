package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * EasyPrefix 2026.
 * <p>
 * Checks the custom prefix or suffix a player enters. Prefixes are parsed with all MiniMessage tags, so a player could
 * add click events (e.g. running a command as the one who clicks), hovers, line breaks, fonts or placeholders. Only the
 * colors and formattings the player has the permission for are kept, all other tags are escaped (shown as text).
 *
 * @author Christian34
 */
public final class CustomLayout {
    /**
     * a percent sign that is inserted after placeholders are resolved, so players can't use placeholders
     */
    public static final TagResolver PERCENT = TagResolver.resolver("percent", Tag.selfClosingInserting(Component.text("%")));
    public static final String PERCENT_TAG = "<percent>";

    private CustomLayout() {
    }

    /**
     * @return the input with legacy colors as tags, every tag the player may not use is escaped and placeholders
     * are disabled
     */
    @NotNull
    public static String sanitize(@NotNull User user, @NotNull String input) {
        String text = TextUtils.escapeLegacyColors(input.replace(PERCENT_TAG, "%"));
        TagResolver allowed = allowedTags(user);
        TagResolver forbidden = new TagResolver() {
            @Override
            public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) {
                return null;
            }

            @Override
            public boolean has(@NotNull String name) {
                return !allowed.has(name);
            }
        };
        String escaped = MiniMessage.builder().tags(forbidden).build().escapeTags(text);
        return escaped.replace("%", PERCENT_TAG);
    }

    /**
     * @param sanitized the result of {@link #sanitize(User, String)}
     * @return true if the text contains a word of the blacklist - also checked against the visible text (tags
     * between letters) and as legacy colors (a blocked "&amp;4" is also &lt;dark_red&gt; or &lt;#aa0000&gt;)
     */
    public static boolean isBlocked(@NotNull User user, @NotNull String input, @NotNull String sanitized) {
        if (user.hasPermission(UserPermission.CUSTOM_BLACKLIST)) return false;
        Component component = TextUtils.miniMessage().deserialize(sanitized);
        List<String> candidates = List.of(input.toLowerCase(Locale.ROOT),
                PlainTextComponentSerializer.plainText().serialize(component).toLowerCase(Locale.ROOT),
                TextUtils.serialize(component).replace('§', '&').toLowerCase(Locale.ROOT));
        for (String blocked : EasyPrefix.getInstance().getConfigData().getList(ConfigData.Keys.CUSTOM_LAYOUT_BLACKLIST)) {
            String word = blocked.replace('§', '&').toLowerCase(Locale.ROOT);
            if (word.isEmpty()) continue;
            if (candidates.stream().anyMatch(candidate -> candidate.contains(word))) return true;
        }
        return false;
    }

    private static TagResolver allowedTags(User user) {
        List<TagResolver> resolvers = new ArrayList<>(List.of(user.getTagResolver(), PERCENT, StandardTags.reset()));
        if (user.hasPermission(UserPermission.CUSTOM_HEX)) {
            resolvers.add(StandardTags.color());
        } else {
            // a permitted color may also be written as hex (<#ff5555>)
            resolvers.add(permittedHexColors(user));
        }
        if (user.hasPermission(UserPermission.CUSTOM_GRADIENT)) {
            resolvers.addAll(List.of(StandardTags.gradient(), StandardTags.transition(), StandardTags.pride()));
        }
        if (user.hasPermission(UserPermission.CUSTOM_SHADOW)) {
            resolvers.add(StandardTags.shadowColor());
        }
        return TagResolver.resolver(resolvers);
    }

    private static TagResolver permittedHexColors(User user) {
        return new TagResolver() {
            @Override
            public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) {
                TextColor color = color(name);
                return color != null ? Tag.styling(color) : null;
            }

            @Override
            public boolean has(@NotNull String name) {
                return color(name) != null;
            }

            private TextColor color(String name) {
                if (!name.startsWith("#")) return null;
                TextColor color = TextColor.fromHexString(name);
                if (color == null) return null;
                return user.getColors().stream().anyMatch(c -> c.getTextColor().equals(color)) ? color : null;
            }
        };
    }

}
