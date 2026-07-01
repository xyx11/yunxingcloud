package com.yunxingcloud.common.core;

import java.util.regex.Pattern;

/** 数据清洗工具 */
public final class DataSanitizer {

    private static final Pattern SQL_INJECTION = Pattern.compile(
            "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|(\\b(select|update|delete|insert|drop|alter|create|truncate)\\b)",
            Pattern.CASE_INSENSITIVE);

    private DataSanitizer() {}

    public static String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("[<>\"'%;()&+]", "")
                    .replaceAll("\\s+", " ")
                    .trim();
    }

    public static boolean isSafe(String input) {
        return input == null || !SQL_INJECTION.matcher(input).find();
    }

    public static String truncate(String input, int maxLen) {
        return input != null && input.length() > maxLen ? input.substring(0, maxLen) : input;
    }
}