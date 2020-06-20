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
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class ChatRespond {
    private final ListenUp LISTENER = new ListenUp();
    private final User RESPONDER;
    private final String TEXT;
    private final EasyPrefix instance;
    private BukkitTask bukkitTask;
    private Predicate<String> inputReader;
    private Consumer<String> text;

    public ChatRespond(User responder, String text) {
        this.RESPONDER = responder;
        this.TEXT = text;
        this.instance = EasyPrefix.getInstance();
        Bukkit.getPluginManager().registerEvents(LISTENER, instance);
        sendMessage();
        startTimer();
    }

    private void startTimer() {
        this.bukkitTask = Bukkit.getScheduler().runTaskLater(instance, () -> {
            try {
                RESPONDER.sendMessage(Message.INPUT_CANCELLED.toString());
            } catch (Exception ignored) {
            }
            exit();
        }, 20 * 60);
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

    public ChatRespond addInputReader(Predicate<String> respond) {
        this.inputReader = respond;
        return this;
    }

    public ChatRespond getInput(Consumer<String> consumer) {
        this.text = consumer;
        return this;
    }

    private class ListenUp implements Listener {

        @EventHandler
        public void onChatEvent(AsyncPlayerChatEvent e) {
            if (!e.getPlayer().equals(RESPONDER.getPlayer())) return;
            User user = instance.getUser(e.getPlayer());
            if (e.getMessage().equals("quit") || e.getMessage().equals("cancel")) {
                user.sendMessage(Message.SETUP_CANCELLED.toString());
                exit();
            } else {
                if (inputReader != null) {
                    if (inputReader.test(e.getMessage())) {
                        text.accept(e.getMessage());
                        exit();
                    } else {

                    }
                } else {
                    text.accept(e.getMessage());
                    exit();
                }


            }
            e.setCancelled(true);
        }
    }

}
