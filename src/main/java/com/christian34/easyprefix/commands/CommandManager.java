package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.commands.arguments.ColorArgument;
import com.christian34.easyprefix.commands.arguments.GroupArgument;
import com.christian34.easyprefix.commands.arguments.SubgroupArgument;
import com.christian34.easyprefix.commands.arguments.UserArgument;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TaskManager;
import com.christian34.easyprefix.utils.UserInterface;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.suggestion.FilteringSuggestionProcessor;

import static org.incendo.cloud.description.CommandDescription.commandDescription;

/**
 * EasyPrefix 2026.
 * <p>
 * Registers all commands with cloud, including the argument parsers, the help menu, the error messages and the
 * /prefix and /suffix aliases.
 *
 * @author Christian34
 */
public class CommandManager {
    private final EasyPrefix instance;
    private final org.incendo.cloud.CommandManager<CommandSender> manager;
    private final AnnotationParser<CommandSender> annotationParser;
    private final MinecraftHelp<CommandSender> minecraftHelp;

    public CommandManager(EasyPrefix instance) {
        this(instance, createPaperManager(instance));
    }

    /**
     * registers all commands at the given cloud manager (the tests use one without a server behind it)
     */
    public CommandManager(EasyPrefix instance, org.incendo.cloud.CommandManager<CommandSender> manager) {
        this.instance = instance;
        this.manager = manager;

        this.manager.suggestionProcessor(new FilteringSuggestionProcessor<>(
                FilteringSuggestionProcessor.Filter.contains(true)
        ));
        this.annotationParser = new AnnotationParser<>(this.manager, CommandSender.class);

        this.minecraftHelp = MinecraftHelp.<CommandSender>builder()
                .commandManager(this.manager)
                .audienceProvider(sender -> sender)
                .commandPrefix("/easyprefix help")
                .colors(MinecraftHelp.helpColors(NamedTextColor.GRAY, NamedTextColor.AQUA, NamedTextColor.GRAY, NamedTextColor.GRAY, NamedTextColor.WHITE))
                .maxResultsPerPage(10)
                .messages(MinecraftHelp.MESSAGE_HELP_TITLE, Message.CHAT_CMD_HELP_HEAD.getText())
                .messages(MinecraftHelp.MESSAGE_DESCRIPTION, Message.CHAT_CMD_DESCRIPTION.getText())
                .messages(MinecraftHelp.MESSAGE_SHOWING_RESULTS_FOR_QUERY, Message.CHAT_CMD_HELP_QUERY.getText())
                .messages(MinecraftHelp.MESSAGE_CLICK_FOR_NEXT_PAGE, Message.CHAT_CMD_HELP_NEXT.getText())
                .messages(MinecraftHelp.MESSAGE_CLICK_FOR_PREVIOUS_PAGE, Message.CHAT_CMD_HELP_PREVIOUS.getText())
                .messages(MinecraftHelp.MESSAGE_AVAILABLE_COMMANDS, Message.CHAT_CMD_HELP_AVAILABLE.getText())
                .build();

        MinecraftExceptionHandler.<CommandSender>create(sender -> sender)
                .handler(InvalidSyntaxException.class, (formatter, ctx) -> {
                    ClickEvent clickEvent = ClickEvent.runCommand("/ep help");
                    Component componentCmd = Component.text("\"/ep help\"").clickEvent(clickEvent).color(NamedTextColor.GRAY);
                    return Component.text(Message.PREFIX + "§cInvalid syntax. Type ")
                            .append(componentCmd).append(Component.text(" for more help.").color(NamedTextColor.RED))
                            .append(Component.text(
                                    String.format("\nCorrect command syntax: /%s", ctx.exception().correctSyntax()),
                                    NamedTextColor.GRAY
                            ));
                })
                .handler(InvalidCommandSenderException.class, (formatter, ctx) -> {
                    if (ctx.exception().requiredSenderTypes().contains(Player.class)) {
                        return Component.text(Message.PREFIX + "§cYou cannot use this command in the console!");
                    } else {
                        return Component.text(Message.PREFIX + "§cInvalid command sender. You must be of type " + ctx.exception().requiredSenderTypes());
                    }
                })
                .handler(NoPermissionException.class, (formatter, ctx) -> Component.text(Message.CHAT_NO_PERMS.getText()))
                .handler(ArgumentParseException.class, (formatter, ctx) -> Component.text(ctx.exception().getCause().getMessage()))
                .defaultCommandExecutionHandler()
                .registerTo(this.manager);
        constructCommands();
    }

