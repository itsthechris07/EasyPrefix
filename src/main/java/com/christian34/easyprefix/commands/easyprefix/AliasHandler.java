package com.christian34.easyprefix.commands.easyprefix;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.utils.Debug;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2021.
 *
 * @author Christian34
 */
public class AliasHandler implements CommandExecutor, TabCompleter {
    private final EasyPrefixCommand parentCommand;
    private final EasyPrefix instance;

    public AliasHandler(EasyPrefixCommand parentCommand) {
        this.parentCommand = parentCommand;
        this.instance = parentCommand.getInstance();

        ConfigData config = instance.getConfigData();
        String prefixAlias = config.getString(ConfigData.Keys.PREFIX_ALIAS, "")
                .replace("/", "");
        String suffixAlias = config.getString(ConfigData.Keys.SUFFIX_ALIAS, "")
                .replace("/", "");

        CommandMap commandMap = getCommandMapInstance();
        if (commandMap == null) {
            throw new CommandException("Couldn't find command map!");
        }

        for (String name : Arrays.asList(prefixAlias, suffixAlias)) {
            PluginCommand pluginCommand = createPluginCommand(name);
            if (pluginCommand != null) {
                pluginCommand.setExecutor(this);
                pluginCommand.setTabCompleter(this);
                commandMap.register(instance.getDescription().getName(), pluginCommand);
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            parentCommand.getSubcommand("help").handleCommand(sender, null);
        }
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
                Debug.handleException(e);
            }
        }
        return null;
    }

}
