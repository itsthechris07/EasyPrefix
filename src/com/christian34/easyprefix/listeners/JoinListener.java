package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
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
    private EasyPrefix instance;

    public JoinListener(EasyPrefix instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        ConfigData configData = this.instance.getFileManager().getConfig();
        if (!configData.getBoolean(ConfigData.ConfigKeys.USE_JOIN_QUIT)) return;
        User user = instance.getUser(e.getPlayer());

        if (configData.getBoolean(ConfigData.ConfigKeys.HIDE_JOIN_QUIT)) {
            e.setJoinMessage(null);
        } else if (e.getJoinMessage() != null) {
            Group group = user.getGroup();
            String joinMsg = group.getJoinMessage(user);
            e.setJoinMessage(joinMsg);
        }
        if (configData.getBoolean(ConfigData.ConfigKeys.USE_JOIN_SOUND)) {
            String cfg = configData.getString(ConfigData.ConfigKeys.JOIN_SOUND);
            String[] soundOption = cfg.replace(" ", "").split(";");
            try {
                Sound sound = Sound.valueOf(soundOption[0]);
                float volume = Integer.parseInt(soundOption[1]);
                float pitch = Integer.parseInt(soundOption[2]);
                if (soundOption.length == 3) {
                    String receiver = configData.getString(ConfigData.ConfigKeys.JOIN_QUIT_SOUND_RECEIVER);
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
            } catch(IllegalArgumentException ignored) {
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
            if (this.instance.getFileManager().getConfig().getBoolean(ConfigData.ConfigKeys.FORCE_GENDER)) {
                if (user.getGenderType() == null) {
                    String prefix = Messages.getText("info.prefix");
                    if (prefix == null) prefix = Messages.getPrefix();
                    TextComponent msg = new TextComponent(TextComponent.fromLegacyText(prefix + Messages.getText(Message.NOTIFY_GENDER_TEXT)));
                    TextComponent change = new TextComponent(TextComponent.fromLegacyText(Messages.getText(Message.NOTIFY_GENDER_BTN)));
                    change.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ep gui settings gender"));
                    msg.addExtra(change);
                    user.getPlayer().spigot().sendMessage(msg);
                }
            }
        });
    }

}