    private static LegacyPaperCommandManager<CommandSender> createPaperManager(EasyPrefix instance) {
        LegacyPaperCommandManager<CommandSender> manager;
        try {
            manager = LegacyPaperCommandManager.createNative(instance, ExecutionCoordinator.asyncCoordinator());
        } catch (final Exception ex) {
            throw new IllegalStateException("Couldn't initialize commands", ex);
        }

        // no brigadier registration on purpose: brigadier rejects incomplete/invalid input itself with vanilla
        // error messages and does not know the custom argument types, so cloud handles all input instead
        if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
        return manager;
    }

    public org.incendo.cloud.CommandManager<CommandSender> getManager() {
        return manager;
    }

    public MinecraftHelp<CommandSender> getMinecraftHelp() {
        return minecraftHelp;
    }

    private void constructCommands() {
        GroupArgument.GroupParser<CommandSender> groupParser = new GroupArgument.GroupParser<>();
        this.manager.parserRegistry().registerParserSupplier(TypeToken.get(Group.class), p -> groupParser);

        SubgroupArgument.SubgroupParser<CommandSender> subgroupParser = new SubgroupArgument.SubgroupParser<>();
        this.manager.parserRegistry().registerParserSupplier(TypeToken.get(Subgroup.class), p -> subgroupParser);

        UserArgument.UserParser<CommandSender> userParser = new UserArgument.UserParser<>();
        this.manager.parserRegistry().registerParserSupplier(TypeToken.get(User.class), p -> userParser);

        ColorArgument.ColorParser<CommandSender> colorParser = new ColorArgument.ColorParser<>();
        this.manager.parserRegistry().registerParserSupplier(TypeToken.get(Color.class), p -> colorParser);

        this.annotationParser.parse(new CommandColor());
        if (instance.getConfigData().getBoolean(ConfigData.Keys.USE_TAGS)) {
            this.annotationParser.parse(new CommandTags());
        }
        this.annotationParser.parse(new CommandEasyPrefix());

        ConfigData config = instance.getConfigData();
        String prefixAlias = config.getString(ConfigData.Keys.PREFIX_ALIAS, "")
                .replace("/", "");
        if (!prefixAlias.isBlank()) {
            this.manager.command(this.manager.commandBuilder(prefixAlias)
                    .commandDescription(commandDescription("modifies your prefix"))
                    .senderType(Player.class)
                    .permission(UserPermission.CUSTOM_PREFIX.toString())
                    .handler(ctx -> {
                        User user = this.instance.getUser(ctx.sender());
                        UserInterface gui = new UserInterface(user);
                        TaskManager.run(ctx.sender(), gui::showCustomPrefixGui);
                    })
            );
        }

        String suffixAlias = config.getString(ConfigData.Keys.SUFFIX_ALIAS, "")
                .replace("/", "");
        if (!suffixAlias.isBlank()) {
            this.manager.command(this.manager.commandBuilder(suffixAlias)
                    .commandDescription(commandDescription("modifies your suffix"))
                    .senderType(Player.class)
                    .permission(UserPermission.CUSTOM_SUFFIX.toString())
                    .handler(ctx -> {
                        User user = this.instance.getUser(ctx.sender());
                        UserInterface gui = new UserInterface(user);
                        TaskManager.run(ctx.sender(), gui::showCustomSuffixGui);
                    })
            );
        }
    }

}
