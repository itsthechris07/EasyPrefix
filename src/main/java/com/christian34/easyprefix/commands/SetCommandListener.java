package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.user.UserPermission;
import com.christian34.easyprefix.utils.ChatButtonConfirm;
import com.christian34.easyprefix.utils.Message;
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.sql.Timestamp;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class SetCommandListener implements Listener {
    private final String PREFIX_ALIAS;
    private final String PREFIX_CMD;
    private final String SUFFIX_ALIAS;
    private final String SUFFIX_CMD;
    private final EasyPrefix instance;

    public SetCommandListener(EasyPrefix instance) {
        this.instance = instance;
        this.PREFIX_ALIAS = ConfigKeys.PREFIX_ALIAS.toString().replace("/", "").toLowerCase();
        this.PREFIX_CMD = "ep setprefix";

        this.SUFFIX_ALIAS = ConfigKeys.SUFFIX_ALIAS.toString().replace("/", "").toLowerCase();
        this.SUFFIX_CMD = "ep setsuffix";
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCommandEvent(PlayerCommandPreprocessEvent e) {
        String request = e.getMessage().substring(1);
        if (!isCommand(request)) {
            return;
        }
        e.setCancelled(true);

        boolean isPrefix = false;
        boolean isSuffix = false;

        User user = instance.getUser(e.getPlayer());

        if (StringUtils.startsWithIgnoreCase(request, "ep ")) {
            request = request.substring(3);
        }

        if (StringUtils.startsWithIgnoreCase(request, PREFIX_ALIAS)) {
            request = "setprefix" + request.substring(PREFIX_ALIAS.length());
        } else if (StringUtils.startsWithIgnoreCase(request, SUFFIX_ALIAS)) {
            request = "setsuffix" + request.substring(SUFFIX_ALIAS.length());
        }

        if (StringUtils.startsWithIgnoreCase(request, "setprefix")) {
            isPrefix = true;
        } else {
            isSuffix = true;
        }


        Timestamp next = getNextTimestamp(user.getLastPrefixUpdate());
        if (!next.before(new Timestamp(System.currentTimeMillis()))
                && !user.hasPermission(UserPermission.CUSTOM_BYPASS)) {
            user.getPlayer().sendMessage(getTimeMessage(next));
            return;
        }

        if ((isPrefix && !user.hasPermission(UserPermission.CUSTOM_PREFIX))
                || (isSuffix && !user.hasPermission(UserPermission.CUSTOM_SUFFIX))) {
            user.getPlayer().sendMessage(Message.CHAT_NO_PERMS.getText());
            return;
        }

        String filtered = request
                .replace("setprefix", "")
                .replace("setsuffix", "");

        if (filtered.isEmpty()) {
            e.setCancelled(false);
            return;
        } else {
            filtered = filtered.substring(1);
        }

        String text;
        if (filtered.equalsIgnoreCase("reset")) {
            if (isPrefix) {
                text = Message.CHAT_INPUT_PREFIX_RESET.getText();
            } else {
                text = Message.CHAT_INPUT_SUFFIX_RESET.getText();
            }
        } else {
            if (isPrefix) {
                text = Message.CHAT_INPUT_PREFIX_CONFIRM.getText().replace("%content%", filtered);
            } else {
                text = Message.CHAT_INPUT_SUFFIX_CONFIRM.getText().replace("%content%", filtered);
            }
        }
        ChatButtonConfirm chatButtonConfirm = new ChatButtonConfirm(user.getPlayer(), text, Message.CHAT_BTN_CONFIRM.getText());
        String finalRequest = request;
        chatButtonConfirm.onClick(() -> applyValue(user, finalRequest));
    }

    private void applyValue(User user, String request) {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String value = request.substring(10);
        if (request.startsWith("setprefix")) {
            if (value.equals("reset")) {
                value = null;
            }
            user.setPrefix(value);

            user.saveData("custom_prefix_update", currentTime.toString());
            user.getPlayer().sendMessage(Message.CHAT_INPUT_PREFIX_SAVED.getText()
                    .replace("%content%", user.getPrefix().replace("§", "&")));
        } else if (request.startsWith("setsuffix")) {
            if (value.equals("reset")) {
                value = null;
            }
            user.setSuffix(value);

            user.saveData("custom_suffix_update", currentTime.toString());
            user.getPlayer().sendMessage(Message.CHAT_INPUT_SUFFIX_SAVED.getText()
                    .replace("%content%", user.getSuffix().replace("§", "&")));
        }
    }

    private String getTimeMessage(Timestamp timestamp) {
        long min = (timestamp.getTime() - System.currentTimeMillis()) / 1000 / 60;
        int minutes = (int) (min % 60);
        int hours = (int) ((min / 60) % 24);
        String msg = Message.CHAT_LAYOUT_UPDATE_COOLDOWN.getText();
        return msg.replace("%h%", Integer.toString(hours)).replace("%m%", (minutes == 0) ? "<1" : Integer.toString(minutes));
    }

    private Timestamp getNextTimestamp(long last) {
        double delay = ConfigKeys.CUSTOM_LAYOUT_COOLDOWN.toDouble();
        long newTime = (long) (last + (delay * 60 * 60 * 1000));
        return new Timestamp(newTime);
    }

    private boolean isCommand(String request) {
        request = request.toLowerCase();
        return request.startsWith(PREFIX_ALIAS) || request.startsWith(PREFIX_CMD)
                || request.startsWith(SUFFIX_ALIAS) || request.startsWith(SUFFIX_CMD);
    }

}
