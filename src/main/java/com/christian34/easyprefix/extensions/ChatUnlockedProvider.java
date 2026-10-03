package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.TextUtils;
import net.milkbowl.vault2.chat.ChatUnlocked;
import net.milkbowl.vault2.chat.InfoKey;
import net.milkbowl.vault2.helper.context.Context;
import net.milkbowl.vault2.helper.subject.Subject;
import net.milkbowl.vault2.helper.subject.SubjectType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * EasyPrefix 2026.
 * <p>
 * Provides prefixes and suffixes of EasyPrefix through the new api of VaultUnlocked (the Vault fork), next to the
 * legacy one of {@link ChatProvider}. Texts are returned with legacy colors (§) and all placeholders replaced, setters
 * take the raw text like the legacy api. The context (world) is ignored, EasyPrefix has no per-world prefixes.
 *
 * @author Christian34
 */
class ChatUnlockedProvider {

    /**
     * @return true if the installed Vault is VaultUnlocked with its new chat api
     */
    static boolean isAvailable() {
        try {
            Class.forName("net.milkbowl.vault2.chat.ChatUnlocked");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    ChatUnlockedProvider(ExpansionManager expansionManager) {
        Bukkit.getServicesManager().register(ChatUnlocked.class, new Handler(expansionManager), expansionManager.getInstance(), ServicePriority.Highest);
    }

    static class Handler implements ChatUnlocked {
        private final ExpansionManager expansionManager;

        Handler(ExpansionManager expansionManager) {
            this.expansionManager = expansionManager;
        }

        private GroupHandler groups() {
            return expansionManager.getInstance().getGroupHandler();
        }

        @Nullable
        private Group group(Subject subject) {
            String name = subject.identifier();
            return subject.type() == SubjectType.GROUP && name != null && groups().isGroup(name) ? groups().getGroup(name) : null;
        }

        @Nullable
        private User user(Subject subject) {
            if (subject.type() != SubjectType.PLAYER) return null;
            try {
                return expansionManager.getUser(Bukkit.getOfflinePlayer(subject.asUUID()));
            } catch (IllegalArgumentException ex) {
                // the identifier is not a uuid - try it as name
                return expansionManager.getUser(subject.identifier());
            }
        }

        private static String legacy(@Nullable String text) {
            return text == null || text.isEmpty() ? "" : TextUtils.colorizeOpenEnd(TextUtils.escapeLegacyColors(text));
        }

        @Override
        public boolean isEnabled() {
            return expansionManager.getInstance().isEnabled();
        }

        @Override
        public String getName() {
            return "EasyPrefix";
        }

        @Override
        public boolean hasGroupSupport() {
            return true;
        }

        // --- prefix/suffix ---

        @Override
        public Optional<String> getPrefix(Context context, Subject subject) {
            User user = user(subject);
            if (user != null) return Optional.of(expansionManager.toLegacy(user, user.getPrefix()));
            Group group = group(subject);
            return group == null ? Optional.empty() : Optional.of(legacy(group.getPrefix()));
        }

        @Override
        public CompletableFuture<Optional<String>> getPrefixAsync(Context context, Subject subject) {
            return CompletableFuture.supplyAsync(() -> getPrefix(context, subject));
        }

        @Override
        public Optional<String> getSuffix(Context context, Subject subject) {
            User user = user(subject);
            if (user != null) return Optional.of(expansionManager.toLegacy(user, user.getSuffix()));
            Group group = group(subject);
            return group == null ? Optional.empty() : Optional.of(legacy(group.getSuffix()));
        }

        @Override
        public CompletableFuture<Optional<String>> getSuffixAsync(Context context, Subject subject) {
            return CompletableFuture.supplyAsync(() -> getSuffix(context, subject));
        }

        /**
         * sets a custom prefix of a player (only shown with the permission EasyPrefix.custom.prefix) or the prefix of a
         * group
         */
        @Override
        public boolean setPrefix(Context context, Subject subject, String prefix) {
            User user = user(subject);
            if (user != null) {
                user.setPrefix(prefix);
                return true;
            }
            Group group = group(subject);
            if (group == null) return false;
            group.setPrefix(prefix);
            return true;
        }

        @Override
        public CompletableFuture<Boolean> setPrefixAsync(Context context, Subject subject, String prefix) {
            return CompletableFuture.supplyAsync(() -> setPrefix(context, subject, prefix));
        }

        /**
         * sets a custom suffix of a player (only shown with the permission EasyPrefix.custom.suffix) or the suffix of a
         * group
         */
        @Override
        public boolean setSuffix(Context context, Subject subject, String suffix) {
            User user = user(subject);
            if (user != null) {
                user.setSuffix(suffix);
                return true;
            }
            Group group = group(subject);
            if (group == null) return false;
            group.setSuffix(suffix);
            return true;
        }

        @Override
        public CompletableFuture<Boolean> setSuffixAsync(Context context, Subject subject, String suffix) {
            return CompletableFuture.supplyAsync(() -> setSuffix(context, subject, suffix));
        }

        // copies the raw text (colors, placeholders), not the resolved one

        @Nullable
        private String rawPrefix(Subject subject) {
            User user = user(subject);
            if (user != null) return user.getPrefix();
            Group group = group(subject);
            return group == null ? null : group.getPrefix();
        }

        @Nullable
        private String rawSuffix(Subject subject) {
            User user = user(subject);
            if (user != null) return user.getSuffix();
            Group group = group(subject);
            return group == null ? null : group.getSuffix();
        }

        @Override
        public boolean copyPrefix(Context context, Subject from, Subject to) {
            String prefix = rawPrefix(from);
            return prefix != null && setPrefix(context, to, prefix);
        }

        @Override
        public CompletableFuture<Boolean> copyPrefixAsync(Context context, Subject from, Subject to) {
            return CompletableFuture.supplyAsync(() -> copyPrefix(context, from, to));
        }

        @Override
        public boolean copySuffix(Context context, Subject from, Subject to) {
            String suffix = rawSuffix(from);
            return suffix != null && setSuffix(context, to, suffix);
        }

        @Override
        public CompletableFuture<Boolean> copySuffixAsync(Context context, Subject from, Subject to) {
            return CompletableFuture.supplyAsync(() -> copySuffix(context, from, to));
        }

        // --- info nodes: EasyPrefix has none ---

        @Override
        public <T> Optional<T> get(Context context, Subject subject, InfoKey<T> key) {
            return Optional.empty();
        }

        @Override
        public <T> CompletableFuture<Optional<T>> getAsync(Context context, Subject subject, InfoKey<T> key) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        @Override
        public <T> boolean set(Context context, Subject subject, InfoKey<T> key, T value) {
            return false;
        }

        @Override
        public <T> CompletableFuture<Boolean> setAsync(Context context, Subject subject, InfoKey<T> key, T value) {
            return CompletableFuture.completedFuture(false);
        }

    }

}
