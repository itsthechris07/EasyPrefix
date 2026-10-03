package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.TaskManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.christian34.easyprefix.utils.TextUtils.miniMessage;

/**
 * EasyPrefix 2026.
 * <p>
 * Players mention others with @name in the chat: the mention is colored for everyone, the mentioned player sees it
 * highlighted (with a mark in front of the line) and hears a sound.
 *
 * @author Christian34
 */
public final class Mentions {
    private final ConfigData config;
    /**
     * lower case name -> name of all players the writer could mention
     */
    private final Map<String, String> names;
    private final Pattern pattern;
    private final Set<Player> mentioned;

    private Mentions(ConfigData config, Map<String, String> names, Pattern pattern, Set<Player> mentioned) {
        this.config = config;
        this.names = names;
        this.pattern = pattern;
        this.mentioned = mentioned;
    }

    /**
     * @return the mentions in the text, null if there are none or mentions are disabled
     */
    @Nullable
    public static Mentions find(@NotNull Player source, @NotNull String text) {
        ConfigData config = EasyPrefix.getInstance().getConfigData();
        if (!config.getBoolean(ConfigData.Keys.MENTIONS) || text.indexOf('@') < 0) return null;

        Map<String, Player> players = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            // vanished players can't be mentioned
            if (player == source || source.canSee(player)) players.put(player.getName().toLowerCase(Locale.ROOT), player);
        }
        if (players.isEmpty()) return null;

        // longest names first, so @Steve2 is not matched as @Steve
        String alternatives = players.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        Pattern pattern = Pattern.compile("(?<!\\w)@(" + alternatives + ")(?!\\w)", Pattern.CASE_INSENSITIVE);

        Set<Player> mentioned = new HashSet<>();
        Matcher matcher = pattern.matcher(text);
        boolean found = false;
        while (matcher.find()) {
            found = true;
            Player player = players.get(matcher.group(1).toLowerCase(Locale.ROOT));
            // writing your own name is formatted, but does not ping yourself
            if (player != source) mentioned.add(player);
        }
        if (!found) return null;

        Map<String, String> names = new HashMap<>();
        players.forEach((lower, player) -> names.put(lower, player.getName()));
        return new Mentions(config, names, pattern, mentioned);
    }

    /**
     * @param viewer the player who reads the message - their own mentions are highlighted, null for everyone else
     * @return the message with formatted mentions
     */
    public Component format(@NotNull Component message, @Nullable Player viewer) {
        String format = config.getString(ConfigData.Keys.MENTION_FORMAT, "@{name}");
        String highlight = config.getString(ConfigData.Keys.MENTION_HIGHLIGHT, format);
        return message.replaceText(TextReplacementConfig.builder().match(pattern).replacement((result, builder) -> {
            String name = names.getOrDefault(result.group(1).toLowerCase(Locale.ROOT), result.group(1));
            boolean own = viewer != null && viewer.getName().equalsIgnoreCase(name);
            return miniMessage().deserialize((own ? highlight : format).replace("{name}", miniMessage().escapeTags(name)));
        }).build());
    }

    /**
     * @return the mark in front of the chat line of mentioned players
     */
    public Component linePrefix() {
        String prefix = config.getString(ConfigData.Keys.MENTION_LINE_PREFIX);
        return prefix == null || prefix.isEmpty() ? Component.empty() : miniMessage().deserialize(prefix);
    }

    /**
     * plays the sound of config.yml to the mentioned players that receive the message
     */
    public void playSound(@NotNull Collection<? extends Audience> viewers) {
        String name = config.getString(ConfigData.Keys.MENTION_SOUND);
        if (name == null || name.isBlank()) return;
        Sound sound;
        try {
            sound = Sound.sound(Key.key(name.trim().toLowerCase(Locale.ROOT)), Sound.Source.PLAYER,
                    (float) config.getDouble(ConfigData.Keys.MENTION_VOLUME), (float) config.getDouble(ConfigData.Keys.MENTION_PITCH));
        } catch (InvalidKeyException e) {
            Debug.warn("Invalid mention sound in config.yml: " + name);
            return;
        }
        for (Player player : mentioned) {
            if (viewers.contains(player)) TaskManager.run(player, () -> player.playSound(sound, Sound.Emitter.self()));
        }
    }

    public Set<Player> getMentioned() {
        return Collections.unmodifiableSet(mentioned);
    }

}
