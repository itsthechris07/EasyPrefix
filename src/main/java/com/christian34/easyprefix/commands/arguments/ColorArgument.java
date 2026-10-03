package com.christian34.easyprefix.commands.arguments;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Message;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * EasyPrefix 2026.
 * <p>
 * Command argument for chat colors - players can only use the colors they have the permission for.
 *
 * @author Christian34
 */
public final class ColorArgument {

    private ColorArgument() {
    }

    public static final class ColorParser<C> implements ArgumentParser<C, Color>, BlockingSuggestionProvider.Strings<C> {

        @Override
        public @NotNull ArgumentParseResult<Color> parse(@NotNull CommandContext<C> commandContext, @NotNull CommandInput commandInput) {
            String input = StringUtils.deleteWhitespace(commandInput.peekString());

            Optional<Color> optional;
            // admins may set every color (e.g. /color <player> set <color>), players only their own
            if (commandContext.sender() instanceof Player player && !player.hasPermission(UserPermission.ADMIN.toString())) {
                User user = EasyPrefix.getInstance().getUser(player);
                optional = user.getColors().stream().filter(color -> color.getName().equalsIgnoreCase(input)).findAny();
            } else {
                optional = EasyPrefix.getInstance().getColors().stream().filter(color -> color.getName().equalsIgnoreCase(input)).findAny();
            }
            if (optional.isPresent()) {
                commandInput.readString();
                return ArgumentParseResult.success(optional.get());
            } else {
                return ArgumentParseResult.failure(new IllegalArgumentException(Message.COLOR_NOT_FOUND.get("color", input)));
            }
        }

        @Override
        public @NotNull Iterable<String> stringSuggestions(@NotNull CommandContext<C> commandContext, @NotNull CommandInput input) {
            List<String> color;
            if (commandContext.sender() instanceof Player player && !player.hasPermission(UserPermission.ADMIN.toString())) {
                User user = EasyPrefix.getInstance().getUser(player);
                color = user.getColors().stream().map(Color::getName).collect(Collectors.toList());
            } else {
                color = EasyPrefix.getInstance().getColors().stream().map(Color::getName).collect(Collectors.toList());
            }
            return color;
        }

    }

}
