package com.christian34.easyprefix.commands.arguments;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.utils.Message;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * EasyPrefix 2026.
 * <p>
 * Command argument for groups, suggests all group names.
 *
 * @author Christian34
 */
public final class GroupArgument {

    private GroupArgument() {
    }

    public static final class GroupParser<C> implements ArgumentParser<C, Group>, BlockingSuggestionProvider.Strings<C> {

        private static EasyPrefix getInstance() {
            return EasyPrefix.getInstance();
        }

        @Override
        public @NotNull ArgumentParseResult<Group> parse(@NotNull CommandContext<C> commandContext, @NotNull CommandInput commandInput) {
            final String input = commandInput.peekString();

            if (getInstance().getGroupHandler().isGroup(input)) {
                Group group = getInstance().getGroupHandler().getGroup(input);
                commandInput.readString();
                return ArgumentParseResult.success(group);
            }
            return ArgumentParseResult.failure(new IllegalArgumentException(Message.CHAT_GROUP_NOT_FOUND.getText()));
        }

        @Override
        public @NotNull Iterable<String> stringSuggestions(@NotNull CommandContext<C> commandContext, @NotNull CommandInput input) {
            return getInstance().getGroupHandler().getGroups().stream().map(Group::getName).collect(Collectors.toList());
        }

    }

}
