package com.christian34.easyprefix.groups.gender;

import com.christian34.easyprefix.messages.Messages;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GenderType {
    private final String name;
    private final String displayName;

    public GenderType(String name) {
        this.name = name.toLowerCase();
        this.displayName = Messages.getText("gender." + name);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

}