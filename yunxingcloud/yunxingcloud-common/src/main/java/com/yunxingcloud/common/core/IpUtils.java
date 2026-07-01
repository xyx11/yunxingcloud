package com.yunxingcloud.common.core;

import jakarta.servlet.http.HttpServletRequest;

public final class IpUtils {

    private IpUtils() {}

    public static String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = req.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = req.getRemoteAddr();
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }

    public static boolean isInternalIp(String ip) {
        if (ip == null) return false;
        return ip.startsWith("10.") || ip.startsWith("192.168.") ||
               ip.startsWith("172.16.") || ip.startsWith("127.");
    }
}