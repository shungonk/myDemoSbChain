package com.myexample.common.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertyUtil {

    private static final String INIT_FILE_PATH = "application.properties";
    private static final Properties properties;

    private PropertyUtil() {}

    static {
        properties = new Properties();
        try {
            properties.load(Files.newBufferedReader(Paths.get(INIT_FILE_PATH), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(final String key) {
        return getProperty(key, "");
    }


    public static String getProperty(final String key, final String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}