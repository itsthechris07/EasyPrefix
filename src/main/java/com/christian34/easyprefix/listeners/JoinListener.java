package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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

        ConfigData configData = this.instance.getFileManager().getConfig();
        if (configData.getData().getBoolean("config.join-quit-messages.sound.join.enabled")) {
            String cfg = configData.getData().getString("config.join-quit-messages.sound.join.sound");
            if (cfg == null) return;
            String[] soundOption = cfg.replace(" ", "").split(";");
            try {
                Sound sound = Sound.valueOf(soundOption[0]);
                float volume = Integer.parseInt(soundOption[1]);
                float pitch = Integer.parseInt(soundOption[2]);
                if (soundOption.length == 3) {
                    String receiver = ConfigKeys.JOIN_QUIT_SOUND_RECEIVER.toString();
                    if (receiver.equals("all")) {
                        for (Player target : Bukkit.getOnlinePlayers()) {
                            target.playSound(target.getLocation(), sound, volume, pitch);
                        }
                    } else {
                        user.getPlayer().playSound(user.getPlayer().getLocation(), sound, volume, pitch);
                    }
                } else {
                    Messages.log("&cCouldn't play sound on player join. Please check up the sound configuration.");
                }
            } catch (IllegalArgumentException ignored) {
                Messages.log("&cCouldn't play sound '" + soundOption[0] + "'. Please use valid sounds!");
            }
        }
    }

    @EventHandler
    public void notifyOnJoin(PlayerJoinEvent event) {
        User user = this.instance.getUser(event.getPlayer());
        Bukkit.getScheduler().runTaskAsynchronously(instance.getPlugin(), () -> {
            if (user.getPlayer().hasPermission("easyprefix.admin")) {
                if (instance.getUpdater().checkForUpdates()) {
                    user.sendMessage(instance.getUpdater().UPDATE_MSG);
                }
            }
            if (ConfigKeys.USE_GENDER.toBoolean() && ConfigKeys.FORCE_GENDER.toBoolean()) {
                if (user.getGenderType() == null) {
                    String prefix = Messages.getText("info.prefix");
                    if (prefix == null) prefix = Messages.getPrefix();
                    TextComponent msg = new TextComponent(TextComponent.fromLegacyText(prefix + Message.NOTIFY_GENDER_TEXT.toString()));
                    TextComponent change = new TextComponent(TextComponent.fromLegacyText(Message.NOTIFY_GENDER_BTN.toString()));
                    change.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ep gui settings gender"));
                    msg.addExtra(change);
                    user.getPlayer().spigot().sendMessage(msg);
                }
            }
        });
    }

}
