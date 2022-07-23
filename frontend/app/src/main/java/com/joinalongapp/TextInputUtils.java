package com.joinalongapp;

public class TextInputUtils {

    /**
     * Returns true if string of any length contains only alphanumerics, -, and whitespaces.
     * @param input
     * @return
     */
    public static boolean isValidNameTitle(String input) {
        String regex = "^[\\w\\-\\s]*$";
        return input.matches(regex);
    }
}
