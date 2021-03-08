package com.myexample.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * ログ出力を行うクラス
 * 
 * <p>クラスロード時にログプロパティファイル（{@link #LOGGING_PROP_PATH}）に基づいて
 * ログ出力方法が設定されます。ファイルが存在しない場合、クラスのロードは失敗します。ログ
 * 出力する際はこのクラスをstaticに使用します（インスタンス化は行いません）。
 * 
 * <p>このクラスは内部に{@link Logger}を保持しています。
 * 
 * @author  S.Nakanishi
 * @version 1.0
 * @see     Logger
 */
public class LogWriter {

    /**
     * ログプロパティファイルのパス
     */
    public static final String LOGGING_PROP_PATH = "resources/logging.properties";
    
    private static final Logger logger;

    static {
        logger = Logger.getLogger(Logger.class.getName());
        var manager = LogManager.getLogManager();
        var logprop = Path.of(LOGGING_PROP_PATH);
        try (var is = Files.newInputStream(logprop)) {
            manager.readConfiguration(is);
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to initialize LogWriter class", e);
        }
    }

    private LogWriter() {}

    /**
     * INFOレベルでログ出力します。
     * 
     * @param  msg
     *         出力メッセージ
     * @see    Logger#info(String)
     */
    public static void info(String msg) {
        logger.info(msg);
    }

    /**
     * WARNINGレベルでログ出力します。
     * 
     * @param  msg
     *         出力メッセージ
     * @see    Logger#warning(String)
     */
    public static void warning(String msg) {
        logger.warning(msg);
    }

    /**
     * SEVEREレベルでログ出力します。
     * 
     * @param  msg
     *         出力メッセージ
     * @see    Logger#severe(String)
     */
    public static void severe(String msg) {
        logger.severe(msg);
    }

    /**
     * SEVEREレベルでログ出力し、ログ出力後に指定した例外を発生させます。
     * 
     * @param  msg
     *         出力メッセージ
     * @param  thrown
     *         ログ出力後に発生させる例外オブジェクト
     * @see    Logger#log(Level, String, Throwable)
     */
    public static void severe(String msg, Throwable thrown) {
        logger.log(Level.SEVERE, msg, thrown);
    }
    
}
