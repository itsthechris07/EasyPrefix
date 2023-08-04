package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.Subcommand;
import com.christian34.easyprefix.sql.database.Migration;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
class DatabaseCommand implements Subcommand {
    private final EasyPrefixCommand parentCommand;
    private final EasyPrefix instance;

    public DatabaseCommand(EasyPrefixCommand parentCommand) {
        this.parentCommand = parentCommand;
        this.instance = parentCommand.getInstance();
    }

    @Override
    @NotNull
    public String getName() {
        return "database";
    }

    @Override
    public UserPermission getPermission() {
        return UserPermission.ADMIN;
    }

    @Override
    @Nullable
    public String getDescription() {
        return "allows the configuration of the database";
    }

    @Override
    @NotNull
    public String getCommandUsage() {
        return "database (<argument>)";
    }

    @Override
    public void handleCommand(@NotNull CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return;
        }

        if (args.get(1).equalsIgnoreCase("migrate")) {
            if (this.instance.getStorageType().equals(StorageType.SQL)) {
                sender.sendMessage(Message.PREFIX + "Downloading data from MySQL to Files...");
                Bukkit.getScheduler().runTaskAsynchronously(this.instance, () -> {
                    long timestamp = System.currentTimeMillis();
                    Migration migration = new Migration();
                    migration.download();
                    sender.sendMessage(Message.PREFIX + String.format("Migration has been completed! (took %s seconds)", ((double) (System.currentTimeMillis() - timestamp) / 1000)));
                });
            } else {
                sender.sendMessage(Message.PREFIX + "Please enable sql in 'config.yml'!");
            }
        } else {
            parentCommand.getSubcommand("help").handleCommand(sender, null);
        }
    }

    @Override
    public @NotNull List<String> getTabCompletion(@NotNull CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

}
