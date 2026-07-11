package com.yunxingcloud.order.service;

import com.yunxingcloud.order.dto.BrandDTO;
import com.yunxingcloud.order.dto.CategoryDTO;
import com.yunxingcloud.order.dto.ProductReviewDTO;
import com.yunxingcloud.order.dto.ProductSkuDTO;
import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MallService {

    private final ProductCategoryRepository catRepo;
    private final ProductBrandRepository brandRepo;
    private final ProductSkuRepository skuRepo;
    private final ProductReviewRepository reviewRepo;
    private final CouponRepository couponRepo;
    private final CouponUserRepository couponUserRepo;
    private final OrderShipmentRepository shipRepo;
    private final UserAddressRepository addrRepo;
    private final BannerRepository bannerRepo;
    private final ProductRepository productRepo;
    private final ProductCacheService cacheService;
    private final UserFavoriteRepository favRepo;

    public MallService(ProductCategoryRepository catRepo, ProductBrandRepository brandRepo,
                       ProductSkuRepository skuRepo, ProductReviewRepository reviewRepo,
                       CouponRepository couponRepo, CouponUserRepository couponUserRepo,
                       OrderShipmentRepository shipRepo, UserAddressRepository addrRepo,
                       BannerRepository bannerRepo, UserFavoriteRepository favRepo,
                       ProductRepository productRepo, ProductCacheService cacheService) {
        this.catRepo = catRepo; this.brandRepo = brandRepo; this.skuRepo = skuRepo;
        this.reviewRepo = reviewRepo; this.couponRepo = couponRepo; this.couponUserRepo = couponUserRepo;
        this.shipRepo = shipRepo; this.addrRepo = addrRepo;
        this.bannerRepo = bannerRepo; this.favRepo = favRepo;
        this.productRepo = productRepo;
        this.cacheService = cacheService;
    }

    private void evictCache() { cacheService.evictCatalog(); }

    // ===== Home =====
    public Map<String, Object> home() {
        var recommended = productRepo.findByStatus("0", PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "sales")));
        return Map.of(
            "banners", cacheService.getBanners(),
            "hotProducts", cacheService.getHotProducts(),
            "newProducts", cacheService.getNewProducts(),
            "categories", cacheService.getCategories(),
            "recommended", recommended
        );
    }

    // ===== Banners =====
    public List<Banner> listBanners() {
        return bannerRepo.findByStatus("0", Sort.by(Sort.Direction.ASC, "sortOrder"));
    }

    @Transactional
    public Banner createBanner(Banner b) { evictCache(); return bannerRepo.save(b); }

    @Transactional
    public Optional<Banner> updateBanner(Long id, Banner body) {
        return bannerRepo.findById(id).map(b -> {
            if (body.getTitle() != null) b.setTitle(body.getTitle());
            if (body.getImageUrl() != null) b.setImageUrl(body.getImageUrl());
            if (body.getLinkUrl() != null) b.setLinkUrl(body.getLinkUrl());
            if (body.getSortOrder() != null) b.setSortOrder(body.getSortOrder());
            if (body.getStatus() != null) b.setStatus(body.getStatus());
            evictCache(); return bannerRepo.save(b);
        });
    }

    @Transactional
    public void deleteBanner(Long id) { bannerRepo.deleteById(id); evictCache(); }

    // ===== Favorites =====
    public List<UserFavorite> listFavorites(String username) {
        return favRepo.findByUsernameOrderByCreatedAtDesc(username);
    }

    @Transactional
    public void addFavorite(String username, Long productId) {
        if (!favRepo.existsByUsernameAndProductId(username, productId)) {
            UserFavorite f = new UserFavorite(); f.setUsername(username); f.setProductId(productId);
            favRepo.save(f);
        }
    }

    @Transactional
    public void removeFavorite(String username, Long productId) {
        favRepo.deleteByUsernameAndProductId(username, productId);
    }

    public boolean isFavorited(String username, Long productId) {
        return favRepo.existsByUsernameAndProductId(username, productId);
    }

    // ===== Categories =====
    public List<ProductCategory> listCategories() {
        return catRepo.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));
    }

    @Transactional
    public ProductCategory createCategory(CategoryDTO dto) {
        ProductCategory c = new ProductCategory();
        c.setName(dto.getName()); c.setIcon(dto.getIcon());
        c.setSortOrder(dto.getSortOrder()); c.setStatus(dto.getStatus());
        if (dto.getParentId() != null) c.setParentId(dto.getParentId());
        evictCache(); return catRepo.save(c);
    }

    @Transactional
    public Optional<ProductCategory> updateCategory(Long id, CategoryDTO dto) {
        return catRepo.findById(id).map(c -> {
            if (dto.getName() != null) c.setName(dto.getName());
            if (dto.getIcon() != null) c.setIcon(dto.getIcon());
            if (dto.getSortOrder() != null) c.setSortOrder(dto.getSortOrder());
            if (dto.getStatus() != null) c.setStatus(dto.getStatus());
            if (dto.getParentId() != null) c.setParentId(dto.getParentId());
            evictCache(); return catRepo.save(c);
        });
    }

    @Transactional
    public void deleteCategory(Long id) { catRepo.deleteById(id); evictCache(); }

    // ===== Brands =====
    public List<ProductBrand> listBrands() { return brandRepo.findAll(); }

    @Transactional
    public ProductBrand createBrand(BrandDTO dto) {
        ProductBrand b = new ProductBrand();
        b.setName(dto.getName()); b.setLogo(dto.getLogo()); b.setDescription(dto.getDescription());
        evictCache(); return brandRepo.save(b);
    }

    @Transactional
    public Optional<ProductBrand> updateBrand(Long id, BrandDTO dto) {
        return brandRepo.findById(id).map(b -> {
            if (dto.getName() != null) b.setName(dto.getName());
            if (dto.getLogo() != null) b.setLogo(dto.getLogo());
            if (dto.getDescription() != null) b.setDescription(dto.getDescription());
            evictCache(); return brandRepo.save(b);
        });
    }

    @Transactional
    public void deleteBrand(Long id) { brandRepo.deleteById(id); evictCache(); }

    // ===== SKU =====
    public Page<ProductSku> listAllSkus(int page, int size) {
        return skuRepo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    public List<ProductSku> listSkusByProduct(Long productId) {
        return skuRepo.findByProductId(productId);
    }

    @Transactional
    public ProductSku createSku(Long productId, ProductSkuDTO dto) {
        ProductSku sku = new ProductSku();
        sku.setName(dto.getName()); sku.setPrice(dto.getPrice());
        sku.setStock(dto.getStock()); sku.setSkuCode(dto.getSkuCode());
        sku.setProductId(productId); return skuRepo.save(sku);
    }

    @Transactional
    public void deleteSku(Long id) { skuRepo.deleteById(id); }

    // ===== Reviews =====
    public List<ProductReview> listReviewsByProduct(Long productId) {
        return reviewRepo.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public Page<ProductReview> listAllReviews(int page, int size) {
        return reviewRepo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Transactional
    public void deleteReview(Long id) { reviewRepo.deleteById(id); }

    @Transactional
    public ProductReview createReview(Long productId, String username, ProductReviewDTO dto) {
        ProductReview r = new ProductReview();
        r.setRating(dto.getRating()); r.setContent(dto.getContent());
        r.setProductId(productId); r.setUsername(username);
        return reviewRepo.save(r);
    }

    // ===== Coupons =====
    public List<Coupon> listCoupons() { return couponRepo.findAll(); }

    @Transactional
    public Coupon createCoupon(Coupon c) { return couponRepo.save(c); }

    @Transactional
    public void deleteCoupon(Long id) { couponRepo.deleteById(id); }

    public List<Coupon> listAvailableCoupons() {
        return couponRepo.findAll().stream().filter(c ->
            "0".equals(c.getStatus()) && c.getUsedQty() < c.getTotalQty() &&
            (c.getEndTime() == null || c.getEndTime().isAfter(LocalDateTime.now()))
        ).toList();
    }

    @Transactional
    public String claimCoupon(Long id, String username) {
        Coupon c = couponRepo.findById(id).orElse(null);
        if (c == null) return "优惠券不存在";
        if (!"0".equals(c.getStatus())) return "优惠券已失效";
        if (c.getEndTime() != null && c.getEndTime().isBefore(LocalDateTime.now())) return "优惠券已过期";
        if (c.getStartTime() != null && c.getStartTime().isAfter(LocalDateTime.now())) return "优惠券未开始";
        if (c.getUsedQty() >= c.getTotalQty()) return "已领完";
        if (couponUserRepo.existsByCouponIdAndUsername(id, username)) return "已领取过";
        CouponUser cu = new CouponUser(); cu.setCouponId(id); cu.setUsername(username);
        couponUserRepo.save(cu);
        c.setUsedQty(c.getUsedQty() + 1); couponRepo.save(c);
        return null;
    }

    public List<CouponUser> listMyCoupons(String username) {
        return couponUserRepo.findByUsername(username);
    }

    // ===== Shipments =====
    public Page<OrderShipment> listAllShipments(int page, int size) {
        return shipRepo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    public List<OrderShipment> listShipments() { return shipRepo.findAll(); }

    public OrderShipment getShipmentByOrder(Long orderId) {
        return shipRepo.findByOrderId(orderId);
    }

    @Transactional
    public OrderShipment ship(Long orderId, String carrier, String trackingNo) {
        OrderShipment s = new OrderShipment();
        s.setOrderId(orderId); s.setCarrier(carrier); s.setTrackingNo(trackingNo);
        s.setShippedAt(LocalDateTime.now());
        return shipRepo.save(s);
    }

    // ===== Addresses =====
    public List<UserAddress> listAddresses(String username) {
        return addrRepo.findByUsernameOrderByIsDefaultDesc(username);
    }

    @Transactional
    public UserAddress createAddress(String username, UserAddress a) {
        a.setUsername(username); return addrRepo.save(a);
    }

    @Transactional
    public Optional<UserAddress> updateAddress(String username, Long id, UserAddress body) {
        return addrRepo.findById(id).map(a -> {
            if (body.getName() != null) a.setName(body.getName());
            if (body.getPhone() != null) a.setPhone(body.getPhone());
            if (body.getProvince() != null) a.setProvince(body.getProvince());
            if (body.getCity() != null) a.setCity(body.getCity());
            if (body.getDistrict() != null) a.setDistrict(body.getDistrict());
            if (body.getDetail() != null) a.setDetail(body.getDetail());
            if (Boolean.TRUE.equals(body.getIsDefault())) {
                addrRepo.findByUsernameOrderByIsDefaultDesc(username).stream()
                    .filter(UserAddress::getIsDefault).forEach(x -> { x.setIsDefault(false); addrRepo.save(x); });
            }
            if (body.getIsDefault() != null) a.setIsDefault(body.getIsDefault());
            return addrRepo.save(a);
        });
    }

    @Transactional
    public void deleteAddress(Long id) { addrRepo.deleteById(id); }
}
