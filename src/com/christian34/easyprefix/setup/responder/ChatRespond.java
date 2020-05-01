package com.christian34.easyprefix.setup.responder;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.function.Function;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class ChatRespond {
    private final ListenUp LISTENER = new ListenUp();
    private final User RESPONDER;
    private final Function<String, String> ANSWER;
    private final String TEXT;
    private String textAllowedEntries;
    private BukkitTask bukkitTask;

    public ChatRespond(User responder, String text, Function<String, String> function) {
        this.RESPONDER = responder;
        this.TEXT = text;
        this.ANSWER = function;
        Bukkit.getPluginManager().registerEvents(LISTENER, EasyPrefix.getInstance().getPlugin());
        sendMessage();
        startTimer();
    }

    public void setAllowedEntriesText(String message) {
        this.textAllowedEntries = message;
    }

    private void startTimer() {
        this.bukkitTask = Bukkit.getScheduler().runTaskLater(EasyPrefix.getInstance().getPlugin(), () -> {
            try {
                RESPONDER.sendMessage(Messages.getText(Message.INPUT_CANCELLED));
            } catch(Exception ignored) {
            }
            exit();
        }, 20 * 60 * 3);
    }

    private void exit() {
        bukkitTask.cancel();
        HandlerList.unregisterAll(LISTENER);
    }

    private void sendMessage() {
        RESPONDER.getPlayer().closeInventory();
        List<String> messages = Messages.getList(Message.CHAT_HEADER);
        for (String msg : messages) {
            RESPONDER.getPlayer().sendMessage(msg.replace("%quit%", "quit").replace("%question%", TEXT.replace("%newline%", "\n")));
        }
    }

    private class ListenUp implements Listener {

        @EventHandler
        public void onChatEvent(AsyncPlayerChatEvent e) {
            if (!e.getPlayer().equals(RESPONDER.getPlayer())) return;
            if (e.getMessage().equals("quit") || e.getMessage().equals("cancel")) {
                ANSWER.apply("cancelled");
                exit();
            } else {
                String respond = ANSWER.apply(e.getMessage());
                switch (respond) {
                    case "correct":
                    case "cancel":
                        exit();
                        break;
                    case "incorrect":
                        RESPONDER.sendMessage(Messages.getText(Message.CHAT_INPUT_WRONGENTRY).replace("%allowed_inputs%", textAllowedEntries).replace("%newline%", "\n"));
                        break;
                    case "error":
                        break;
                    default:
                        sendMessage();
                        break;
                }
            }
            e.setCancelled(true);
        }

    }

}