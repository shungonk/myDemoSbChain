package com.myexample.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogWriter {

    private static final String LOGGING_PROP_PATH = "resources/logging.properties";
    private static final Logger logger;

    static {
        logger = Logger.getLogger(Logger.class.getName());
        var manager = LogManager.getLogManager();
        var logprop = Path.of(LOGGING_PROP_PATH);
        try (var is = Files.newInputStream(logprop)) {
            manager.readConfiguration(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize LogWriter class", e);
        }
    }

    private LogWriter() {}

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void warning(String msg) {
        logger.warning(msg);
    }

    public static void severe(String msg) {
        logger.severe(msg);
    }

    public static void severe(String msg, Throwable thrown) {
        logger.log(Level.SEVERE, msg, thrown);
    }
    
}
