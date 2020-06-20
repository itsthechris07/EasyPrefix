package com.christian34.easyprefix.utils;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class InputReader {

    /**
     * @param args    input by user
     * @param counter first input index
     * @return String translated input
     */
    public static String readInput(String[] args, int counter) {
        StringBuilder stringBuilder = new StringBuilder();
        while (args.length > counter) {
            if (counter != 1) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(args[counter]);
            counter++;
        }
        return stringBuilder.toString();
    }

}
