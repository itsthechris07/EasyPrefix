package com.christian34.easyprefix.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2022.
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
                if (arg.toLowerCase().startsWith(input.toLowerCase())) {
                    matches.add(arg);
                }
            }
            return matches;
        }
    }

}
