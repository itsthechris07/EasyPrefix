package com.christian34.easyprefix.responder;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
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
    private final EasyPrefix instance;
    private final String TEXT;
    private BukkitTask bukkitTask;
    private Predicate<String> inputReader;
    private Consumer<String> text;

    public ChatRespond(User responder, String text) {
        this.RESPONDER = responder;
        this.TEXT = text;
        this.instance = EasyPrefix.getInstance();
        Bukkit.getPluginManager().registerEvents(LISTENER, instance);
        if (text != null) {
            sendMessage();
            startTimer();
        }
    }

    private void startTimer() {
        this.bukkitTask = Bukkit.getScheduler().runTaskLater(instance, () -> {
            if (RESPONDER != null) RESPONDER.sendMessage(Message.INPUT_CANCELLED.toString());
            exit();
        }, 20 * 60);
    }

    private void exit() {
        bukkitTask.cancel();
        HandlerList.unregisterAll(LISTENER);
    }

    public void sendMessage() {
        RESPONDER.getPlayer().closeInventory();
        List<String> messages = Messages.getList(Message.CHAT_HEADER);
        for (String msg : messages) {
            RESPONDER.getPlayer().sendMessage(msg.replace("%quit%", "quit").replace("%question%", TEXT.replace("%newline%", "\n")));
        }
    }

    public void sendMessage(BaseComponent baseComponent) {
        RESPONDER.getPlayer().closeInventory();
        RESPONDER.getPlayer().spigot().sendMessage(baseComponent);
    }

    public void addInputReader(Predicate<String> respond) {
        this.inputReader = respond;
    }

    public void getInput(Consumer<String> consumer) {
        this.text = consumer;
    }

    private class ListenUp implements Listener {

        @EventHandler
        public void onChatEvent(AsyncPlayerChatEvent e) {
            if (!e.getPlayer().equals(RESPONDER.getPlayer())) return;
            User user = instance.getUser(e.getPlayer());
            user.getPlayer().spigot().sendMessage();
            if (e.getMessage().equals("quit")) {
                user.sendMessage(Message.SETUP_CANCELLED.toString());
                exit();
            } else {
                if (inputReader != null) {
                    if (inputReader.test(e.getMessage())) {
                        text.accept(e.getMessage());
                        exit();
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
