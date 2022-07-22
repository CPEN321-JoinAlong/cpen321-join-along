package com.joinalongapp;

public class TextInputUtils {
    public static boolean isValidNameTitle(String input) {
        String regex = "^[\\w\\-\\s]*$";
        return input.matches(regex);
    }
}
