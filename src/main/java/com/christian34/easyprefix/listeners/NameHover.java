package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * EasyPrefix 2026.
 * <p>
 * The text shown when hovering over the name of a player in the chat, set per group ({@link Group#getHover()}). It
 * knows some placeholders of its own, which are only available here and not registered at PlaceholderAPI. A line is
 * left out if one of these placeholders has no value (e.g. the tag line of a player without a tag).
 *
 * @author Christian34
 */
final class NameHover {
    private static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";

    private NameHover() {
    }

    /**
     * @return the hover text, null if there is nothing to show
     */
    @Nullable
    static Component create(@NotNull User user) {
        Map<String, String> placeholders = placeholders(user);
        List<String> lines = new ArrayList<>();
        for (String line : lines(user)) {
            String resolved = replace(line, placeholders);
            if (resolved != null) lines.add(resolved);
        }
        // blank lines at the end (e.g. before a left out line) would only add space
        while (!lines.isEmpty() && lines.getLast().isBlank()) lines.removeLast();
        if (lines.isEmpty()) return null;

        // '<reset>': the formatting of a line (e.g. bold) must not continue in the next one
        String text = Optional.ofNullable(EasyPrefix.getInstance().setPlaceholders(user, String.join("<reset><newline>", lines))).orElse("");
        return TextUtils.miniMessage().deserialize(TextUtils.escapeLegacyColors(text));
    }

    /**
     * @return the hover text of the group with legacy colors (&amp;c) translated
     */
    @NotNull
    static List<String> lines(@NotNull User user) {
        return user.getGroup().getHover().stream().map(line -> Objects.requireNonNullElse(Message.setPlaceholders(line), "")).toList();
    }

    /**
     * @return the line with the placeholders replaced, null if it has to be left out
     */
    @Nullable
    static String replace(@NotNull String line, @NotNull Map<String, String> placeholders) {
        for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
            String key = "%" + placeholder.getKey() + "%";
            if (!line.contains(key)) continue;
            // colors don't show any text themselves, they only count as empty if there is no value at all
            boolean empty = placeholder.getKey().endsWith("_color") ? placeholder.getValue() == null || placeholder.getValue().isEmpty()
                    : isEmpty(placeholder.getValue());
            if (empty) return null;
            line = line.replace(key, placeholder.getValue());
        }
        return line;
    }

    /**
     * values like a prefix that only consists of a color ("&lt;gray&gt;") don't show anything either
     */
    private static boolean isEmpty(@Nullable String value) {
        if (value == null || value.isBlank()) return true;
        try {
            Component component = TextUtils.miniMessage().deserialize(TextUtils.escapeLegacyColors(value));
            return PlainTextComponentSerializer.plainText().serialize(component).isBlank();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    @NotNull
    static Map<String, String> placeholders(@NotNull User user) {
        Player player = user.getPlayer();
        Subgroup tag = user.getSubgroup();
        String displayName = player == null ? user.getName()
                : LegacyComponentSerializer.legacySection().serialize(player.displayName());
        String plainDisplayName = player == null ? user.getName()
                : PlainTextComponentSerializer.plainText().serialize(player.displayName());

        // longer names first, so %ep_user_tag% does not replace a part of %ep_user_tag_prefix%
        Map<String, String> values = new LinkedHashMap<>();
        values.put("ep_user_display_name", displayName);
        values.put("ep_user_real_name", plainDisplayName.equals(user.getName()) ? "" : user.getName());
        values.put("ep_user_name", user.getName());
        values.put("ep_user_group_prefix", user.getGroup().getPrefix());
        values.put("ep_user_group_color", user.getGroup().getGroupColor());
        values.put("ep_user_group", user.getGroup().getName());
        values.put("ep_user_tag_prefix", tag == null ? "" : tag.getPrefix());
        values.put("ep_user_tag_color", tag == null ? "" : tag.getGroupColor());
        values.put("ep_user_tag", tag == null ? "" : tag.getName());
        values.put("ep_user_playtime", player == null ? "" : playtime(player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20L));
        values.put("ep_user_first_join", player == null ? "" : date(player.getFirstPlayed()));
        values.put("ep_user_world", player == null ? "" : player.getWorld().getName());
        return values;
    }

    /**
     * @return e.g. "3d 4h", "12h 30m" or "5m"
     */
    @NotNull
    static String playtime(long seconds) {
        long minutes = seconds / 60, hours = minutes / 60, days = hours / 24;
        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        return Math.max(minutes, 1) + "m";
    }

    @NotNull
    private static String date(long millis) {
        if (millis <= 0) return "";
        String pattern = EasyPrefix.getInstance().getConfigData().getString(ConfigData.Keys.DATE_FORMAT, DEFAULT_DATE_FORMAT);
        DateTimeFormatter formatter;
        try {
            formatter = DateTimeFormatter.ofPattern(pattern);
        } catch (IllegalArgumentException ex) {
            Debug.warn(String.format("Invalid date-format '%s' in config.yml, using %s instead!", pattern, DEFAULT_DATE_FORMAT));
            formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
        }
        return formatter.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()));
    }

}
