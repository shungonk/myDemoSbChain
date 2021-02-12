package com.myexample.utils;

import java.util.Collections;

public class StringUtil {
    
    public static String repeat(String s, int n) {
        return String.join("", Collections.nCopies(n, s));
    }
}
