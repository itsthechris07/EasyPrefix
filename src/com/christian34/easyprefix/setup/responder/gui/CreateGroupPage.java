package com.christian34.easyprefix.setup.responder.gui;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.responder.ChatRespond;
import com.christian34.easyprefix.user.User;

public class CreateGroupPage {
    private User user;

    public CreateGroupPage(User user) {
        this.user = user;
        open();
    }

    private void open() {
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        new ChatRespond(user, Messages.getText(Message.CHAT_GROUP), (answer) -> {
            if (answer.equals("cancelled")) {
                user.sendMessage(Messages.getText(Message.SETUP_CANCELLED, user));
                return null;
            } else {
                if (answer.split(" ").length == 1) {
                    if (groupHandler.isGroup(answer)) {
                        user.sendMessage(Messages.getText(Message.GROUP_EXISTS, user));
                        return "error";
                    } else {
                        groupHandler.createGroup(answer.replace(" ", ""));
                        user.sendMessage(Messages.getText(Message.GROUP_CREATED, user));
                        return "correct";
                    }
                } else {
                    return "incorrect";
                }
            }
        }).setAllowedEntriesText("Please type in one word without spaces!");
    }

}