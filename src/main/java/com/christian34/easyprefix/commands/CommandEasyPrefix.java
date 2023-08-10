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
    public void main(CommandSender sender) {
        sender.sendMessage(Message.PREFIX + String.format("§7This server uses §9EasyPrefix §7version §b%s §7by Christian34.\nType '/easyprefix help' to get a command overview.", getInstance().getDescription().getVersion()));
    }

    @Suggestions("help_queries")
    public @NotNull List<String> suggestHelpQueries(@NotNull CommandContext<CommandSender> ctx, @NotNull String input) {
        return getBukkitCommandManager().createCommandHelpHandler().queryRootIndex(ctx.getSender()).getEntries().stream()
                .map(CommandHelpHandler.VerboseHelpEntry::getSyntaxString)
                .collect(Collectors.toList());
    }

    @CommandMethod("easyprefix help [query]")
    @CommandDescription("Help menu")
    public void commandHelp(CommandSender sender, @Argument(value = "query", suggestions = "help_queries") @Greedy String query) {
        getInstance().getCommandManager().getMinecraftHelp().queryCommands(query == null ? "" : query, sender);
    }

    @CommandMethod("easyprefix group <group> info")
    @CommandDescription("configure groups")
    @CommandPermission("easyprefix.admin")
    public void groupInfo(CommandSender sender, @Argument("group") Group group) {
        sender.sendMessage(String.format(" \n§7--------------=== §9§l%s §7===--------------\n ", group.getName()));
        sender.sendMessage(String.format("§9Prefix§f: §8«§7%s§8»", group.getPrefix()));
        sender.sendMessage(String.format("§9Suffix§f: §8«§7%s§8»", group.getSuffix()));
        String cc = group.getChatColor().getCode();
        if (group.getChatFormatting() != null) cc += group.getChatFormatting().getCode();
        sender.sendMessage("§9Chat color§f: §7" + cc.replace("§", "&"));
        sender.sendMessage("§9Join message§f: §7" + group.getJoinMessage());
        sender.sendMessage("§9Quit message§f: §7" + group.getQuitMessage());
        sender.sendMessage(" \n§7-----------------------------------------------\n ");
    }

    @CommandMethod("easyprefix user <user> info")
    @CommandDescription("shows user information and applied settings")
    @CommandPermission("easyprefix.admin")
    public void userInfo(CommandSender sender, @Argument("user") User user) {
        String subgroup = (user.getSubgroup() != null) ? user.getSubgroup().getName() : "-";
        String chatColor = user.getChatColor().getCode();
        ChatFormatting chatFormatting = user.getChatFormatting();
        String formatting = "";
        if (chatFormatting != null) {
            formatting = chatFormatting.getCode();
        }

        sender.sendMessage(String.format(" \n§7--------------=== §9§l%s §7===--------------", user.getName()));
        sender.sendMessage("§9Group§f: §7" + user.getGroup().getName());
        sender.sendMessage("§9Tag§f: §7" + subgroup);
        sender.sendMessage("§9Prefix§f: §8«§7" + Optional.ofNullable(user.getPrefix()).orElse("-") + "§8»"
                + (user.hasCustomPrefix() ? " §7(§9customized§7)"
                + "\n  §7↳ §9last update§f: §7" + new Timestamp(user.getLastPrefixUpdate()) : ""));
        sender.sendMessage("§9Suffix§f: §8«§7" + Optional.ofNullable(user.getSuffix()).orElse("-") + "§8»"
                + (user.hasCustomSuffix() ? " §7(§9customized§7)"
                + "\n  §7↳ §9last update§f: §7" + new Timestamp(user.getLastSuffixUpdate()) : ""));
        sender.sendMessage(" §9chat color§f: §7" + chatColor.replace("§", "&"));
        if (chatFormatting != null) {
            sender.sendMessage(" §9chat formatting§f: §7" + formatting.replace("§", "&"));
        }
        sender.sendMessage(" \n§7-----------------------------------------------\n ");
    }

    @CommandMethod("easyprefix user <user> setgroup <group>")
    @CommandDescription("sets a group to a player")
    @CommandPermission("easyprefix.admin")
    public void userSetgroup(CommandSender sender, @Argument("user") User user, @Argument("group") Group group) {
        user.setGroup(group, true);
        sender.sendMessage(Message.PREFIX + "User has been updated!");
    }

    @CommandMethod("easyprefix user <user> setsubgroup <subgroup>")
    @CommandDescription("sets a subgroup/tag to a player")
    @CommandPermission("easyprefix.admin")
    public void userSetsubgroup(CommandSender sender, @Argument("user") User user, @Argument("subgroup") Subgroup subgroup) {
        user.setSubgroup(subgroup);
        sender.sendMessage(Message.PREFIX + "User has been updated!");
    }

    @CommandMethod("easyprefix user <user> settag <subgroup>")
    @CommandDescription("sets a tag to a player")
    @CommandPermission("easyprefix.admin")
    public void userSetsubgroup2(CommandSender sender, @Argument("user") User user, @Argument("subgroup") Subgroup subgroup) {
        userSetsubgroup(sender, user, subgroup);
    }

    @CommandMethod("easyprefix user <user> unsetsubgroup")
    @CommandDescription("removes a subgroup/tag from a player")
    @CommandPermission("easyprefix.admin")
    public void userUnsetSubgroup(CommandSender sender, @Argument("user") User user) {
        user.setSubgroup(null);
        sender.sendMessage(Message.PREFIX + "User has been updated!");
    }

    @CommandMethod("easyprefix settings")
    @CommandDescription("opens the graphical user interface which allows you to make settings")
    @CommandPermission("easyprefix.settings")
    public void openSettings(Player player) {
        UserInterface gui = new UserInterface(EasyPrefix.getInstance().getUser(player));
        TaskManager.run(gui::openUserSettings);
    }

    @CommandMethod("easyprefix setup")
    @CommandDescription("opens the graphical user interface which allows you to setup the plugin")
    @CommandPermission("easyprefix.admin")
    public void openSetup(Player player) {
        UserInterface gui = new UserInterface(EasyPrefix.getInstance().getUser(player));
        TaskManager.run(gui::openPageSetup);
    }

    @CommandMethod("easyprefix reload")
    @CommandDescription("reloads the plugin (not recommended, please stop and start the server)")
    @CommandPermission("easyprefix.admin")
    public void reload(CommandSender sender) {
        TaskManager.run(getInstance()::reload);
        sender.sendMessage(Message.PREFIX + "§aPlugin has been reloaded!");
    }

    @CommandMethod("easyprefix debug")
    @CommandDescription("shows useful debug information")
    @CommandPermission("easyprefix.admin")
    public void showDebugInfo(CommandSender sender) {
        GroupHandler groupHandler = getInstance().getGroupHandler();
        sender.sendMessage(" \n§7------------=== §9§lEasyPrefix DEBUG §7===------------");
        sender.sendMessage("§9Version: §7" + VersionController.getPluginVersion());
        sender.sendMessage(String.format("§9Groups/Subgroups: §7%s/%s", groupHandler.getGroups().size(), groupHandler.getSubgroups().size()));
        sender.sendMessage("§9Users cached: §7" + getInstance().getUsers().size());
        sender.sendMessage("§9Bukkit Version: §7" + Bukkit.getVersion());
        sender.sendMessage("§9Java Version: §7" + System.getProperty("java.version"));
        sender.sendMessage("§9Version Name: §7" + Bukkit.getBukkitVersion());
        sender.sendMessage("§9Storage: §7" + ((getInstance().getStorageType() == StorageType.SQL) ? "MySQL" : "local"));
        sender.sendMessage("§9active EventHandler: §7" + HandlerList.getRegisteredListeners(getInstance().getPlugin()).size());
        sender.sendMessage("§9Client ID: §7" + getInstance().getConfigData().getString(ConfigData.Keys.CLIENT_ID));
    }

    @CommandMethod("easyprefix debug stop")
    @CommandPermission("easyprefix.admin")
    public void stopPlugin(CommandSender sender) {
        sender.sendMessage(Message.PREFIX + "disabling plugin...");
        TaskManager.run(() -> {
            Bukkit.getPluginManager().disablePlugin(EasyPrefix.getInstance());
        });
    }

    @CommandMethod("easyprefix database migrate")
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
        Chat.send(player, "Hello <rainbow>world</rainbow>, isn't <underlined>MiniMessage</underlined> fun?");

        Chat.send(player, "<green><bold>Hai");
    }

}
