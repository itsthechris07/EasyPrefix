package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.sql.database.Migration;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.Decoration;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TaskManager;
import com.christian34.easyprefix.utils.UserInterface;
import com.christian34.easyprefix.utils.VersionController;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.help.result.CommandEntry;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EasyPrefix 2026.
 * <p>
 * The /easyprefix (/ep) command: help, settings and setup menus, groups, users, reload and debug information.
 *
 * @author Christian34
 */
@CommandContainer
public class CommandEasyPrefix {

    private EasyPrefix getInstance() {
        return EasyPrefix.getInstance();
    }

    private org.incendo.cloud.CommandManager<CommandSender> getCloudCommandManager() {
        return getInstance().getCommandManager().getManager();
    }

    @Command("easyprefix|ep")
    @CommandDescription("main command")
    public void mainCmd(CommandSender sender) {
        sender.sendMessage(Message.CMD_INFO.admin("version", getInstance().getPlugin().getPluginMeta().getVersion()));
    }

    @Suggestions("help_queries")
    public @NotNull List<String> suggestHelpQueries(@NotNull CommandContext<CommandSender> ctx, @NotNull String input) {
        return getCloudCommandManager().createHelpHandler().queryRootIndex(ctx.sender()).entries().stream()
                .map(CommandEntry::syntax)
                .collect(Collectors.toList());
    }

    @Command("easyprefix|ep help [query]")
    @CommandDescription("Help menu")
    public void commandHelp(CommandSender sender, @Argument(value = "query", suggestions = "help_queries") @Greedy String query) {
        getInstance().getCommandManager().getMinecraftHelp().queryCommands(query == null ? "" : query, sender);
    }

    @Command("easyprefix|ep group <group> info")
    @CommandDescription("configure groups")
    @Permission("easyprefix.admin")
    public void groupInfo(CommandSender sender, @Argument("group") Group group) {
        Message.CMD_GROUP_INFO.getList(
                "group", group.getName(),
                "group_prefix", orNone(group.getPrefix()),
                "group_suffix", orNone(group.getSuffix()),
                "color", group.getColor().getDisplayName(),
                "priority", Integer.toString(group.getPriority()),
                "join_message", orNone(group.getJoinMessage()),
                "quit_message", orNone(group.getQuitMessage())
        ).forEach(sender::sendMessage);
    }

    private static String orNone(String value) {
        return value == null ? "-" : value;
    }

    @Command("easyprefix|ep group <group> setprefix <prefix>")
    @CommandDescription("set a groups prefix. you also might use quotes at the beginning and end of the prefix")
    @Permission("easyprefix.admin")
    public void groupSetPrefix(CommandSender sender, @Argument("group") Group group, @Argument("prefix") @Greedy String prefix) {
        if (prefix.startsWith("\"") && prefix.endsWith("\"")) {
            prefix = prefix.substring(1, prefix.length() - 1);
        }
        group.setPrefix(prefix);
        sender.sendMessage(Message.CMD_GROUP_PREFIX_SET.admin("group", group.getName(), "content", orNone(group.getPrefix())));
    }

    @Command("easyprefix|ep group <group> setsuffix <suffix>")
    @CommandDescription("set a groups suffix. you also might use quotes at the beginning and end of the suffix")
    @Permission("easyprefix.admin")
    public void groupSetSuffix(CommandSender sender, @Argument("group") Group group, @Argument("suffix") @Greedy String suffix) {
        if (suffix.startsWith("\"") && suffix.endsWith("\"")) {
            suffix = suffix.substring(1, suffix.length() - 1);
        }
        group.setSuffix(suffix);
        sender.sendMessage(Message.CMD_GROUP_SUFFIX_SET.admin("group", group.getName(), "content", orNone(group.getSuffix())));
    }

    @Command("easyprefix|ep group <group> setpriority <priority>")
    @CommandDescription("players with several groups get the one with the highest priority, the tab list is sorted by it")
    @Permission("easyprefix.admin")
    public void groupSetPriority(CommandSender sender, @Argument("group") Group group, @Argument("priority") int priority) {
        group.setPriority(priority);
        sender.sendMessage(Message.CMD_GROUP_PRIORITY_SET.admin("group", group.getName(), "priority", Integer.toString(priority)));
    }

    @Command("easyprefix|ep user <user> info")
    @CommandDescription("shows user information and applied settings")
    @Permission("easyprefix.admin")
    public void userInfo(CommandSender sender, @Argument("user") User user) {
        Message.CMD_USER_INFO.getList(
                "player", user.getName(),
                "group", user.getGroup().getName(),
                "tag", user.getSubgroup() != null ? user.getSubgroup().getName() : "-",
                "user_prefix", orNone(user.getPrefix()),
                "prefix_custom", user.hasCustomPrefix() ? customized(user.getLastPrefixUpdate()) : "",
                "user_suffix", orNone(user.getSuffix()),
                "suffix_custom", user.hasCustomSuffix() ? customized(user.getLastSuffixUpdate()) : "",
                "color", user.getColor() != null ? user.getColor().getDisplayName() : "-",
                "colors", Integer.toString(user.getColors().size()),
                "formatting", user.getDecoration() != null ? user.getDecoration().getDisplayName() : "-"
        ).forEach(sender::sendMessage);
    }

