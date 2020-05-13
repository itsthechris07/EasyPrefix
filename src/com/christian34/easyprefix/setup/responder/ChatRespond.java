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
    private final Function<String, Respond> ANSWER;
    private final String TEXT;
    private final EasyPrefix instance;
    private BukkitTask bukkitTask;
    private String errorText;

    public ChatRespond(User responder, String text, Function<String, Respond> function) {
        this.RESPONDER = responder;
        this.TEXT = text;
        this.ANSWER = function;
        this.instance = EasyPrefix.getInstance();
        sendMessage();
        Bukkit.getPluginManager().registerEvents(LISTENER, instance);
        startTimer();
    }

    public void setErrorText(String message) {
        this.errorText = message;
    }

    private void startTimer() {
        this.bukkitTask = Bukkit.getScheduler().runTaskLater(instance, () -> {
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

    public enum Respond {
        ACCEPTED, CANCELLED, WRONG_INPUT, ERROR
    }

    private class ListenUp implements Listener {

        @EventHandler
        public void onChatEvent(AsyncPlayerChatEvent e) {
            if (!e.getPlayer().equals(RESPONDER.getPlayer())) return;
            User user = EasyPrefix.getInstance().getUser(e.getPlayer());
            if (e.getMessage().equals("quit") || e.getMessage().equals("cancel")) {
                user.sendMessage(Messages.getText(Message.SETUP_CANCELLED, user));
                exit();
            } else {
                Respond respond = ANSWER.apply(e.getMessage());
                switch (respond) {
                    case ACCEPTED:
                    case CANCELLED:
                        exit();
                        break;
                    case WRONG_INPUT:
                        RESPONDER.sendMessage(Messages.getText(Message.CHAT_INPUT_WRONGENTRY).replace("%allowed_inputs%", errorText).replace("%newline%", "\n"));
                        break;
                    case ERROR:
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