package com.christian34.easyprefix.setup.responder.gui;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.responder.ChatRespond;
import com.christian34.easyprefix.user.User;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class CreateGroupPage {
    private final User user;

    public CreateGroupPage(User user) {
        this.user = user;
        open();
    }

    private void open() {
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        new ChatRespond(user, Messages.getText(Message.CHAT_GROUP), (answer) -> {
            if (answer.split(" ").length == 1) {
                if (groupHandler.isGroup(answer)) {
                    user.sendMessage(Messages.getText(Message.GROUP_EXISTS, user));
                    return ChatRespond.Respond.ERROR;
                } else {
                    groupHandler.createGroup(answer.replace(" ", ""));
                    user.sendMessage(Messages.getText(Message.GROUP_CREATED, user));
                    return ChatRespond.Respond.ACCEPTED;
                }
            } else {
                return ChatRespond.Respond.WRONG_INPUT;
            }
        }).setErrorText("Please type in one word without spaces!");
    }

}