    private static String customized(long lastUpdate) {
        return Message.CMD_USER_INFO_CUSTOM.get("time", new Timestamp(lastUpdate).toString());
    }

    @Command("easyprefix|ep user <user> setgroup <group>")
    @CommandDescription("sets a group to a player")
    @Permission("easyprefix.admin")
    public void userSetgroup(CommandSender sender, @Argument("user") User user, @Argument("group") Group group) {
        user.setGroup(group, true);
        sender.sendMessage(Message.CMD_USER_UPDATED.admin());
    }

    @Command("easyprefix|ep user <user> setsubgroup <subgroup>")
    @CommandDescription("sets a subgroup/tag to a player")
    @Permission("easyprefix.admin")
    public void userSetsubgroup(CommandSender sender, @Argument("user") User user, @Argument("subgroup") Subgroup subgroup) {
        user.setSubgroup(subgroup);
        sender.sendMessage(Message.CMD_USER_UPDATED.admin());
    }

    @Command("easyprefix|ep user <user> settag <subgroup>")
    @CommandDescription("sets a tag to a player")
    @Permission("easyprefix.admin")
    public void userSetsubgroup2(CommandSender sender, @Argument("user") User user, @Argument("subgroup") Subgroup subgroup) {
        userSetsubgroup(sender, user, subgroup);
    }

    @Command("easyprefix|ep user <user> setprefix <prefix>")
    @CommandDescription("sets a users prefix. you also might use quotes at the beginning and end of the suffix")
    @Permission("easyprefix.admin")
    public void userSetPrefix(CommandSender sender, @Argument("user") User user, @Argument("prefix") @Greedy String prefix) {
        if (prefix.startsWith("\"") && prefix.endsWith("\"")) {
            prefix = prefix.substring(1, prefix.length() - 1);
        }
        user.setPrefix(prefix);
        sender.sendMessage(Message.CMD_USER_PREFIX_SET.admin("player", user.getName(), "content", orNone(user.getPrefix())));
    }

    @Command("easyprefix|ep user <user> setsuffix <suffix>")
    @CommandDescription("sets a users suffix. you also might use quotes at the beginning and end of the suffix")
    @Permission("easyprefix.admin")
    public void userSetSuffix(CommandSender sender, @Argument("user") User user, @Argument("suffix") @Greedy String suffix) {
        if (suffix.startsWith("\"") && suffix.endsWith("\"")) {
            suffix = suffix.substring(1, suffix.length() - 1);
        }
        user.setSuffix(suffix);
        sender.sendMessage(Message.CMD_USER_SUFFIX_SET.admin("player", user.getName(), "content", orNone(user.getSuffix())));
    }

    @Command("easyprefix|ep user <user> setcolor <color>")
    @CommandDescription("sets the chat color of a player, also if they are offline")
    @Permission("easyprefix.admin")
    public void userSetColor(CommandSender sender, @Argument("user") User user, @Argument("color") Color color) {
        CommandColor.setColor(sender, user, color);
    }

    @Command("easyprefix|ep user <user> setformat <formatting>")
    @CommandDescription("sets the chat formatting (e.g. bold) of a player, also if they are offline")
    @Permission("easyprefix.admin")
    public void userSetFormatting(CommandSender sender, @Argument("user") User user, @Argument("formatting") Decoration decoration) {
        CommandColor.setFormatting(sender, user, decoration);
    }

    @Command("easyprefix|ep user <user> setformat none")
    @CommandDescription("removes the chat formatting of a player")
    @Permission("easyprefix.admin")
    public void userRemoveFormatting(CommandSender sender, @Argument("user") User user) {
        CommandColor.setFormatting(sender, user, null);
    }

    @Command("easyprefix|ep user <user> resetcolor")
    @CommandDescription("resets the chat color and formatting of a player to the default of their group")
    @Permission("easyprefix.admin")
    public void userResetColor(CommandSender sender, @Argument("user") User user) {
        CommandColor.resetColor(sender, user);
    }

    @Command("easyprefix|ep settings")
    @CommandDescription("opens the graphical user interface which allows you to make settings")
    @Permission("easyprefix.settings")
    public void openSettings(Player player) {
        UserInterface gui = new UserInterface(EasyPrefix.getInstance().getUser(player));
        TaskManager.run(player, gui::openUserSettings);
    }

    @Command("easyprefix|ep mentions")
    @CommandDescription("turns on or off whether @mentions of your name ping you")
    @Permission("easyprefix.settings")
    public void toggleMentions(Player player) {
        if (!getInstance().getConfigData().getBoolean(ConfigData.Keys.MENTIONS)) {
            player.sendMessage(Message.MENTIONS_DISABLED_GLOBALLY.getText());
            return;
        }
        User user = getInstance().getUser(player);
        user.setMentionable(!user.isMentionable());
        player.sendMessage((user.isMentionable() ? Message.MENTIONS_ENABLED : Message.MENTIONS_DISABLED).getText());
    }

