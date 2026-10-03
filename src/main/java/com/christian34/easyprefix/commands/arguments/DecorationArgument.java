package com.christian34.easyprefix.commands.arguments;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.Decoration;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * EasyPrefix 2026.
 * <p>
 * Command argument for chat formattings (e.g. bold) - players can only use the formattings they have the permission
 * for.
 *
 * @author Christian34
 */
public final class DecorationArgument {

    private DecorationArgument() {
    }

    public static final class DecorationParser<C> implements ArgumentParser<C, Decoration>, BlockingSuggestionProvider.Strings<C> {

        @Override
        public @NotNull ArgumentParseResult<Decoration> parse(@NotNull CommandContext<C> commandContext, @NotNull CommandInput commandInput) {
            String input = commandInput.peekString();
            Decoration decoration = Decoration.of(input);
            if (decoration != null && available(commandContext).contains(decoration)) {
                commandInput.readString();
                return ArgumentParseResult.success(decoration);
            }
            return ArgumentParseResult.failure(new IllegalArgumentException(Message.FORMATTING_NOT_FOUND.get("formatting", input)));
        }

        @Override
        public @NotNull Iterable<String> stringSuggestions(@NotNull CommandContext<C> commandContext, @NotNull CommandInput input) {
            return available(commandContext).stream().map(Decoration::getName).toList();
        }

        /**
         * admins may set every formatting (e.g. /color &lt;player&gt; format &lt;formatting&gt;), players only their own
         */
        private Collection<Decoration> available(CommandContext<C> commandContext) {
            EasyPrefix instance = EasyPrefix.getInstance();
            if (commandContext.sender() instanceof Player player && !player.hasPermission(UserPermission.ADMIN.toString())) {
                return instance.getUser(player).getDecorations();
            }
            return instance.getDecorations();
        }

    }

}
