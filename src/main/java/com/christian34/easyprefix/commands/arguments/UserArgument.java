package com.christian34.easyprefix.commands.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public final class UserArgument<CommandSender> extends CommandArgument<CommandSender, User> {

    public UserArgument() {
        super(true, "user", new UserParser<>(), User.class);
    }

    public static final class UserParser<CommandSender> implements ArgumentParser<CommandSender, User> {

        @SuppressWarnings("deprecation")
        @Override
        public @NonNull ArgumentParseResult<@NonNull User> parse(@NonNull CommandContext<@NonNull CommandSender> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(UserArgument.class, commandContext));
            }

            EasyPrefix instance = EasyPrefix.getInstance();
            Player player = Bukkit.getPlayer(input);
            if (player != null) {
                inputQueue.remove();
                return ArgumentParseResult.success(instance.getUser(player));
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
                User user = instance.getUser(offlinePlayer);
                if (!offlinePlayer.hasPlayedBefore() || user == null) {
                    return ArgumentParseResult.failure(new IllegalArgumentException(Message.CHAT_PLAYER_NOT_FOUND.getText()));
                }
                inputQueue.remove();
                return ArgumentParseResult.success(user);
            }
        }

        @Override
        public @NotNull List<String> suggestions(final @NotNull CommandContext<CommandSender> commandContext, final @NotNull String input) {
            List<String> names = new ArrayList<>();
            names.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(p -> p != null).collect(Collectors.toList()));
            names.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).filter(p -> p != null).collect(Collectors.toList()));
            return names;
        }

    }

}