    @Command("easyprefix|ep setup")
    @CommandDescription("opens the graphical user interface which allows you to setup the plugin")
    @Permission("easyprefix.admin")
    public void openSetup(Player player) {
        UserInterface gui = new UserInterface(EasyPrefix.getInstance().getUser(player));
        TaskManager.run(player, gui::openPageSetup);
    }

    @Command("easyprefix|ep reload")
    @CommandDescription("reloads the plugin (not recommended, please stop and start the server)")
    @Permission("easyprefix.admin")
    public void reload(CommandSender sender) {
        TaskManager.global(getInstance()::reload);
        sender.sendMessage(Message.CMD_RELOADED.admin());
    }

    @Command("easyprefix|ep debug")
    @CommandDescription("shows useful debug information")
    @Permission("easyprefix.admin")
    public void showDebugInfo(CommandSender sender) {
        // not in messages.yml on purpose: the output is meant for bug reports
        GroupHandler groupHandler = getInstance().getGroupHandler();
        sender.sendMessage(" \n§7------------=== §9§lEasyPrefix DEBUG §7===------------");
        sender.sendMessage("§9Version: §7" + VersionController.getPluginVersion());
        sender.sendMessage(String.format("§9Groups/Subgroups: §7%s/%s", groupHandler.getGroups().size(), groupHandler.getSubgroups().size()));
        sender.sendMessage("§9Users cached: §7" + getInstance().getUsers().size());
        sender.sendMessage("§9installed colors: §7" + getInstance().getColors().size());
        sender.sendMessage("§9Bukkit Version: §7" + Bukkit.getVersion());
        sender.sendMessage("§9Java Version: §7" + System.getProperty("java.version"));
        sender.sendMessage("§9Version Name: §7" + Bukkit.getBukkitVersion());
        sender.sendMessage("§9Storage: §7" + ((getInstance().getStorageType() == StorageType.SQL) ? "MySQL" : "local"));
        sender.sendMessage("§9active EventHandler: §7" + HandlerList.getRegisteredListeners(getInstance().getPlugin()).size());
    }

    @Command("easyprefix|ep debug stop")
    @Permission("easyprefix.admin")
    public void stopPlugin(CommandSender sender) {
        sender.sendMessage(Message.PREFIX + "disabling plugin...");
        TaskManager.global(() -> Bukkit.getPluginManager().disablePlugin(EasyPrefix.getInstance()));
    }

    @Command("easyprefix|ep database migrate")
    @CommandDescription("downloads groups, tags and users from MySQL into the local storage (groups.yml and storage.db)")
    @Permission("easyprefix.admin")
    public void databaseMigration(CommandSender sender) {
        if (getInstance().getStorageType().equals(StorageType.SQL)) {
            sender.sendMessage(Message.CMD_DOWNLOAD_START.admin());
            TaskManager.async(() -> {
                long timestamp = System.currentTimeMillis();
                try {
                    new Migration().download();
                } catch (RuntimeException ex) {
                    Debug.handleException(ex);
                    sender.sendMessage(Message.CMD_DOWNLOAD_FAILED.admin());
                    return;
                }
                sender.sendMessage(Message.CMD_DOWNLOAD_DONE.admin("seconds", Double.toString((System.currentTimeMillis() - timestamp) / 1000.0)));
                sender.sendMessage(Message.CMD_DOWNLOAD_HINT.admin());
            });
        } else {
            sender.sendMessage(Message.CMD_DOWNLOAD_REQUIRES_SQL.admin());
        }
    }

    @Command("easyprefix|ep database upload")
    @CommandDescription("uploads groups, tags and users of the local storage (groups.yml and storage.db) to MySQL")
    @Permission("easyprefix.admin")
    public void databaseUpload(CommandSender sender) {
        if (getInstance().getStorageType() != StorageType.SQL) {
            sender.sendMessage(Message.CMD_UPLOAD_REQUIRES_SQL.admin());
            return;
        }
        sender.sendMessage(Message.CMD_UPLOAD_START.admin());
        TaskManager.async(() -> {
            Migration.UploadResult result;
            try {
                result = new Migration().upload();
            } catch (RuntimeException ex) {
                Debug.handleException(ex);
                sender.sendMessage(Message.CMD_UPLOAD_FAILED.admin());
                return;
            }
            getInstance().getGroupHandler().load();
            getInstance().reloadUsers();
            getInstance().getDisplayManager().updateAll();
            sender.sendMessage(Message.CMD_UPLOAD_DONE.admin("groups", Integer.toString(result.groups()),
                    "tags", Integer.toString(result.tags()), "users", Integer.toString(result.users())));
        });
    }

}
