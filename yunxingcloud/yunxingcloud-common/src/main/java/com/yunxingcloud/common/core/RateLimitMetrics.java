package com.yunxingcloud.common.core;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 限流实时监控: 内存计数器 (Prometheus 可抓取)
 * 通过 /actuator/metrics/ratelimit.* 暴露
 */
@Component
public class RateLimitMetrics {

    private final Map<String, AtomicLong> allowed = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> rejected = new ConcurrentHashMap<>();

    public void recordAllowed(String key) {
        allowed.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();
    }

    public void recordRejected(String key) {
        rejected.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();
    }

    public long getAllowed(String key) {
        return allowed.getOrDefault(key, new AtomicLong(0)).get();
    }

    public long getRejected(String key) {
        return rejected.getOrDefault(key, new AtomicLong(0)).get();
    }

    public double getRejectRate(String key) {
        long a = getAllowed(key);
        long r = getRejected(key);
        long total = a + r;
        return total > 0 ? (double) r / total : 0;
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> result = new ConcurrentHashMap<>();
        for (String key : allowed.keySet()) {
            long a = getAllowed(key);
            long r = getRejected(key);
            result.put(key, Map.of(
                    "allowed", a,
                    "rejected", r,
                    "total", a + r,
                    "rejectRate", String.format("%.1f%%", getRejectRate(key) * 100)
            ));
        }
        return result;
    }
}