package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCacheService {

    private static final Logger log = LoggerFactory.getLogger(ProductCacheService.class);

    private final ProductRepository productRepo;
    private final ProductCategoryRepository categoryRepo;
    private final ProductBrandRepository brandRepo;
    private final BannerRepository bannerRepo;
    private final CouponRepository couponRepo;

    public ProductCacheService(ProductRepository productRepo, ProductCategoryRepository categoryRepo,
                               ProductBrandRepository brandRepo, BannerRepository bannerRepo,
                               CouponRepository couponRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.brandRepo = brandRepo;
        this.bannerRepo = bannerRepo;
        this.couponRepo = couponRepo;
    }

    @Cacheable(value = "hotProducts", unless = "#result == null || #result.isEmpty()")
    public List<Product> getHotProducts() {
        log.debug("Cache MISS: hotProducts");
        return productRepo.findByIsHotTrueAndStatus("0", Sort.by(Sort.Direction.DESC, "sales"));
    }

    @Cacheable(value = "newProducts", unless = "#result == null || #result.isEmpty()")
    public List<Product> getNewProducts() {
        log.debug("Cache MISS: newProducts");
        return productRepo.findByIsNewTrueAndStatus("0", Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Cacheable(value = "categories", unless = "#result == null || #result.isEmpty()")
    public List<ProductCategory> getCategories() {
        log.debug("Cache MISS: categories");
        return categoryRepo.findAll();
    }

    @Cacheable(value = "brands", unless = "#result == null || #result.isEmpty()")
    public List<ProductBrand> getBrands() {
        log.debug("Cache MISS: brands");
        return brandRepo.findAll();
    }

    @Cacheable(value = "banners", unless = "#result == null || #result.isEmpty()")
    public List<Banner> getBanners() {
        log.debug("Cache MISS: banners");
        return bannerRepo.findAllByOrderBySortOrderAsc();
    }

    @Cacheable(value = "recommendedProducts", unless = "#result == null || #result.isEmpty()")
    public List<Product> getRecommendedProducts() {
        log.debug("Cache MISS: recommendedProducts");
        return productRepo.findByStatus("0", PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "sales")));
    }

    @Cacheable(value = "coupons", unless = "#result == null || #result.isEmpty()")
    public List<Coupon> getCoupons() {
        log.debug("Cache MISS: coupons");
        return couponRepo.findAll();
    }

    @CacheEvict(value = {"recommendedProducts", "coupons"}, allEntries = true)
    public void evictHomeCache() {}

    @CacheEvict(value = "hotProducts", allEntries = true)
    public void evictHotProducts() {}

    @CacheEvict(value = "newProducts", allEntries = true)
    public void evictNewProducts() {}

    @CacheEvict(value = {"categories", "brands", "banners"}, allEntries = true)
    public void evictCatalog() {}

    /** 预热所有缓存 */
    public java.util.Map<String, Object> refreshAllCaches() {
        int count = 0;
        try { getHotProducts(); count++; } catch (Exception ignored) {}
        try { getNewProducts(); count++; } catch (Exception ignored) {}
        try { getCategories(); count++; } catch (Exception ignored) {}
        try { getBanners(); count++; } catch (Exception ignored) {}
        try { getRecommendedProducts(); count++; } catch (Exception ignored) {}
        try { getCoupons(); count++; } catch (Exception ignored) {}
        log.info("缓存预热完成: {} 项", count);
        return java.util.Map.of("refreshed", count, "status", "OK");
    }
}