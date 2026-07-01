package com.yunxingcloud.order.config;

import com.yunxingcloud.order.service.ProductCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CacheWarmer {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmer.class);
    private final ProductCacheService cacheService;

    public CacheWarmer(ProductCacheService cacheService) { this.cacheService = cacheService; }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        log.info("缓存预热中...");
        try { cacheService.getHotProducts(); log.info("  ✓ 热门商品缓存"); } catch (Exception e) { log.warn("  ✗ 热门商品: {}", e.getMessage()); }
        try { cacheService.getNewProducts(); log.info("  ✓ 新品缓存"); } catch (Exception e) { log.warn("  ✗ 新品: {}", e.getMessage()); }
        try { cacheService.getCategories(); log.info("  ✓ 分类缓存"); } catch (Exception e) { log.warn("  ✗ 分类: {}", e.getMessage()); }
        try { cacheService.getBrands(); log.info("  ✓ 品牌缓存"); } catch (Exception e) { log.warn("  ✗ 品牌: {}", e.getMessage()); }
        try { cacheService.getBanners(); log.info("  ✓ 横幅缓存"); } catch (Exception e) { log.warn("  ✗ 横幅: {}", e.getMessage()); }
        log.info("缓存预热完成");
    }
}