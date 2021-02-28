package com.myexample.utils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class StringUtil {

    public static final String AMPERSAND = "&";
    public static final String EQUAL = "=";

    private static final Gson GSON = new Gson();

    private StringUtil() {}

    public static String repeat(String str, int n) {
        var nStr = new StringBuilder();
        for (int i = 0; i < n; i++) {
            nStr.append(str);
        }
        return nStr.toString();
    }

    public static String singleEntryJson(String key, String value) {
        return String.format("{\"%s\":\"%s\"}", key, value);
    }

    public static String doubleEntryJson(String key1, String value1, String key2, String value2) {
        return String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", key1, value1, key2, value2);
    }

    public static String valueInJson(String json, String key) {
        var map = GSON.fromJson(json, HashMap.class);
        return (String) map.get(key);
    }

    public static String toJson(Object o) {
        return GSON.toJson(o);
    }

    public static <T> T fromJson(String json, Class<T> cls) {
        return GSON.fromJson(json, cls);
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

    public static String formatDecimal(BigDecimal amount, int scale) {
        String format = "%,." + Integer.toString(scale) + "f";
        return String.format(format, amount);
    }
}
