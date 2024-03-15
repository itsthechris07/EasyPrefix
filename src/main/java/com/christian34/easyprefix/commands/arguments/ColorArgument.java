package com.christian34.easyprefix.commands.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Message;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class ColorArgument<CommandSender> extends CommandArgument<CommandSender, Color> {

    public ColorArgument() {
        super(true, "color", new ColorParser<>(), Color.class);
    }

    public static final class ColorParser<CommandSender> implements ArgumentParser<CommandSender, Color> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull Color> parse(@NonNull CommandContext<@NonNull CommandSender> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
            if (inputQueue.peek() == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(ColorArgument.class, commandContext));
            }
            String input = StringUtils.deleteWhitespace(inputQueue.peek());

            Optional<Color> optional;
            if (commandContext.getSender() instanceof Player) {
                User user = EasyPrefix.getInstance().getUser((Player) commandContext.getSender());
                optional = user.getColors().stream().filter(color -> color.getName().equalsIgnoreCase(input)).findAny();
            } else {
                optional = EasyPrefix.getInstance().getColors().stream().filter(color -> color.getName().equalsIgnoreCase(input)).findAny();
            }
            if (optional.isPresent()) {
                inputQueue.remove();
                return ArgumentParseResult.success(optional.get());
            } else {
                return ArgumentParseResult.failure(new IllegalArgumentException(Message.COLOR_NOT_FOUND.get("color", input)));
            }
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<CommandSender> commandContext, @NonNull String input) {
            List<String> color;
            if (commandContext.getSender() instanceof Player) {
                User user = EasyPrefix.getInstance().getUser((Player) commandContext.getSender());
                color = user.getColors().stream().map(Color::getName).collect(Collectors.toList());
            } else {
                color = EasyPrefix.getInstance().getColors().stream().map(Color::getName).collect(Collectors.toList());
            }
            color.add("none");
            return color;
        }

    }

}
