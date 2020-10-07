package com.christian34.easyprefix.sql;

import java.util.HashMap;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Data {
    private final HashMap<String, Object> hash;

    public Data(HashMap<String, Object> data) {
        this.hash = data;
    }

    public boolean isEmpty() {
        return hash == null || hash.keySet().size() == 0;
    }

    public String getString(String key) {
        if (hash == null) return null;
        return (String) hash.get(key);
    }

    public String getStringOr(String key, String alternative) {
        String value = getString(key);
        return value == null ? alternative : value;
    }

    public HashMap<String, Object> getData() {
        return hash;
    }

    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public boolean getBoolean(String key) {
        return getInt(key) == 1;
    }

}
