package com.yunxingcloud.common.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简易风控引擎: 频率检测/异常行为标记
 */
@Service
public class RiskControlService {

    private static final Logger log = LoggerFactory.getLogger(RiskControlService.class);
    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final Map<String, Long> blocklist = new ConcurrentHashMap<>();

    /** 检测频率异常 */
    public boolean isSuspicious(String key, int maxPerMinute) {
        int count = counters.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
        if (count > maxPerMinute) {
            log.warn("[风控] 频率异常: key={} count={}/min", key, count);
            return true;
        }
        return false;
    }

    /** 封禁 */
    public void block(String key, int minutes) {
        blocklist.put(key, System.currentTimeMillis() + minutes * 60_000L);
        log.warn("[风控] 已封禁: key={} duration={}min", key, minutes);
    }

    public boolean isBlocked(String key) {
        Long expireAt = blocklist.get(key);
        if (expireAt != null && System.currentTimeMillis() < expireAt) return true;
        if (expireAt != null) blocklist.remove(key);
        return false;
    }

    /** 重置计数器 (可定时调用) */
    public void reset() { counters.clear(); }
}