package com.christian34.easyprefix.user;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public enum UserPermission {
    ADMIN,
    SETTINGS,
    CUSTOM_PREFIX,
    CUSTOM_SUFFIX,
    CUSTOM_BYPASS;

    private final static String PERMISSION_PREFIX = "EasyPrefix.";

    @Override
    public String toString() {
        return PERMISSION_PREFIX + name().toLowerCase().replace("_", ".");
    }

}
