package com.yunxingcloud.order.config;

import com.yunxingcloud.order.service.ProductCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 缓存预热：应用启动时自动加载首页热榜/新品/分类到缓存
 */
@Component
public class CacheWarmupRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmupRunner.class);

    private final ProductCacheService cacheService;

    public CacheWarmupRunner(ProductCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void run(String... args) {
        log.info("开始缓存预热...");
        try {
            cacheService.getBanners();
            log.info("  ✓ Banners 已缓存");
            cacheService.getHotProducts();
            log.info("  ✓ HotProducts 已缓存");
            cacheService.getNewProducts();
            log.info("  ✓ NewProducts 已缓存");
            cacheService.getCategories();
            log.info("  ✓ Categories 已缓存");
            cacheService.getRecommendedProducts();
            log.info("  ✓ Recommended 已缓存");
            cacheService.getCoupons();
            log.info("  ✓ Coupons 已缓存");
            log.info("缓存预热完成 (6项)");
        } catch (Exception e) {
            log.warn("缓存预热部分失败: {}", e.getMessage());
        }
    }
}