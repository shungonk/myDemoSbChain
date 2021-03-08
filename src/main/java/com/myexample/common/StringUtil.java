package com.myexample.common;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 文字列処理ユーティリティクラス
 * 
 * @author  S.Nakanishi
 * @version 1.0
 */
public class StringUtil {

    private static final Gson GSON = new Gson();
    
    private static final Gson GSON_PRETTY_PRINTING = new GsonBuilder().setPrettyPrinting().create();

    private StringUtil() {}

    /**
     * 文字列を指定した回数繰り返し連結します。
     * 
     * @param  str
     *         繰り返す文字列
     * @param  n
     *         繰り返す回数
     * @return {@code str}を{@code n}回繰り返して連結した文字列
     */
    public static String repeat(String str, int n) {
        var nStr = new StringBuilder();
        for (int i = 0; i < n; i++) {
            nStr.append(str);
        }
        return nStr.toString();
    }

    /**
     * エントリが１つのJSON文字列を生成します。
     * 
     * @param  key
     *         エントリのキー値
     * @param  value
     *         エントリのバリュー値
     * @return JSON文字列
     */
    public static String makeJson(String key, String value) {
        return String.format("{\"%s\":\"%s\"}", key, value);
    }

    /**
     * エントリが２つのJSON文字列を生成します。
     * 
     * @param  key1
     *         １つ目のエントリのキー値
     * @param  value1
     *         １つ目のエントリのバリュー値
     * @param  key2
     *         ２つ目のエントリのキー値
     * @param  value2
     *         ２つ目のエントリのバリュー値
     * @return JSON文字列
     */
    public static String makeJson(String key1, String value1, 
            String key2, String value2) {
        return String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", 
            key1, value1, key2, value2);
    }

    /**
     * マップからJSON文字列を生成します。
     * 
     * @param  map 生成元となるMap
     * @return JSON文字列
     */
    public static String makeJson(LinkedHashMap<String, String> map) {
        return map.entrySet()
            .stream()
            .map(e -> String.format("\"%s\":\"%s\"",
                e.getKey(), e.getValue()))
            .collect(Collectors.joining(",", "{", "}"));
    }

    /**
     * JSON文字列から指定したキー値に対応するバリュー値（文字列）を抽出します。
     * 
     * @param  json
     *         JSON文字列
     * @param  key
     *         取得したいバリュー値に対応するキー値
     * @return キー値に対応するバリュー値（文字列型）
     * 
     * @see    Gson#fromJson(String, Class)
     */
    public static String valueInJson(String json, String key) {
        var map = GSON.fromJson(json, HashMap.class);
        return (String) map.get(key);
    }

    /**
     * JSON文字列を整形します。
     * 
     * @param  json
     *         JSON文字列
     * @return 整形JSON文字列
     */
    public static String formatJson(String json) {
        return json
            .replace(",", "\n")
            .replaceAll("[\"\\{\\}]", "");
    }

    /**
     * オブジェクトをJSON文字列に変換します
     * 
     * @param  o
     *         JSON文字列に変換するオブジェクト
     * @return JSON文字列
     * 
     * @see    Gson#toJson(Object)
     */
    public static String toJson(Object o) {
        return GSON.toJson(o);
    }

    /**
     * オブジェクトを整形JSON文字列に変換します
     * 
     * @param  o
     *         整形JSON文字列に変換するオブジェクト
     * @return 整形JSON文字列
     * 
     * @see    GsonBuilder#setPrettyPrinting()
     */
    public static String toJsonPrettyPrinting(Object o) {
        return GSON_PRETTY_PRINTING.toJson(o);
    }

    /**
     * JSON文字列を指定したオブジェクトに変換します。
     * 
     * @param  <T>
     *         生成するオブジェクトの型変数
     * @param  json
     *         JSON文字列
     * @param  cls
     *         生成するオブジェクトのクラス
     * @return 生成オブジェクト
     * 
     * @see    Gson#fromJson(String, Class)
     */
    public static <T> T fromJson(String json, Class<T> cls) {
        return GSON.fromJson(json, cls);
    }

    /**
     * クエリ文字列をマップに変換します。
     * 
     * @param  query
     *         クエリ文字列
     * @return マップ
     */
    public static Map<String, String> splitQuery(String query) {
        return Arrays
            .stream(query.split("&"))
            .filter(s -> s.contains("="))
            .collect(Collectors.toMap(
                s -> s.substring(0, s.indexOf("=")),
                s -> s.substring(s.indexOf("=") + 1),
                (v1, v2) -> v1
            ));
    }

    /**
     * BigDecimalを指定した小数桁スケールで文字列に書式変換します（カンマを含む）。
     * 
     * @param  amount
     *         書式変換するBigDecimal
     * @param  scale
     *         BigDecimalの小数点以下のスケール
     * @return 書式変換文字列
     */
    public static String formatDecimal(BigDecimal amount, int scale) {
        String format = "%,." + Integer.toString(scale) + "f";
        return String.format(format, amount);
    }
}
