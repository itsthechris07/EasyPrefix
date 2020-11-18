package com.christian34.easyprefix.utils;

import java.util.ArrayList;
import java.util.List;

/*
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class ListUtils {

    public static List<String> replace(List<String> list, String placeholder, String value) {
        List<String> arrayList = new ArrayList<>();
        for (String val : list) {
            val = val.replace(placeholder, value);
            arrayList.add(val);
        }
        return arrayList;
    }

}
