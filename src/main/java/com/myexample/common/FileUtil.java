package com.myexample.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {

    private FileUtil() {
    }

    public static void serializeObject(Path path, Object obj) throws IOException {
        try (var os = Files.newOutputStream(path);
            var oos = new ObjectOutputStream(os)) {
            oos.writeObject(obj);
        }
    }

    public static Object deserializeObject(Path path) throws IOException, ClassNotFoundException {
        try (var is = Files.newInputStream(path);
            var ois = new ObjectInputStream(is)) {
            return ois.readObject();
        }
    }
}
