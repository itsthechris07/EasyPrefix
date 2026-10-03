package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.TextUtils;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2026.
 * <p>
 * Provides prefixes and suffixes of EasyPrefix through Vault, e.g. for other chat plugins (handle-chat: false). Texts
 * are returned with legacy colors (§) and all placeholders replaced.
 *
 * @author Christian34
 */
// the legacy api is deprecated in VaultUnlocked, but still the one most plugins use
@SuppressWarnings("deprecation")
class ChatProvider {

    ChatProvider(ExpansionManager expansionManager) {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        Permission perms = rsp == null ? null : rsp.getProvider();
        Bukkit.getServicesManager().register(Chat.class, new Handler(expansionManager, perms), expansionManager.getInstance(), ServicePriority.Highest);
    }

    static class Handler extends Chat {
        private final ExpansionManager expansionManager;
        @Nullable
        private final Permission perms;

        Handler(ExpansionManager expansionManager, @Nullable Permission perms) {
            super(perms);
            this.expansionManager = expansionManager;
            this.perms = perms;
        }

        private static String legacy(@Nullable String text) {
            return text == null ? "" : TextUtils.colorizeOpenEnd(TextUtils.escapeLegacyColors(text));
        }

        private GroupHandler groups() {
            return expansionManager.getInstance().getGroupHandler();
        }

        @Nullable
        private Group group(String name) {
            return name != null && groups().isGroup(name) ? groups().getGroup(name) : null;
        }

        @Override
        public String getName() {
            return "EasyPrefix";
        }

        @Override
        public boolean isEnabled() {
            return expansionManager.getInstance().isEnabled();
        }

        // --- players ---

        @Override
        public String getPlayerPrefix(String world, String player) {
            User user = expansionManager.getUser(player);
            return user == null ? "" : expansionManager.toLegacy(user, user.getPrefix());
        }

        /**
         * sets a custom prefix - it is only shown if the player has the permission EasyPrefix.custom.prefix
         */
        @Override
        public void setPlayerPrefix(String world, String player, String prefix) {
            User user = expansionManager.getUser(player);
            if (user != null) user.setPrefix(prefix);
        }

        @Override
        public String getPlayerSuffix(String world, String player) {
            User user = expansionManager.getUser(player);
            return user == null ? "" : expansionManager.toLegacy(user, user.getSuffix());
        }

        /**
         * sets a custom suffix - it is only shown if the player has the permission EasyPrefix.custom.suffix
         */
        @Override
        public void setPlayerSuffix(String world, String player, String suffix) {
            User user = expansionManager.getUser(player);
            if (user != null) user.setSuffix(suffix);
        }

        // --- groups ---

        @Override
        public String getGroupPrefix(String world, String group) {
            Group target = group(group);
            return target == null ? "" : legacy(target.getPrefix());
        }

        @Override
        public void setGroupPrefix(String world, String group, String prefix) {
            Group target = group(group);
            if (target != null) target.setPrefix(prefix);
        }

        @Override
        public String getGroupSuffix(String world, String group) {
            Group target = group(group);
            return target == null ? "" : legacy(target.getSuffix());
        }

        @Override
        public void setGroupSuffix(String world, String group, String suffix) {
            Group target = group(group);
            if (target != null) target.setSuffix(suffix);
        }

        // group membership comes from the permission plugin if there is one, otherwise from EasyPrefix

        @Override
        public String getPrimaryGroup(String world, OfflinePlayer player) {
            if (perms != null) return super.getPrimaryGroup(world, player);
            User user = expansionManager.getUser(player);
            return user == null ? null : user.getGroup().getName();
        }

        @Override
        public String getPrimaryGroup(String world, String player) {
            if (perms != null) return super.getPrimaryGroup(world, player);
            User user = expansionManager.getUser(player);
            return user == null ? null : user.getGroup().getName();
        }

        @Override
        public String[] getPlayerGroups(String world, OfflinePlayer player) {
            if (perms != null) return super.getPlayerGroups(world, player);
            String group = getPrimaryGroup(world, player);
            return group == null ? new String[0] : new String[]{group};
        }

        @Override
        public String[] getPlayerGroups(String world, String player) {
            if (perms != null) return super.getPlayerGroups(world, player);
            String group = getPrimaryGroup(world, player);
            return group == null ? new String[0] : new String[]{group};
        }

        @Override
        public boolean playerInGroup(String world, OfflinePlayer player, String group) {
            if (perms != null) return super.playerInGroup(world, player, group);
            return group != null && group.equalsIgnoreCase(getPrimaryGroup(world, player));
        }

        @Override
        public boolean playerInGroup(String world, String player, String group) {
            if (perms != null) return super.playerInGroup(world, player, group);
            return group != null && group.equalsIgnoreCase(getPrimaryGroup(world, player));
        }

        @Override
        public String[] getGroups() {
            if (perms != null) return super.getGroups();
            return groups().getGroups().stream().map(Group::getName).sorted().toArray(String[]::new);
        }

        // --- info nodes: EasyPrefix has none, so every lookup returns the given default ---

        @Override
        public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
            return defaultValue;
        }

        @Override
        public void setPlayerInfoInteger(String world, String player, String node, int value) {
        }

        @Override
        public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
            return defaultValue;
        }

        @Override
        public void setGroupInfoInteger(String world, String group, String node, int value) {
        }

        @Override
        public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
            return defaultValue;
        }

        @Override
        public void setPlayerInfoDouble(String world, String player, String node, double value) {
        }

        @Override
        public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
            return defaultValue;
        }

        @Override
        public void setGroupInfoDouble(String world, String group, String node, double value) {
        }

        @Override
        public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
            return defaultValue;
        }

        @Override
        public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {
        }

        @Override
        public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
            return defaultValue;
        }

        @Override
        public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
        }

        @Override
        public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
            return defaultValue;
        }

        @Override
        public void setPlayerInfoString(String world, String player, String node, String value) {
        }

        @Override
        public String getGroupInfoString(String world, String group, String node, String defaultValue) {
            return defaultValue;
        }

        @Override
        public void setGroupInfoString(String world, String group, String node, String value) {
        }

    }

}
