package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.sql.database.StorageType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class DatabaseCommand implements Subcommand {
    private final CommandHandler commandHandler;
    private final EasyPrefix instance;

    public DatabaseCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
        this.instance = commandHandler.getInstance();
    }

    @Override
    public String getName() {
        return "database";
    }

    @Override
    public String getPermission() {
        return "admin";
    }

    @Override
    public void handleCommand(CommandSender sender, List<String> args) {
        if (this.instance.getStorageType() == StorageType.LOCAL) {
            commandHandler.getSubcommand("help").handleCommand(sender, null);
            return;
        }

        if (args.size() == 2) {
            if (args.get(1).equalsIgnoreCase("upload")) {
                sender.sendMessage(Messages.getPrefix() + "§7Uploading data to database. This could take a while...");
                instance.getDataMigration().upload();
            }
        } else {
            sender.sendMessage(" \n§7---------------=== §5§lEasyPrefix §7===---------------\n ");
            sender.sendMessage("§7/§5EasyPrefix database upload §f| §7upload local data to mysql, this could take a while... (necessary for bungeecord)");
            sender.sendMessage(" \n§7------------------------------------------------\n ");
        }

    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, List<String> args) {
        if (args.size() == 2) {
            List<String> matches = Collections.singletonList("upload");
            if (args.get(1).isEmpty()) {
                return matches;
            } else {
                List<String> complete = new ArrayList<>();
                for (String sub : matches) {
                    if (sub.startsWith(args.get(1))) {
                        complete.add(sub);
                    }
                }
                return complete;
            }
        }
        return Collections.emptyList();
    }

}
