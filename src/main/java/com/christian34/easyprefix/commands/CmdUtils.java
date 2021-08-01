package com.christian34.easyprefix.commands;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2021.
 *
 * @author Christian34
 */
public class CmdUtils {

    public static List<String> matches(List<String> possible, String input) {
        if (input.isEmpty()) {
            return possible;
        } else {
            List<String> matches = new ArrayList<>();
            for (String arg : possible) {
                if (StringUtils.startsWithIgnoreCase(arg, input)) {
                    matches.add(arg);
                }
            }
            return matches;
        }
    }

}
