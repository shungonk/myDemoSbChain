package com.myexample.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ファイル処理ユーティリティクラス
 * 
 * @author  S.Nakanishi
 * @version 1.0
 */
public class FileUtil {

    private FileUtil() {
    }

    /**
     * ファイルにオブジェクトをシリアライズして保存します。
     * 
     * @param  path
     *         出力ファイルパス
     * @param  obj
     *         ファイルにシリアライズして保存するオブジェクト
     * @throws IOException
     *         I/Oエラーが起こった場合
     * @see    ObjectOutputStream
     */
    public static void serializeObject(Path path, Object obj) 
            throws IOException {
        try (var os = Files.newOutputStream(path);
            var oos = new ObjectOutputStream(os)) {
            oos.writeObject(obj);
        }
    }

    /**
     * ファイルからオブジェクトをデシリアライズします。
     * 
     * @param  path
     *         入力ファイルパス
     * @return ファイルからデシリアライズされたオブジェクト
     * 
     * @throws IOException
     *         I/Oエラーが起こった場合
     * @throws ClassNotFoundException
     *         デシリアライズしたオブジェクトに該当するクラスが存在しない場合
     * @see    ObjectOutputStream
     */
    public static Object deserializeObject(Path path) 
            throws IOException, ClassNotFoundException {
        try (var is = Files.newInputStream(path);
            var ois = new ObjectInputStream(is)) {
            return ois.readObject();
        }
    }
}
