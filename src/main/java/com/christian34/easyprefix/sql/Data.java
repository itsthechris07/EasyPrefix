package com.christian34.easyprefix.sql;

import java.util.Map;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Data {
    private final Map<String, Object> hash;

    public Data(Map<String, Object> data) {
        this.hash = data;
    }

    public boolean isEmpty() {
        return hash == null || hash.keySet().isEmpty();
    }

    public String getString(String key) {
        if (hash == null) return null;
        return (String) hash.get(key);
    }

    public String getStringOr(String key, String alternative) {
        String value = getString(key);
        return value == null ? alternative : value;
    }

    public Map<String, Object> getData() {
        return hash;
    }

    public int getInt(String key) {
        String val = getString(key);
        if (val == null) return 0;
        return Integer.parseInt(getString(key));
    }

    public boolean getBoolean(String key) {
        return getInt(key) == 1;
    }

}
