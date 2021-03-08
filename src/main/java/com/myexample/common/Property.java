package com.myexample.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * プロパティ値を取得するクラス
 * 
 * <p>クラスロード時にプロパティファイル（{@link #APPLICATION_PROP_PATH}）から値を
 * 取得します。ファイルが存在しない場合、クラスのロードは失敗します。プロパティ値を取得する
 * 際はこのクラスをstaticに使用します（インスタンス化は行いません）。
 * 
 * <p>このクラスは内部に{@link Properties}を保持しています。
 * 
 * @author  S.Nakanishi
 * @version 1.0
 * @see     Properties
 */
public class Property {

    /**
     * プロパティファイルのパス
     */
    public static final String APPLICATION_PROP_PATH = "resources/application.properties";
    
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

    /**
     * 指定したキー値に対応するプロパティ値を取得します。
     * 
     * @param  key
     *         キー値
     * @return {@code key}に対応するプロパティ値
     * 
     * @see    #getProperty(String, String)
     */
    public static String getProperty(final String key) {
        return getProperty(key, "");
    }

    /**
     * 指定したキー値に対応するプロパティ値を取得し、キー値が存在しない場合は指定した
     * デフォルト値を返却します。
     * 
     * @param  key
     *         キー値
     * @param  defaultValue
     *         {@code key}に対応するプロパティ値が取得できなかった場合に返却する値
     * @return {@code key}に対応するプロパティ値、または{@code defaultValue}
     * 
     * @see    Properties#getProperty(String, String)
     */
    public static String getProperty(final String key, final String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}