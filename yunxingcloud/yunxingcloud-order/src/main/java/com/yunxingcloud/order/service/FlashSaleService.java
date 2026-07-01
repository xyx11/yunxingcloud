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

    /** 秒杀下单: Redis 原子扣减库存 */
    @Transactional
    public boolean tryBuy(Long flashId, Long productId, String username) {
        // 校验是否在秒杀时间内
        FlashSale fs = flashRepo.findById(flashId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(fs.getStartTime())) throw new IllegalStateException("秒杀尚未开始");
        if (now.isAfter(fs.getEndTime())) throw new IllegalStateException("秒杀已结束");

        // 限购检查
        String userKey = "flash:user:" + flashId + ":" + username;
        Long userCount = redis.opsForValue().increment(userKey);
        if (userCount != null && userCount == 1) {
            redis.expire(userKey, 24, TimeUnit.HOURS);
        }
        if (userCount != null && userCount > fs.getLimitPerUser()) {
            redis.opsForValue().decrement(userKey);
            throw new IllegalStateException("已达限购数量 (" + fs.getLimitPerUser() + "件)");
        }

        // Redis 原子扣减库存
        String stockKey = "flash:stock:" + flashId;
        Long remaining = redis.opsForValue().decrement(stockKey);
        if (remaining == null || remaining < 0) {
            redis.opsForValue().increment(stockKey);  // 回滚
            redis.opsForValue().decrement(userKey);
            throw new IllegalStateException("已售罄");
        }

        // 原子更新 DB 已售数量
        flashRepo.incrementSold(flashId);

        return true;
    }

    /** 获取秒杀剩余库存 */
    public int getRemainingStock(Long flashId) {
        String v = redis.opsForValue().get("flash:stock:" + flashId);
        return v != null ? Integer.parseInt(v) : 0;
    }
}