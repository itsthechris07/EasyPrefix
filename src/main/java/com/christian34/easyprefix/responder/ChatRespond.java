package com.christian34.easyprefix.responder;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.user.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

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
    private final EasyPrefix instance;
    private final String TEXT;
    private BukkitTask bukkitTask;
    private Predicate<String> inputReader;
    private Consumer<String> text;

    public ChatRespond(User responder, String text) {
        this.RESPONDER = responder;
        this.TEXT = text.replace("%newline%", "\n");
        this.instance = EasyPrefix.getInstance();
        Bukkit.getPluginManager().registerEvents(LISTENER, instance);
        sendMessage();
        startTimer();
    }

    private void startTimer() {
        this.bukkitTask = Bukkit.getScheduler().runTaskLater(instance, () -> {
            if (RESPONDER != null) RESPONDER.sendMessage(Message.CHAT_SETUP_CANCELLED.getText());
            exit();
        }, 20 * 60);
    }

    private void exit() {
        bukkitTask.cancel();
        HandlerList.unregisterAll(LISTENER);
    }

    public void sendMessage() {
        RESPONDER.getPlayer().closeInventory();
        for (String msg : Message.CHAT_INPUT_LAYOUT.getList()) {
            RESPONDER.getPlayer().sendMessage(msg.replace("%text%", TEXT.replace("%newline%", "\n")));
        }
    }

    public void addInputReader(Predicate<@Nullable String> respond) {
        this.inputReader = respond;
    }

    public void getInput(Consumer<@Nullable String> consumer) {
        this.text = consumer;
    }

    private class ListenUp implements Listener {

        @EventHandler
        public void onChatEvent(AsyncPlayerChatEvent e) {
            if (!e.getPlayer().equals(RESPONDER.getPlayer())) return;
            User user = instance.getUser(e.getPlayer());
            user.getPlayer().spigot().sendMessage();
            if (e.getMessage().equals("quit")) {
                user.getPlayer().sendMessage(Message.CHAT_SETUP_CANCELLED.getText());
                exit();
            } else {
                String msg = e.getMessage();
                if (msg.equalsIgnoreCase("empty")) {
                    msg = null;
                }
                if (inputReader != null) {
                    if (inputReader.test(msg)) {
                        text.accept(msg);
                        exit();
                    }
                } else {
                    text.accept(msg);
                    exit();
                }
            }
            e.setCancelled(true);
        }
    }

}
