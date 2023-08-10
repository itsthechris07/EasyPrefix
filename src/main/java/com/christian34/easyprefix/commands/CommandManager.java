package com.christian34.easyprefix.commands;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
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
import com.christian34.easyprefix.utils.*;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Function;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class CommandManager {
    private final EasyPrefix instance;
    private final BukkitCommandManager<CommandSender> manager;
    private final AnnotationParser<CommandSender> annotationParser;
    private final MinecraftHelp<CommandSender> minecraftHelp;
    private final BukkitAudiences bukkitAudiences;
    private CommandConfirmationManager<CommandSender> confirmationManager;

    public CommandManager(EasyPrefix instance) {
        this.instance = instance;
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build();
        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try {
            this.manager = new PaperCommandManager<>(instance, executionCoordinatorFunction, mapperFunction, mapperFunction);
        } catch (final Exception ignored) {
            throw new Error("Couldn't initialize commands...");
        }


        this.manager.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<CommandSender>contains(true).andTrimBeforeLastSpace()
        ));

        if (this.manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            this.manager.registerBrigadier();
        }
        if (this.manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            ((PaperCommandManager<CommandSender>) this.manager).registerAsynchronousCompletions();
        }
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                CommandMeta.simple().with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description")).build();
        this.annotationParser = new AnnotationParser<>(this.manager, CommandSender.class, commandMetaFunction);


        this.bukkitAudiences = Chat.getBukkitAudiences();

        this.minecraftHelp = new MinecraftHelp<>("/easyprefix help", bukkitAudiences::sender, this.manager);
        MinecraftHelp.HelpColors helpColors = MinecraftHelp.HelpColors.of(NamedTextColor.GRAY, NamedTextColor.AQUA, NamedTextColor.GRAY, NamedTextColor.GRAY, NamedTextColor.WHITE);
        this.minecraftHelp.setHelpColors(helpColors);
        this.minecraftHelp.setMaxResultsPerPage(10);
        this.minecraftHelp.setMessage(MinecraftHelp.MESSAGE_HELP_TITLE, Message.CHAT_CMD_HELP_HEAD.getText());
        this.minecraftHelp.setMessage(MinecraftHelp.MESSAGE_DESCRIPTION, Message.CHAT_CMD_DESCRIPTION.getText());
        this.minecraftHelp.setMessage(MinecraftHelp.MESSAGE_SHOWING_RESULTS_FOR_QUERY, Message.CHAT_CMD_HELP_QUERY.getText());
        this.minecraftHelp.setMessage(MinecraftHelp.MESSAGE_CLICK_FOR_NEXT_PAGE, Message.CHAT_CMD_HELP_NEXT.getText());
        this.minecraftHelp.setMessage(MinecraftHelp.MESSAGE_CLICK_FOR_PREVIOUS_PAGE, Message.CHAT_CMD_HELP_PREVIOUS.getText());
        this.minecraftHelp.setMessage(MinecraftHelp.MESSAGE_AVAILABLE_COMMANDS, Message.CHAT_CMD_HELP_AVAILABLE.getText());

        new MinecraftExceptionHandler<CommandSender>()
                .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SYNTAX, e -> {
                    ClickEvent clickEvent = ClickEvent.runCommand("/ep help");
                    Component componentCmd = Component.text("\"/ep help\"").clickEvent(clickEvent).color(NamedTextColor.GRAY);
                    return Component.text(Message.PREFIX + "§cInvalid syntax. Type ")
                            .append(componentCmd).append(Component.text(" for more help.").color(NamedTextColor.RED))
                            .append(Component.text(
                                    String.format("\nCorrect command syntax: /%s", ((InvalidSyntaxException) e).getCorrectSyntax()),
                                    NamedTextColor.GRAY
                            ));
                })
                .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SENDER, e -> {
                    String required = ((InvalidCommandSenderException) e).getRequiredSender().getSimpleName();
                    if (required.equals("Player")) {
                        return Component.text(Message.PREFIX + "§cYou cannot use this command in the console!");
                    } else {
                        return Component.text(Message.PREFIX + "§cInvalid command sender. You must be of type " + required);
                    }
                })
                .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION, exception -> Component.text(Message.CHAT_NO_PERMS.getText()))
                .withHandler(MinecraftExceptionHandler.ExceptionType.ARGUMENT_PARSING, exception -> Component.text(exception.getCause().getMessage()))
                .withCommandExecutionHandler()
                .apply(this.manager, bukkitAudiences::sender);
        constructCommands();
    }

    public BukkitCommandManager<CommandSender> getManager() {
        return manager;
    }

    public MinecraftHelp<CommandSender> getMinecraftHelp() {
        return minecraftHelp;
    }

    private void constructCommands() {
        GroupArgument.GroupParser<CommandSender> groupParser = new GroupArgument.GroupParser<>();
        this.manager.argumentBuilder(Group.class, "group").withParser(groupParser);
        this.manager.parserRegistry().registerParserSupplier(TypeToken.get(Group.class), p -> groupParser);

        SubgroupArgument.SubgroupParser<CommandSender> subgroupParser = new SubgroupArgument.SubgroupParser<>();
        this.manager.argumentBuilder(Subgroup.class, "subgroup").withParser(subgroupParser);
        this.manager.parserRegistry().registerParserSupplier(TypeToken.get(Subgroup.class), p -> subgroupParser);

        UserArgument.UserParser<CommandSender> userParser = new UserArgument.UserParser<>();
        this.manager.argumentBuilder(User.class, "user").withParser(userParser);
        this.manager.parserRegistry().registerParserSupplier(TypeToken.get(User.class), p -> userParser);

        if (instance.getConfigData().getBoolean(ConfigData.Keys.HANDLE_COLORS)) {
            ColorArgument.ColorParser<CommandSender> colorParser = new ColorArgument.ColorParser<>();
            this.manager.argumentBuilder(Color.class, "color").withParser(colorParser);
            this.manager.parserRegistry().registerParserSupplier(TypeToken.get(Color.class), p -> colorParser);

            this.annotationParser.parse(new CommandColor());
        }

        if (instance.getConfigData().getBoolean(ConfigData.Keys.USE_TAGS)) {
            this.annotationParser.parse(new CommandTags());
        }
        try {
            this.annotationParser.parseContainers();
        } catch (Exception e) {
            throw new Error();
        }

        ConfigData config = instance.getConfigData();
        String prefixAlias = config.getString(ConfigData.Keys.PREFIX_ALIAS, "")
                .replace("/", "");
        if (!prefixAlias.isBlank()) {
            this.manager.command(this.manager.commandBuilder(prefixAlias)
                    .meta(CommandMeta.DESCRIPTION, "modifies your prefix")
                    .senderType(Player.class)
                    .permission(UserPermission.CUSTOM_PREFIX.toString())
                    .handler(ctx -> {
                        User user = this.instance.getUser((Player) ctx.getSender());
                        UserInterface gui = new UserInterface(user);
                        TaskManager.run(gui::showCustomPrefixGui);
                    })
            );
        }

        String suffixAlias = config.getString(ConfigData.Keys.SUFFIX_ALIAS, "")
                .replace("/", "");
        if (!suffixAlias.isBlank()) {
            this.manager.command(this.manager.commandBuilder(suffixAlias)
                    .meta(CommandMeta.DESCRIPTION, "modifies your suffix")
                    .senderType(Player.class)
                    .permission(UserPermission.CUSTOM_SUFFIX.toString())
                    .handler(ctx -> {
                        User user = this.instance.getUser((Player) ctx.getSender());
                        UserInterface gui = new UserInterface(user);
                        TaskManager.run(gui::showCustomSuffixGui);
                    })
            );
        }
    }

}
