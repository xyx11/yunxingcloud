package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.FlashSale;
import com.yunxingcloud.order.repository.FlashSaleRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class FlashSaleService {

    private final FlashSaleRepository flashRepo;
    private final StringRedisTemplate redis;

    public FlashSaleService(FlashSaleRepository flashRepo, StringRedisTemplate redis) {
        this.flashRepo = flashRepo;
        this.redis = redis;
    }

    /** 预热: 秒杀开始前将库存加载到 Redis */
    public void preheat(Long flashId) {
        FlashSale fs = flashRepo.findById(flashId).orElseThrow();
        String stockKey = "flash:stock:" + flashId;
        redis.opsForValue().set(stockKey, String.valueOf(fs.getStock() - fs.getSold()),
                fs.getEndTime().getSecond() - LocalDateTime.now().getSecond(), TimeUnit.SECONDS);
    }

    private static final long RESERVE_TIMEOUT_MINUTES = 5;

    /** 秒杀下单: Redis 原子扣减库存。用户有5分钟完成下单，超时库存自动释放 */
    @Transactional
    public boolean tryBuy(Long flashId, Long productId, String username) {
        FlashSale fs = flashRepo.findById(flashId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(fs.getStartTime())) throw new IllegalStateException("秒杀尚未开始");
        if (now.isAfter(fs.getEndTime())) throw new IllegalStateException("秒杀已结束");

        String userKey = "flash:user:" + flashId + ":" + username;
        Long userCount = redis.opsForValue().increment(userKey);
        if (userCount != null && userCount == 1) {
            redis.expire(userKey, 24, TimeUnit.HOURS);
        }
        if (userCount != null && userCount > fs.getLimitPerUser()) {
            redis.opsForValue().decrement(userKey);
            throw new IllegalStateException("已达限购数量 (" + fs.getLimitPerUser() + "件)");
        }

        String stockKey = "flash:stock:" + flashId;
        Long remaining = redis.opsForValue().decrement(stockKey);
        if (remaining == null || remaining < 0) {
            redis.opsForValue().increment(stockKey);
            redis.opsForValue().decrement(userKey);
            throw new IllegalStateException("已售罄");
        }

        // 预留超时: 5分钟内未完成下单则自动释放库存
        String reserveKey = "flash:reserve:" + flashId + ":" + username;
        redis.opsForValue().set(reserveKey, "1", RESERVE_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        flashRepo.incrementSold(flashId);
        return true;
    }

    /** 确认下单: 清除预留标记，库存永久扣减 */
    public void confirmOrder(Long flashId, String username) {
        redis.delete("flash:reserve:" + flashId + ":" + username);
    }

    /** 释放预留库存 (用户取消/超时) */
    public void releaseReservation(Long flashId, String username) {
        String reserveKey = "flash:reserve:" + flashId + ":" + username;
        if (Boolean.TRUE.equals(redis.hasKey(reserveKey))) {
            redis.delete(reserveKey);
            String userKey = "flash:user:" + flashId + ":" + username;
            redis.opsForValue().decrement(userKey);
            redis.opsForValue().increment("flash:stock:" + flashId);
        }
    }

    /** 获取秒杀剩余库存 */
    public int getRemainingStock(Long flashId) {
        String v = redis.opsForValue().get("flash:stock:" + flashId);
        return v != null ? Integer.parseInt(v) : 0;
    }

    /** 设置秒杀提醒 */
    public void setRemind(Long saleId, Long productId, String username) {
        String key = "flash:remind:" + saleId + ":" + username;
        redis.opsForValue().set(key, String.valueOf(productId != null ? productId : ""), 24, TimeUnit.HOURS);
    }
}