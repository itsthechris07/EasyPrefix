package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
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
import java.util.Optional;
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
        sender.sendMessage(Message.PREFIX + String.format("§7This server uses §9EasyPrefix §7version §b%s §7by Christian34.\nType '/easyprefix help' to get a command overview.", getInstance().getPlugin().getPluginMeta().getVersion()));
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
        sender.sendMessage(String.format(" \n§7--------------=== §9§l%s §7===--------------\n ", group.getName()));
        sender.sendMessage(String.format("§9Prefix§f: §8«§7%s§8»", group.getPrefix()));
        sender.sendMessage(String.format("§9Suffix§f: §8«§7%s§8»", group.getSuffix()));
        sender.sendMessage("§9Chat color§f: §7" + group.getColor().getDisplayName());
        sender.sendMessage("§9Priority§f: §7" + group.getPriority());
        sender.sendMessage("§9Join message§f: §7" + group.getJoinMessage());
        sender.sendMessage("§9Quit message§f: §7" + group.getQuitMessage());
        sender.sendMessage(" \n§7-----------------------------------------------\n ");
    }

    @Command("easyprefix|ep group <group> setprefix <prefix>")
    @CommandDescription("set a groups prefix. you also might use quotes at the beginning and end of the prefix")
    @Permission("easyprefix.admin")
    public void groupSetPrefix(CommandSender sender, @Argument("group") Group group, @Argument("prefix") @Greedy String prefix) {
        if (prefix.startsWith("\"") && prefix.endsWith("\"")) {
            prefix = prefix.substring(1, prefix.length() - 1);
        }
        group.setPrefix(prefix);
        sender.sendMessage(String.format("§aThe prefix of group §7%s §ahas been set to §7\"%s\"§a.", group.getName(), group.getPrefix()));
    }

    @Command("easyprefix|ep group <group> setsuffix <suffix>")
    @CommandDescription("set a groups suffix. you also might use quotes at the beginning and end of the suffix")
    @Permission("easyprefix.admin")
    public void groupSetSuffix(CommandSender sender, @Argument("group") Group group, @Argument("suffix") @Greedy String suffix) {
        if (suffix.startsWith("\"") && suffix.endsWith("\"")) {
            suffix = suffix.substring(1, suffix.length() - 1);
        }
        group.setSuffix(suffix);
        sender.sendMessage(String.format("§aThe suffix of group §7%s §ahas been set to §7\"%s\"§a.", group.getName(), group.getSuffix()));
    }

    @Command("easyprefix|ep group <group> setpriority <priority>")
    @CommandDescription("players with several groups get the one with the highest priority, the tab list is sorted by it")
    @Permission("easyprefix.admin")
    public void groupSetPriority(CommandSender sender, @Argument("group") Group group, @Argument("priority") int priority) {
        group.setPriority(priority);
        sender.sendMessage(String.format("§aThe priority of group §7%s §ahas been set to §7%d§a.", group.getName(), priority));
    }

    @Command("easyprefix|ep user <user> info")
    @CommandDescription("shows user information and applied settings")
    @Permission("easyprefix.admin")
    public void userInfo(CommandSender sender, @Argument("user") User user) {
        String subgroup = (user.getSubgroup() != null) ? user.getSubgroup().getName() : "-";

        sender.sendMessage(String.format(" \n§7--------------=== §9§l%s §7===--------------", user.getName()));
        sender.sendMessage("§9Group§f: §7" + user.getGroup().getName());
        sender.sendMessage("§9Tag§f: §7" + subgroup);
        sender.sendMessage("§9Prefix§f: §8«§7" + Optional.ofNullable(user.getPrefix()).orElse("-") + "§8»"
                + (user.hasCustomPrefix() ? " §7(§9customized§7)"
                + "\n  §7↳ §9last update§f: §7" + new Timestamp(user.getLastPrefixUpdate()) : ""));
        sender.sendMessage("§9Suffix§f: §8«§7" + Optional.ofNullable(user.getSuffix()).orElse("-") + "§8»"
                + (user.hasCustomSuffix() ? " §7(§9customized§7)"
                + "\n  §7↳ §9last update§f: §7" + new Timestamp(user.getLastSuffixUpdate()) : ""));
        sender.sendMessage("§9current color§f: §7" + ((user.getColor() != null) ? user.getColor().getDisplayName() : "-"));
        sender.sendMessage("§9colors§f: §7" + user.getColors().size());
        if (user.getDecoration() != null) {
            sender.sendMessage(" §9chat formatting§f: §7" + user.getDecoration().getDisplayName());
        }
        sender.sendMessage(" \n§7-----------------------------------------------\n ");
    }

    @Command("easyprefix|ep user <user> setgroup <group>")
    @CommandDescription("sets a group to a player")
    @Permission("easyprefix.admin")
    public void userSetgroup(CommandSender sender, @Argument("user") User user, @Argument("group") Group group) {
        user.setGroup(group, true);
        sender.sendMessage(Message.PREFIX + "User has been updated!");
    }

    @Command("easyprefix|ep user <user> setsubgroup <subgroup>")
    @CommandDescription("sets a subgroup/tag to a player")
    @Permission("easyprefix.admin")
    public void userSetsubgroup(CommandSender sender, @Argument("user") User user, @Argument("subgroup") Subgroup subgroup) {
        user.setSubgroup(subgroup);
        sender.sendMessage(Message.PREFIX + "User has been updated!");
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
        sender.sendMessage(String.format(Message.PREFIX + "§cThe prefix of §7%s §chas been set to §7%s§c.", user.getName(), user.getPrefix()));
    }

    @Command("easyprefix|ep user <user> setsuffix <suffix>")
    @CommandDescription("sets a users suffix. you also might use quotes at the beginning and end of the suffix")
    @Permission("easyprefix.admin")
    public void userSetSuffix(CommandSender sender, @Argument("user") User user, @Argument("suffix") @Greedy String suffix) {
        if (suffix.startsWith("\"") && suffix.endsWith("\"")) {
            suffix = suffix.substring(1, suffix.length() - 1);
        }
        user.setSuffix(suffix);
        sender.sendMessage(String.format(Message.PREFIX + "§cThe suffix of §7%s §chas been set to §7%s§c.", user.getName(), user.getSuffix()));
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
        sender.sendMessage(Message.PREFIX + "§aPlugin has been reloaded!");
    }

    @Command("easyprefix|ep debug")
    @CommandDescription("shows useful debug information")
    @Permission("easyprefix.admin")
    public void showDebugInfo(CommandSender sender) {
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
            sender.sendMessage(Message.PREFIX + "Downloading data from MySQL to Files...");
            TaskManager.async(() -> {
                long timestamp = System.currentTimeMillis();
                try {
                    new Migration().download();
                } catch (RuntimeException ex) {
                    Debug.handleException(ex);
                    sender.sendMessage(Message.PREFIX + "§cThe migration failed, please check the console! Your local files are in the backup folder.");
                    return;
                }
                sender.sendMessage(Message.PREFIX + String.format("§aMigration has been completed! (took %s seconds)", ((double) (System.currentTimeMillis() - timestamp) / 1000)));
                sender.sendMessage(Message.PREFIX + "§7To use the local storage, set 'sql.enabled' in config.yml to false and restart the server.");
            });
        } else {
            sender.sendMessage(Message.PREFIX + "§cThis downloads the data from MySQL to the local storage - please enable sql in 'config.yml' first!");
        }
    }

    @Command("easyprefix|ep database upload")
    @CommandDescription("uploads groups, tags and users of the local storage (groups.yml and storage.db) to MySQL")
    @Permission("easyprefix.admin")
    public void databaseUpload(CommandSender sender) {
        if (getInstance().getStorageType() != StorageType.SQL) {
            sender.sendMessage(Message.PREFIX + "§cThis uploads the local storage to MySQL - please enable sql in 'config.yml' and restart the server first!");
            return;
        }
        sender.sendMessage(Message.PREFIX + "Uploading groups.yml and storage.db to MySQL...");
        TaskManager.async(() -> {
            Migration.UploadResult result;
            try {
                result = new Migration().upload();
            } catch (RuntimeException ex) {
                Debug.handleException(ex);
                sender.sendMessage(Message.PREFIX + "§cThe upload failed, please check the console!");
                return;
            }
            getInstance().getGroupHandler().load();
            getInstance().reloadUsers();
            getInstance().getDisplayManager().updateAll();
            sender.sendMessage(Message.PREFIX + String.format("§aUploaded %d groups, %d tags and %d users!",
                    result.groups(), result.tags(), result.users()));
        });
    }

}
