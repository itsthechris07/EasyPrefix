package com.christian34.easyprefix.sql;

import com.christian34.easyprefix.utils.Debug;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public String getString(String key) {
        if (hash == null) return null;
        return (String) hash.get(key);
    }

    @NotNull
    public String getStringOr(@NotNull String key, @NotNull String alternative) {
        String value = getString(key);
        return value == null ? alternative : value;
    }

    public Map<String, Object> getData() {
        return hash;
    }

    public boolean getBoolean(String key) {
        String val = getString(key);
        if (val == null) return false;
        try {
            return Integer.parseInt(val) == 1;
        } catch (NumberFormatException ex) {
            Debug.warn("Column '" + key + "' must contain a valid boolean! (1 = true, 0 = false)");
            return false;
        }
    }

}
