package com.christian34.easyprefix.commands.arguments;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * EasyPrefix 2026.
 * <p>
 * Command argument for the users of online players.
 *
 * @author Christian34
 */
public final class UserArgument {

    private UserArgument() {
    }

    public static final class UserParser<C> implements ArgumentParser<C, User>, BlockingSuggestionProvider.Strings<C> {

        @Override
        public @NotNull ArgumentParseResult<User> parse(@NotNull CommandContext<C> commandContext, @NotNull CommandInput commandInput) {
            final String input = commandInput.peekString();

            EasyPrefix instance = EasyPrefix.getInstance();
            Player player = Bukkit.getPlayer(input);
            if (player != null) {
                commandInput.readString();
                return ArgumentParseResult.success(instance.getUser(player));
            } else {
                // only known players - an unknown name would be looked up at mojang
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(input);
                User user = offlinePlayer != null && offlinePlayer.hasPlayedBefore() ? instance.getUser(offlinePlayer) : null;
                if (user == null) {
                    return ArgumentParseResult.failure(new IllegalArgumentException(Message.CHAT_PLAYER_NOT_FOUND.getText()));
                }
                commandInput.readString();
                return ArgumentParseResult.success(user);
            }
        }

        @Override
        public @NotNull Iterable<String> stringSuggestions(@NotNull CommandContext<C> commandContext, @NotNull CommandInput input) {
            // offline players can still be typed, suggesting all of them would be too slow on big servers
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }

    }

}
