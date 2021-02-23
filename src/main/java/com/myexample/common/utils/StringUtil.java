package com.myexample.common.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

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

    public static String valueInJson(String json, String key) {
        var map = new Gson().fromJson(json, HashMap.class);
        return (String) map.get(key);
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
