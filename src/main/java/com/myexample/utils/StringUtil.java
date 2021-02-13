package com.myexample.utils;

import java.util.Base64;
import java.util.Collections;

public class StringUtil {
    
    public static String repeat(String s, int n) {
        return String.join("", Collections.nCopies(n, s));
    }

	public static String base64Encode(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	public static byte[] base64Decode(String data) {
		return Base64.getDecoder().decode(data);
	}
}