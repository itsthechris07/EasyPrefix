package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Updater;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class JoinListener implements Listener {
    private final EasyPrefix instance;

    public JoinListener(EasyPrefix instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        if (!ConfigKeys.USE_JOIN_QUIT.toBoolean()) return;
        User user = instance.getUser(e.getPlayer());

        if (ConfigKeys.HIDE_JOIN_QUIT.toBoolean()) {
            e.setJoinMessage(null);
        } else if (e.getJoinMessage() != null) {
            Group group = user.getGroup();
            String joinMsg = group.getJoinMessage(user);
            e.setJoinMessage(joinMsg);
        }
    }

    @EventHandler
    public void notifyOnJoin(PlayerJoinEvent event) {
        User user = this.instance.getUser(event.getPlayer());
        Bukkit.getScheduler().runTaskAsynchronously(instance.getPlugin(), () -> {
            if (user.getPlayer().hasPermission("easyprefix.admin")) {
                if (instance.getUpdater().checkForUpdates()) {
                    user.sendMessage(Updater.UPDATE_MSG);
                }
            }
            if (ConfigKeys.USE_GENDER.toBoolean() && ConfigKeys.FORCE_GENDER.toBoolean()) {
                if (user.getGenderType() == null) {
                    String prefix = Message.PREFIX_ALT.getText();
                    if (prefix == null) prefix = Message.getPrefix();
                    TextComponent msg = new TextComponent(TextComponent.fromLegacyText(prefix + Message.NOTIFY_GENDER_TEXT.getText()));
                    TextComponent change = new TextComponent(TextComponent.fromLegacyText(Message.NOTIFY_GENDER_BTN.getText()));
                    change.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ep gui settings gender"));
                    msg.addExtra(change);
                    user.getPlayer().spigot().sendMessage(msg);
                }
            }
        });
    }

}
