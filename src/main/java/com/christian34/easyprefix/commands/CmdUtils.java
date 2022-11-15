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
            input = input.toLowerCase();
            for (String arg : possible) {
                arg = arg.toLowerCase();
                if (arg.equalsIgnoreCase(input) || arg.startsWith(input)) {
                    matches.add(arg);
                }
            }
            return matches;
        }
    }

}
