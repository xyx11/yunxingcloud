package com.yunxingcloud.common.core;

import com.yunxingcloud.common.annotation.Sensitive;

public final class SensitiveSerializer {

    private SensitiveSerializer() {}

    public static String mask(String s, Sensitive.Type type) {
        if (s == null || s.isEmpty()) return s;
        return switch (type) {
            case PHONE   -> s.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
            case EMAIL   -> s.replaceAll("(.).*(@.*)", "$1***$2");
            case ID_CARD -> s.replaceAll("(\\d{4})\\d+(\\d{4})", "$1********$2");
            case NAME    -> s.length() <= 2 ? s.charAt(0) + "*" : s.charAt(0) + "*".repeat(s.length() - 1);
            case ADDRESS -> s.length() > 6 ? s.substring(0, 6) + "***" : s + "***";
            case AUTO    -> autoMask(s);
        };
    }

    private static String autoMask(String s) {
        if (s.matches("^1[3-9]\\d{9}$")) return mask(s, Sensitive.Type.PHONE);
        if (s.contains("@")) return mask(s, Sensitive.Type.EMAIL);
        if (s.length() == 18 && s.matches("\\d{17}[0-9Xx]")) return mask(s, Sensitive.Type.ID_CARD);
        if (s.length() > 10) return mask(s, Sensitive.Type.ADDRESS);
        return s;
    }
}