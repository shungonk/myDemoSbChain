package com.myexample.common.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class StringUtil {

    public static final String AMPERSAND = "&";
    public static final String EQUAL = "=";

    private StringUtil() {}

    public static String messageJson(String message) {
        return singleEntryJson("message", message);
    }

    public static String singleEntryJson(String key, String value) {
        return String.format("{\"%s\":\"%s\"}", key, value);
    }

    public static String singleEntryJson(String key, float value) {
        return String.format("{\"%s\":\"%f\"}", key, value);
    }

    public static Map<String, String> splitQuery(String query) {
        return Arrays
            .stream(query.split(AMPERSAND))
            .filter(s -> s.contains(EQUAL))
            .collect(Collectors.toMap(
                s -> s.substring(0, s.indexOf(EQUAL)),
                s -> s.substring(s.indexOf(EQUAL) + 1),
                (v1, v2) -> v1
            ));
    }
}
