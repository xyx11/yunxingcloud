package com.yunxingcloud.common.config;

import org.springframework.context.annotation.Configuration;
import java.util.concurrent.*;

@Configuration
public class RateLimitConfig {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    /** 检查是否允许请求 (本地令牌桶, Redis为分布式版) */
    public boolean tryAcquire(String key, int permits, int windowSeconds) {
        TokenBucket bucket = buckets.computeIfAbsent(key,
                k -> new TokenBucket(permits, windowSeconds));
        return bucket.tryAcquire();
    }

    private static class TokenBucket {
        private final int maxTokens;
        private final long windowNanos;
        private long tokens;
        private long lastRefill;

        TokenBucket(int maxTokens, int windowSeconds) {
            this.maxTokens = maxTokens; this.tokens = maxTokens;
            this.windowNanos = windowSeconds * 1_000_000_000L;
            this.lastRefill = System.nanoTime();
        }

        synchronized boolean tryAcquire() {
            refill();
            if (tokens > 0) { tokens--; return true; }
            return false;
        }

        private void refill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefill;
            long newTokens = elapsed * maxTokens / windowNanos;
            if (newTokens > 0) {
                tokens = Math.min(maxTokens, tokens + newTokens);
                lastRefill = now;
            }
        }
    }
}