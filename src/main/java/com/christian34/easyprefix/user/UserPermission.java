package com.christian34.easyprefix.user;

/**
 * EasyPrefix 2026.
 * <p>
 * Permissions of EasyPrefix, e.g. ADMIN is EasyPrefix.admin.
 *
 * @author Christian34
 */
public enum UserPermission {
    ADMIN, SETTINGS, CUSTOM_PREFIX, CUSTOM_SUFFIX, CUSTOM_BYPASS, TAGS_SWITCH, CUSTOM_BLACKLIST,
    CUSTOM_HEX, CUSTOM_GRADIENT, CUSTOM_SHADOW;

    private final static String PERMISSION_PREFIX = "EasyPrefix.";

    @Override
    public String toString() {
        return PERMISSION_PREFIX + name().toLowerCase().replace("_", ".");
    }

}
