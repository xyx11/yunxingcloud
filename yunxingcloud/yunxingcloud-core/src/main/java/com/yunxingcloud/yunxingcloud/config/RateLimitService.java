package com.yunxingcloud.yunxingcloud.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // IP → (windowStart, count)
    private final ConcurrentHashMap<String, long[]> attempts = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 10;   // 10 attempts
    private static final long WINDOW_MS = 60_000; // per minute

    public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        long[] record = attempts.computeIfAbsent(ip, k -> new long[]{now, 0});

        synchronized (record) {
            if (now - record[0] > WINDOW_MS) {
                record[0] = now;
                record[1] = 0;
            }
            if (record[1] >= MAX_ATTEMPTS) {
                return false;
            }
            record[1]++;
            return true;
        }
    }

    @Scheduled(fixedRate = 60_000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        attempts.entrySet().removeIf(e -> {
            long[] r = e.getValue();
            return now - r[0] > WINDOW_MS;
        });
    }
}
