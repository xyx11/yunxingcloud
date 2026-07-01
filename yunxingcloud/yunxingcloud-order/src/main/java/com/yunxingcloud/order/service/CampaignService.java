package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Campaign;
import com.yunxingcloud.order.repository.CampaignRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class CampaignService {

    private final CampaignRepository repo;
    private final StringRedisTemplate redis;

    public CampaignService(CampaignRepository repo, StringRedisTemplate redis) {
        this.repo = repo; this.redis = redis;
    }

    /** 计算优惠金额 (满减/折扣) */
    public long calculateDiscount(Long campaignId, Long orderAmount, String username) {
        Campaign c = repo.findById(campaignId).orElseThrow();
        if (!"1".equals(c.getStatus())) throw new IllegalStateException("活动未开始或已结束");
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(c.getStartTime()) || now.isAfter(c.getEndTime()))
            throw new IllegalStateException("不在活动时间内");

        // 限购检查
        String userKey = "campaign:user:" + campaignId + ":" + username;
        Long used = redis.opsForValue().increment(userKey);
        if (used != null && used == 1) redis.expire(userKey, 24, TimeUnit.HOURS);
        if (used != null && used > c.getLimitPerUser()) {
            redis.opsForValue().decrement(userKey);
            throw new IllegalStateException("已达参与次数上限");
        }

        // 库存检查
        if (c.getTotalStock() != null && c.getUsedCount() >= c.getTotalStock())
            throw new IllegalStateException("活动已抢完");

        return switch (c.getType()) {
            case "full_reduction" -> orderAmount >= c.getThreshold() ? c.getDiscount() : 0;
            case "discount" -> {
                long d = orderAmount * c.getDiscount() / 100;
                yield c.getMaxDiscount() != null ? Math.min(d, c.getMaxDiscount()) : d;
            }
            default -> 0;
        };
    }

    @Transactional
    public void lockStock(Long campaignId) {
        int updated = repo.incrementUsedCount(campaignId);
        if (updated == 0) throw new IllegalStateException("活动库存不足或不存在");
    }
}