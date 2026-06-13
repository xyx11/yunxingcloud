package com.yunxingcloud.yunxingcloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final String PREFIX = "ratelimit:";

    @Value("${app.ratelimit.max-attempts:10}")
    private int maxAttempts;

    @Value("${app.ratelimit.window-ms:60000}")
    private long windowMs;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConcurrentHashMap<String, long[]> fallback = new ConcurrentHashMap<>();

    public RateLimitService(@org.springframework.beans.factory.annotation.Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String ip) {
        try {
            String key = PREFIX + ip;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofMillis(windowMs));
            }
            return count == null || count <= maxAttempts;
        } catch (Exception e) {
            long now = System.currentTimeMillis();
            long[] record = fallback.computeIfAbsent(ip, k -> new long[]{now, 0});
            synchronized (record) {
                if (now - record[0] > windowMs) {
                    record[0] = now;
                    record[1] = 0;
                }
                if (record[1] >= maxAttempts) return false;
                record[1]++;
                return true;
            }
        }
    }
}
