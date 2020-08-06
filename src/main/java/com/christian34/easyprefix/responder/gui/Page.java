package com.christian34.easyprefix.responder.gui;

import com.christian34.easyprefix.user.User;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public abstract class Page {
    private final User user;

    public Page(User user) {
        this.user = user;
    }

}