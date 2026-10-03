package com.christian34.easyprefix.utils.textinput;

import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TaskManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * EasyPrefix 2026.
 * <p>
 * Asks the player for a text in the chat on Folia, which does not support the conversation api of {@link ChatInput}
 * (its timeout uses the BukkitScheduler). Works like it: typing "quit" or not answering within 3 minutes cancels the
 * input. The input is taken from the chat event with the lowest priority, so other plugins listening on the lowest
 * priority as well may see it.
 *
 * @author Christian34
 */
public class FoliaChatInput extends UserInput {
    private static final long TIMEOUT_TICKS = 180 * 20;
    private static final Map<UUID, Consumer<String>> PENDING = new ConcurrentHashMap<>();

    @Override
    public void build(User user, String title, @Nullable String value, Consumer<String> consumer) {
        Player player = user.getPlayer();
        UUID uniqueId = player.getUniqueId();
        player.closeInventory();
        PENDING.put(uniqueId, consumer);
        player.sendMessage(Message.PREFIX_ALT.getText() + " " + title);
        TaskManager.runLater(player, () -> {
            // a newer input of the player has its own timeout
            if (PENDING.remove(uniqueId, consumer)) player.sendMessage(Message.CHAT_INPUT_TIMEOUT.getText());
        }, TIMEOUT_TICKS);
    }

    /**
     * takes the input from the chat - only registered on Folia
     */
    public static class Listener implements org.bukkit.event.Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncChatEvent event) {
            Player player = event.getPlayer();
            Consumer<String> consumer = PENDING.remove(player.getUniqueId());
            if (consumer == null) return;
            event.setCancelled(true);
            String text = event.signedMessage().message();
            if (text.equals("quit")) {
                player.sendMessage(Message.CHAT_INPUT_CANCELLED.getText());
                return;
            }
            TaskManager.run(player, () -> consumer.accept(text));
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            PENDING.remove(event.getPlayer().getUniqueId());
        }

    }

}
