package com.yunxingcloud.inventory.config;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedissonLockService {

    private static final Logger log = LoggerFactory.getLogger(RedissonLockService.class);
    private final RedissonClient redisson;

    public RedissonLockService(RedissonClient redisson) {
        this.redisson = redisson;
    }

    public <T> T withLock(String key, long waitSec, long leaseSec, Supplier<T> action) {
        RLock lock = redisson.getLock("lock:inventory:" + key);
        try {
            if (lock.tryLock(waitSec, leaseSec, TimeUnit.SECONDS)) {
                try {
                    return action.get();
                } finally {
                    lock.unlock();
                }
            } else {
                throw new IllegalStateException("系统繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("操作被中断");
        }
    }

    public void withLock(String key, long waitSec, long leaseSec, Runnable action) {
        withLock(key, waitSec, leaseSec, () -> { action.run(); return null; });
    }
}