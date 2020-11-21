package com.christian34.easyprefix.utils;

import java.util.List;
import java.util.stream.Collectors;

/*
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public final class ListUtils {

    public static List<String> replace(List<String> list, String placeholder, String value) {
        return list.stream().map(val -> val.replace(placeholder, value)).collect(Collectors.toList());
    }

}
