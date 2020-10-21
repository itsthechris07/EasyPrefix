package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class AliasHandler implements CommandExecutor, TabCompleter {
    private final String prefixAlias, suffixAlias;
    private final CommandHandler commandHandler;
    private final EasyPrefix instance;

    public AliasHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
        this.instance = commandHandler.getInstance();

        this.prefixAlias = ConfigKeys.PREFIX_ALIAS.toString().replace("/", "");
        this.suffixAlias = ConfigKeys.SUFFIX_ALIAS.toString().replace("/", "");
        CommandMap commandMap = getCommandMapInstance();
        if (commandMap == null) return;


        PluginCommand prefixCmd = createPluginCommand(this.prefixAlias);
        if (prefixCmd != null) {
            prefixCmd.setExecutor(this);
            prefixCmd.setTabCompleter(this);
            commandMap.register(instance.getDescription().getName(), prefixCmd);
        }

        PluginCommand suffixCmd = createPluginCommand(this.suffixAlias);
        if (suffixCmd != null) {
            suffixCmd.setExecutor(this);
            suffixCmd.setTabCompleter(this);
            commandMap.register(instance.getDescription().getName(), suffixCmd);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            commandHandler.getSubcommand("help").handleCommand(sender, null);
            return true;
        }

        List<String> value = new ArrayList<>(Arrays.asList(args));
        if (cmd.getName().equalsIgnoreCase(this.prefixAlias)) {
            value.add(0, "setprefix");
        } else if (cmd.getName().equalsIgnoreCase(this.suffixAlias)) {
            value.add(0, "setsuffix");
        }
        commandHandler.getSubcommand("set").handleCommand(sender, value);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reset");
        }
        return Collections.emptyList();
    }

    private PluginCommand createPluginCommand(String name) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, instance);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CommandMap getCommandMapInstance() {
        if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager spm = (SimplePluginManager) Bukkit.getPluginManager();
            try {
                Field field = FieldUtils.getDeclaredField(spm.getClass(), "commandMap", true);
                return (CommandMap) field.get(spm);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
