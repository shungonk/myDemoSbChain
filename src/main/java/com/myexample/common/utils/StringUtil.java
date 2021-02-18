package com.myexample.common.utils;

public class StringUtil {

    private StringUtil() {}

    public static String messageJson(String message) {
        return String.format("{\"message\":\"%s\"}", message);
    }
}
