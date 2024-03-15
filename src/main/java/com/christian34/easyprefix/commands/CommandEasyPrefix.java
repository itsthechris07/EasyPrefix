package com.christian34.easyprefix.commands;

import cloud.commandframework.CommandHelpHandler;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.processing.CommandContainer;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.context.CommandContext;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.sql.database.Migration;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */

@CommandContainer
public class CommandEasyPrefix {

    private EasyPrefix getInstance() {
        return EasyPrefix.getInstance();
    }

    private BukkitCommandManager<CommandSender> getBukkitCommandManager() {
        return getInstance().getCommandManager().getManager();
    }

    @CommandMethod("easyprefix|ep")
    @CommandDescription("main command")
    public void mainCmd(CommandSender sender) {
        sender.sendMessage(Message.PREFIX + String.format("§7This server uses §9EasyPrefix §7version §b%s §7by Christian34.\nType '/easyprefix help' to get a command overview.", getInstance().getDescription().getVersion()));
    }

    @Suggestions("help_queries")
    public @NotNull List<String> suggestHelpQueries(@NotNull CommandContext<CommandSender> ctx, @NotNull String input) {
        return getBukkitCommandManager().createCommandHelpHandler().queryRootIndex(ctx.getSender()).getEntries().stream()
                .map(CommandHelpHandler.VerboseHelpEntry::getSyntaxString)
                .collect(Collectors.toList());
    }

    @CommandMethod("easyprefix|ep help [query]")
    @CommandDescription("Help menu")
    public void commandHelp(CommandSender sender, @Argument(value = "query", suggestions = "help_queries") @Greedy String query) {
        getInstance().getCommandManager().getMinecraftHelp().queryCommands(query == null ? "" : query, sender);
    }

    @CommandMethod("easyprefix|ep group <group> info")
    @CommandDescription("configure groups")
    @CommandPermission("easyprefix.admin")
    public void groupInfo(CommandSender sender, @Argument("group") Group group) {
        sender.sendMessage(String.format(" \n§7--------------=== §9§l%s §7===--------------\n ", group.getName()));
        sender.sendMessage(String.format("§9Prefix§f: §8«§7%s§8»", group.getPrefix()));
        sender.sendMessage(String.format("§9Suffix§f: §8«§7%s§8»", group.getSuffix()));
        sender.sendMessage("§9Chat color§f: §7" + group.getColor().getDisplayName());
        sender.sendMessage("§9Join message§f: §7" + group.getJoinMessage());
        sender.sendMessage("§9Quit message§f: §7" + group.getQuitMessage());
        sender.sendMessage(" \n§7-----------------------------------------------\n ");
    }

    @CommandMethod("easyprefix|ep group <group> setprefix <prefix>")
    @CommandDescription("set a groups prefix. you also might use quotes at the beginning and end of the prefix")
    @CommandPermission("easyprefix.admin")
    public void groupSetPrefix(CommandSender sender, @Argument("group") Group group, @Argument("prefix") @Greedy() String prefix) {
        if (prefix.startsWith("\"") && prefix.endsWith("\"")) {
            prefix = prefix.substring(1, prefix.length() - 1);
        }
        group.setPrefix(prefix);
        sender.sendMessage(String.format("§aThe prefix of group §7%s §ahas been set to §7\"%s\"§a.", group.getName(), group.getPrefix()));
    }

    @CommandMethod("easyprefix|ep group <group> setsuffix <suffix>")
    @CommandDescription("set a groups suffix. you also might use quotes at the beginning and end of the suffix")
    @CommandPermission("easyprefix.admin")
    public void groupSetSuffix(CommandSender sender, @Argument("group") Group group, @Argument("suffix") @Greedy() String suffix) {
        if (suffix.startsWith("\"") && suffix.endsWith("\"")) {
            suffix = suffix.substring(1, suffix.length() - 1);
        }
        group.setSuffix(suffix);
        sender.sendMessage(String.format("§aThe suffix of group §7%s §ahas been set to §7\"%s\"§a.", group.getName(), group.getSuffix()));
    }

