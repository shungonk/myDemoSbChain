package com.myexample.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Property {

    private static final String APPLICATION_PROP_PATH = "application.properties";
    private static final Properties properties;

    private Property() {}

    static {
        properties = new Properties();
        try {
            Path filepath = Path.of(APPLICATION_PROP_PATH);
            properties.load(Files.newBufferedReader(filepath, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Property class", e);
        }
    }

    public static String getProperty(final String key) {
        return getProperty(key, "");
    }

    public static String getProperty(final String key, final String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}