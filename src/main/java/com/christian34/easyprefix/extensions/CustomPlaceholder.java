package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Debug;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2026.
 * <p>
 * Placeholders of EasyPrefix for PlaceholderAPI: %ep_&lt;identifier&gt;%. Texts are returned with legacy colors (§).
 *
 * @author Christian34
 */
class CustomPlaceholder extends PlaceholderExpansion {
    private final ExpansionManager expansionManager;

    CustomPlaceholder(ExpansionManager expansionManager) {
        this.expansionManager = expansionManager;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * the expansion belongs to EasyPrefix and must not be unloaded on /papi reload
     */
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "ep";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "Christian34";
    }

    @Override
    @NotNull
    public String getVersion() {
        return expansionManager.getInstance().getPluginMeta().getVersion();
    }

    @Override
    @Nullable
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String identifier) {
        User user = expansionManager.getUser(player);
        if (user == null) return "";
        return switch (identifier.toLowerCase()) {
            case "prefix", "user_prefix" -> expansionManager.toLegacy(user, user.getPrefix());
            case "suffix", "user_suffix" -> expansionManager.toLegacy(user, user.getSuffix());
            case "user_group" -> user.getGroup().getName();
            case "user_tag", "user_subgroup" -> user.getSubgroup() == null ? "" : user.getSubgroup().getName();
            case "user_subgroup_prefix", "tag_prefix" ->
                    user.getSubgroup() == null ? "" : expansionManager.toLegacy(user, user.getSubgroup().getPrefix());
            case "user_subgroup_suffix", "tag_suffix" ->
                    user.getSubgroup() == null ? "" : expansionManager.toLegacy(user, user.getSubgroup().getSuffix());
            case "user_chatcolor" -> user.getColor() == null ? "" : user.getColor().getTagName();
            case "user_color" -> user.getColor() == null ? "" : user.getColor().getName();
            case "user_formatting" -> user.getDecoration() == null ? "" : user.getDecoration().getName();
            default -> {
                // placeholders are requested very often (e.g. by scoreboards), so this is only logged on debug level
                Debug.recordAction("Unknown placeholder: %ep_" + identifier + "%");
                yield null;
            }
        };
    }

}