    @CommandMethod("easyprefix|ep user <user> info")
    @CommandDescription("shows user information and applied settings")
    @CommandPermission("easyprefix.admin")
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
        sender.sendMessage("§9current color§f: §7" + user.getColor().getDisplayName());
        sender.sendMessage("§9colors§f: §7" + user.getColors().size());
        if (user.getDecoration() != null) {
            sender.sendMessage(" §9chat formatting§f: §7" + user.getDecoration().getDisplayName());
        }
        sender.sendMessage(" \n§7-----------------------------------------------\n ");
    }

    @CommandMethod("easyprefix|ep user <user> setgroup <group>")
    @CommandDescription("sets a group to a player")
    @CommandPermission("easyprefix.admin")
    public void userSetgroup(CommandSender sender, @Argument("user") User user, @Argument("group") Group group) {
        user.setGroup(group, true);
        sender.sendMessage(Message.PREFIX + "User has been updated!");
    }

    @CommandMethod("easyprefix|ep user <user> setsubgroup <subgroup>")
    @CommandDescription("sets a subgroup/tag to a player")
    @CommandPermission("easyprefix.admin")
    public void userSetsubgroup(CommandSender sender, @Argument("user") User user, @Argument("subgroup") Subgroup subgroup) {
        user.setSubgroup(subgroup);
        sender.sendMessage(Message.PREFIX + "User has been updated!");
    }

    @CommandMethod("easyprefix|ep user <user> settag <subgroup>")
    @CommandDescription("sets a tag to a player")
    @CommandPermission("easyprefix.admin")
    public void userSetsubgroup2(CommandSender sender, @Argument("user") User user, @Argument("subgroup") Subgroup subgroup) {
        userSetsubgroup(sender, user, subgroup);
        sender.sendMessage(Message.PREFIX + "User has been updated!");
    }

    @CommandMethod("easyprefix|ep user <user> setprefix <prefix>")
    @CommandDescription("sets a users prefix. you also might use quotes at the beginning and end of the suffix")
    @CommandPermission("easyprefix.admin")
    public void userSetPrefix(CommandSender sender, @Argument("user") User user, @Argument("prefix") @Greedy String prefix) {
        if (prefix.startsWith("\"") && prefix.endsWith("\"")) {
            prefix = prefix.substring(1, prefix.length() - 1);
        }
        user.setPrefix(prefix);
        sender.sendMessage(String.format(Message.PREFIX + "§cThe prefix of §7%s §chas been set to §7%s§c.", user.getName(), user.getPrefix()));
    }

    @CommandMethod("easyprefix|ep user <user> setsuffix <suffix>")
    @CommandDescription("sets a users suffix. you also might use quotes at the beginning and end of the suffix")
    @CommandPermission("easyprefix.admin")
    public void userSetSuffix(CommandSender sender, @Argument("user") User user, @Argument("suffix") @Greedy String suffix) {
        if (suffix.startsWith("\"") && suffix.endsWith("\"")) {
            suffix = suffix.substring(1, suffix.length() - 1);
        }
        user.setSuffix(suffix);
        sender.sendMessage(String.format(Message.PREFIX + "§cThe suffix of §7%s §chas been set to §7%s§c.", user.getName(), user.getSuffix()));
    }

    @CommandMethod("easyprefix|ep settings")
    @CommandDescription("opens the graphical user interface which allows you to make settings")
    @CommandPermission("easyprefix.settings")
    public void openSettings(Player player) {
        UserInterface gui = new UserInterface(EasyPrefix.getInstance().getUser(player));
        TaskManager.run(gui::openUserSettings);
    }

    @CommandMethod("easyprefix|ep setup")
    @CommandDescription("opens the graphical user interface which allows you to setup the plugin")
    @CommandPermission("easyprefix.admin")
    public void openSetup(Player player) {
        UserInterface gui = new UserInterface(EasyPrefix.getInstance().getUser(player));
        TaskManager.run(gui::openPageSetup);
    }

    @CommandMethod("easyprefix|ep reload")
    @CommandDescription("reloads the plugin (not recommended, please stop and start the server)")
    @CommandPermission("easyprefix.admin")
    public void reload(CommandSender sender) {
        TaskManager.run(getInstance()::reload);
        sender.sendMessage(Message.PREFIX + "§aPlugin has been reloaded!");
    }

    @CommandMethod("easyprefix|ep debug")
    @CommandDescription("shows useful debug information")
    @CommandPermission("easyprefix.admin")
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
        sender.sendMessage("§9Client ID: §7" + getInstance().getConfigData().getString(ConfigData.Keys.CLIENT_ID));
    }

    @CommandMethod("easyprefix|ep debug stop")
    @CommandPermission("easyprefix.admin")
    public void stopPlugin(CommandSender sender) {
        sender.sendMessage(Message.PREFIX + "disabling plugin...");
        TaskManager.run(() -> Bukkit.getPluginManager().disablePlugin(EasyPrefix.getInstance()));
    }

    @CommandMethod("easyprefix|ep database migrate")
    @CommandPermission("easyprefix.admin")
    public void databaseMigration(CommandSender sender) {
        if (getInstance().getStorageType().equals(StorageType.SQL)) {
            sender.sendMessage(Message.PREFIX + "Downloading data from MySQL to Files...");
            TaskManager.async(() -> {
                long timestamp = System.currentTimeMillis();
                Migration migration = new Migration();
                migration.download();
                sender.sendMessage(Message.PREFIX + String.format("Migration has been completed! (took %s seconds)", ((double) (System.currentTimeMillis() - timestamp) / 1000)));
            });
        } else {
            //todo uploading
            sender.sendMessage(Message.PREFIX + "Please enable sql in 'config.yml'!");
        }
    }

    @CommandMethod("test")
    public void test(Player player) {
        final Component component = LegacyComponentSerializer.legacyAmpersand().deserialize("&6Hello &b&lworld&c!");
        player.sendMessage(TextUtils.serialize(component));
    }

}
