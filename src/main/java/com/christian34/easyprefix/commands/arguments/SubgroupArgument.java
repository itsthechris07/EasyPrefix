package com.christian34.easyprefix.commands.arguments;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.utils.Message;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * EasyPrefix 2026.
 * <p>
 * Command argument for tags (subgroups), suggests all tag names.
 *
 * @author Christian34
 */
public final class SubgroupArgument {

    private SubgroupArgument() {
    }

    public static final class SubgroupParser<C> implements ArgumentParser<C, Subgroup>, BlockingSuggestionProvider.Strings<C> {

        @Override
        public @NotNull ArgumentParseResult<Subgroup> parse(@NotNull CommandContext<C> commandContext, @NotNull CommandInput commandInput) {
            final String input = commandInput.peekString();

            EasyPrefix instance = EasyPrefix.getInstance();
            Subgroup subgroup;
            if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("null")) {
                subgroup = new Subgroup("null");
            } else {
                subgroup = instance.getGroupHandler().getSubgroup(input);
            }

            if (subgroup != null) {
                commandInput.readString();
                return ArgumentParseResult.success(subgroup);
            } else {
                return ArgumentParseResult.failure(new RuntimeException(Message.CHAT_GROUP_NOT_FOUND.getText()));
            }
        }

        @Override
        public @NotNull Iterable<String> stringSuggestions(@NotNull CommandContext<C> commandContext, @NotNull CommandInput input) {
            List<String> names = EasyPrefix.getInstance().getGroupHandler().getSubgroups().stream().map(Subgroup::getName).collect(Collectors.toList());
            names.add("none");
            return names;
        }

    }

}
