package com.christian34.easyprefix.commands.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.Subgroup;
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
public final class SubgroupArgument<CommandSender> extends CommandArgument<CommandSender, Subgroup> {

    public SubgroupArgument() {
        super(true, "subgroup", new SubgroupParser<>(), Subgroup.class);
    }

    public static final class SubgroupParser<CommandSender> implements ArgumentParser<CommandSender, Subgroup> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull Subgroup> parse(@NonNull CommandContext<@NonNull CommandSender> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(SubgroupArgument.class, commandContext));
            }

            EasyPrefix instance = EasyPrefix.getInstance();
            Subgroup subgroup;
            if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("null")) {
                subgroup = new Subgroup("null");
            } else {
                subgroup = instance.getGroupHandler().getSubgroup(input);
            }

            if (subgroup != null) {
                inputQueue.remove();
                return ArgumentParseResult.success(subgroup);
            } else {
                return ArgumentParseResult.failure(new RuntimeException(Message.CHAT_GROUP_NOT_FOUND.getText()));
            }
        }

        @Override
        public List<String> suggestions(final @NotNull CommandContext<CommandSender> commandContext, final @NotNull String input) {
            List<String> names = EasyPrefix.getInstance().getGroupHandler().getSubgroups().stream().map(Subgroup::getName).collect(Collectors.toList());
            names.add("none");
            return names;
        }

    }

}
