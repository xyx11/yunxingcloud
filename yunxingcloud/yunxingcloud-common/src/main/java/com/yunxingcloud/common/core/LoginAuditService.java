package com.yunxingcloud.common.core;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoginAuditService {

    private static final Logger log = LoggerFactory.getLogger("LOGIN_AUDIT");

    @Async
    public void recordSuccess(String username, HttpServletRequest req) {
        log.info("LOGIN_SUCCESS | user={} | ip={} | ua={} | os={} | time={}",
                username, getIp(req), getUserAgent(req),
                getOS(req), LocalDateTime.now());
    }

    @Async
    public void recordFailure(String username, HttpServletRequest req, String reason) {
        log.warn("LOGIN_FAILED | user={} | ip={} | ua={} | reason={} | time={}",
                username, getIp(req), getUserAgent(req),
                reason, LocalDateTime.now());
    }

    @Async
    public void recordLogout(String username) {
        log.info("LOGOUT | user={} | time={}", username, LocalDateTime.now());
    }

    private String getIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = req.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = req.getRemoteAddr();
        // 多级代理取第一个
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }

    private String getUserAgent(HttpServletRequest req) {
        String ua = req.getHeader("User-Agent");
        if (ua == null) return "unknown";
        if (ua.contains("Edg")) return "Edge";
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Safari")) return "Safari";
        if (ua.contains("Firefox")) return "Firefox";
        return ua.length() > 50 ? ua.substring(0, 50) : ua;
    }

    private String getOS(HttpServletRequest req) {
        String ua = req.getHeader("User-Agent");
        if (ua == null) return "unknown";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac")) return "macOS";
        if (ua.contains("Linux")) return "Linux";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        return "Other";
    }
}