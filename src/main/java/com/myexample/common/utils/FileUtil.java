package com.myexample.common.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileUtil {

    private FileUtil() {
    }

    public static void serializeObject(String path, Object obj) throws IOException {
        try (var os = new FileOutputStream(path);
            var oos = new ObjectOutputStream(os)) {
            oos.writeObject(obj);
        }
    }

    public static <T> T deserializeObject(String path, Class<T> cls) throws IOException, ClassNotFoundException {
        try (var is = new FileInputStream(path);
            var ois = new ObjectInputStream(is)) {
            return cls.cast(ois.readObject());
        }
    }
}
