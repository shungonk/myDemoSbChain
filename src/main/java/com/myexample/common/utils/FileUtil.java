package com.myexample.common.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {

    private FileUtil() {
    }

    public static void serializeObject(String path, Object obj) throws IOException {
        Path filepath = Path.of(path);
        try (var os = Files.newOutputStream(filepath);
            var oos = new ObjectOutputStream(os)) {
            oos.writeObject(obj);
        }
    }

    public static <T> T deserializeObject(String path, Class<T> cls) throws IOException, ClassNotFoundException {
        Path filepath = Path.of(path);
        try (var is = Files.newInputStream(filepath);
            var ois = new ObjectInputStream(is)) {
            return cls.cast(ois.readObject());
        }
    }
}
