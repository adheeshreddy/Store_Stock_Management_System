package com.stockManagement.utilities;

public final class StringUtil {
    private StringUtil() {}
    public static boolean isBlank(Object s) {
        return s == null || String.valueOf(s).trim().isEmpty();
    }
}