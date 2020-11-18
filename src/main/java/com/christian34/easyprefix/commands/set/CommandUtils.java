package com.christian34.easyprefix.commands.set;

import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.messages.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.sql.Timestamp;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
final class CommandUtils {

    static Timestamp getNextTimestamp(long last) {
        double delay = ConfigKeys.CUSTOM_LAYOUT_COOLDOWN.toDouble();
        long newTime = (long) (last + (delay * 60 * 60 * 1000));
        return new Timestamp(newTime);
    }

    static String getTimeMessage(Timestamp timestamp) {
        long min = (timestamp.getTime() - System.currentTimeMillis()) / 1000 / 60;
        int minutes = (int) (min % 60);
        int hours = (int) ((min / 60) % 24);
        String msg = Message.CHAT_LAYOUT_UPDATE_COOLDOWN.getText();
        return msg.replace("%h%", Integer.toString(hours)).replace("%m%", (minutes == 0) ? "<1" : Integer.toString(minutes));
    }

    /**
     * @param args input by user
     * @return String translated input
     */
    static String readInput(List<String> args) {
        StringBuilder stringBuilder = new StringBuilder();
        int counter = 1;
        while (args.size() > counter) {
            String arg = args.get(counter);
            if (arg.equals("submit")) break;
            if (counter != 1) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(arg);
            counter++;
        }
        return stringBuilder.toString();
    }

    static TextComponent buildConfirmComponent(String text, String command) {
        TextComponent msg = new TextComponent(TextComponent.fromLegacyText(text.replace("%newline%", "\n")));
        TextComponent confirm = new TextComponent(TextComponent.fromLegacyText(" " + Message.CHAT_BTN_CONFIRM.getText() + " "));
        confirm.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        msg.addExtra(confirm);
        return msg;
    }

}
