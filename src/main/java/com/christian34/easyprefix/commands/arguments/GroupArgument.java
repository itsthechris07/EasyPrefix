package com.christian34.easyprefix.commands.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.utils.Message;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public final class GroupArgument<CommandSender> extends CommandArgument<CommandSender, Group> {

    public GroupArgument() {
        super(true, "group", new GroupParser<>(), Group.class);
    }

    public static final class GroupParser<CommandSender> implements ArgumentParser<CommandSender, Group> {

        private static EasyPrefix getInstance() {
            return EasyPrefix.getInstance();
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull Group> parse(@NonNull CommandContext<@NonNull CommandSender> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(GroupArgument.class, commandContext));
            }

            if (getInstance().getGroupHandler().isGroup(input)) {
                Group group = getInstance().getGroupHandler().getGroup(input);
                inputQueue.remove();
                return ArgumentParseResult.success(group);
            }
            return ArgumentParseResult.failure(new IllegalArgumentException(Message.CHAT_GROUP_NOT_FOUND.getText()));
        }

        @Override
        public @NotNull List<String> suggestions(final @NotNull CommandContext<CommandSender> commandContext, final @NotNull String input) {
            return getInstance().getGroupHandler().getGroups().stream().map(Group::getName).collect(Collectors.toList());
        }

    }

}
