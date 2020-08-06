package com.christian34.easyprefix.utils;

import io.sentry.Sentry;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Debug {

    static {
        Sentry.getContext().setUser(new UserBuilder().setId("test").build());
    }

    public static void recordAction(String message) {
        Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setMessage(message).build());
    }


}
