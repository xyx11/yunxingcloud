package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
public class MallController {

    private final ProductCategoryRepository catRepo;
    private final ProductBrandRepository brandRepo;
    private final ProductSkuRepository skuRepo;
    private final ProductReviewRepository reviewRepo;
    private final CouponRepository couponRepo;
    private final CouponUserRepository couponUserRepo;
    private final OrderShipmentRepository shipRepo;
    private final UserAddressRepository addrRepo;
    private final BannerRepository bannerRepo;
    private final UserFavoriteRepository favRepo;

    public MallController(ProductCategoryRepository catRepo, ProductBrandRepository brandRepo,
                          ProductSkuRepository skuRepo, ProductReviewRepository reviewRepo,
                          CouponRepository couponRepo, CouponUserRepository couponUserRepo,
                          OrderShipmentRepository shipRepo, UserAddressRepository addrRepo,
                          BannerRepository bannerRepo, UserFavoriteRepository favRepo) {
        this.catRepo = catRepo; this.brandRepo = brandRepo; this.skuRepo = skuRepo;
        this.reviewRepo = reviewRepo; this.couponRepo = couponRepo; this.couponUserRepo = couponUserRepo;
        this.shipRepo = shipRepo; this.addrRepo = addrRepo;
        this.bannerRepo = bannerRepo; this.favRepo = favRepo;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    // ===== Banners =====
    @GetMapping("/api/banners") public ResponseEntity<?> banners() {
        return ResponseEntity.ok(bannerRepo.findByStatus("0", Sort.by(Sort.Direction.ASC, "sortOrder")));
    }
    @PostMapping("/api/banners") public ResponseEntity<?> addBanner(@RequestBody Banner b) { return ResponseEntity.ok(bannerRepo.save(b)); }
    @PutMapping("/api/banners/{id}") public ResponseEntity<?> updateBanner(@PathVariable Long id, @RequestBody Banner body) {
        return bannerRepo.findById(id).map(b -> {
            if (body.getTitle() != null) b.setTitle(body.getTitle());
            if (body.getImageUrl() != null) b.setImageUrl(body.getImageUrl());
            if (body.getLinkUrl() != null) b.setLinkUrl(body.getLinkUrl());
            if (body.getSortOrder() != null) b.setSortOrder(body.getSortOrder());
            if (body.getStatus() != null) b.setStatus(body.getStatus());
            return ResponseEntity.ok(bannerRepo.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/api/banners/{id}") public ResponseEntity<?> delBanner(@PathVariable Long id) { bannerRepo.deleteById(id); return ResponseEntity.ok().build(); }

    // ===== Favorites =====
    @GetMapping("/api/favorites") public ResponseEntity<?> favorites() {
        return ResponseEntity.ok(favRepo.findByUsernameOrderByCreatedAtDesc(user()));
    }
    @PostMapping("/api/favorites/{productId}") public ResponseEntity<?> addFavorite(@PathVariable Long productId) {
        if (favRepo.existsByUsernameAndProductId(user(), productId))
            return ResponseEntity.ok(Map.of("success", true));
        UserFavorite f = new UserFavorite(); f.setUsername(user()); f.setProductId(productId);
        favRepo.save(f);
        return ResponseEntity.ok(Map.of("success", true));
    }
    @DeleteMapping("/api/favorites/{productId}") public ResponseEntity<?> removeFavorite(@PathVariable Long productId) {
        favRepo.deleteByUsernameAndProductId(user(), productId);
        return ResponseEntity.ok(Map.of("success", true));
    }
    @GetMapping("/api/favorites/{productId}/check") public ResponseEntity<?> checkFavorite(@PathVariable Long productId) {
        return ResponseEntity.ok(Map.of("favorited", favRepo.existsByUsernameAndProductId(user(), productId)));
    }

    // ===== Categories =====
    @GetMapping("/api/categories") public ResponseEntity<?> categories() { return ResponseEntity.ok(catRepo.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"))); }
    @PostMapping("/api/categories") public ResponseEntity<?> addCategory(@RequestBody ProductCategory c) { return ResponseEntity.ok(catRepo.save(c)); }
    @DeleteMapping("/api/categories/{id}") public ResponseEntity<?> delCategory(@PathVariable Long id) { catRepo.deleteById(id); return ResponseEntity.ok().build(); }

    // ===== Brands =====
    @GetMapping("/api/brands") public ResponseEntity<?> brands() { return ResponseEntity.ok(brandRepo.findAll()); }
    @PostMapping("/api/brands") public ResponseEntity<?> addBrand(@RequestBody ProductBrand b) { return ResponseEntity.ok(brandRepo.save(b)); }
    @DeleteMapping("/api/brands/{id}") public ResponseEntity<?> delBrand(@PathVariable Long id) { brandRepo.deleteById(id); return ResponseEntity.ok().build(); }

    // ===== SKU =====
    @GetMapping("/api/products/{id}/skus") public ResponseEntity<?> skus(@PathVariable Long id) { return ResponseEntity.ok(skuRepo.findByProductId(id)); }
    @PostMapping("/api/products/{id}/skus") public ResponseEntity<?> addSku(@PathVariable Long id, @RequestBody ProductSku sku) { sku.setProductId(id); return ResponseEntity.ok(skuRepo.save(sku)); }
    @DeleteMapping("/api/skus/{id}") public ResponseEntity<?> delSku(@PathVariable Long id) { skuRepo.deleteById(id); return ResponseEntity.ok().build(); }

    // ===== Reviews =====
    @GetMapping("/api/products/{id}/reviews") public ResponseEntity<?> reviews(@PathVariable Long id) { return ResponseEntity.ok(reviewRepo.findByProductIdOrderByCreatedAtDesc(id)); }
    @PostMapping("/api/products/{id}/reviews") public ResponseEntity<?> addReview(@PathVariable Long id, @RequestBody ProductReview r) {
        r.setProductId(id); r.setUsername(user()); return ResponseEntity.ok(reviewRepo.save(r));
    }

    // ===== Coupons (admin) =====
    @GetMapping("/api/coupons") public ResponseEntity<?> coupons() { return ResponseEntity.ok(couponRepo.findAll()); }
    @PostMapping("/api/coupons") public ResponseEntity<?> addCoupon(@RequestBody Coupon c) { return ResponseEntity.ok(couponRepo.save(c)); }
    @DeleteMapping("/api/coupons/{id}") public ResponseEntity<?> delCoupon(@PathVariable Long id) { couponRepo.deleteById(id); return ResponseEntity.ok().build(); }
    @GetMapping("/api/coupons/available") public ResponseEntity<?> availableCoupons() {
        return ResponseEntity.ok(couponRepo.findAll().stream().filter(c ->
            "0".equals(c.getStatus()) && c.getUsedQty() < c.getTotalQty() &&
            (c.getEndTime() == null || c.getEndTime().isAfter(LocalDateTime.now()))
        ).toList());
    }
    @PostMapping("/api/coupons/{id}/claim") public ResponseEntity<?> claim(@PathVariable Long id) {
        Coupon c = couponRepo.findById(id).orElse(null);
        if (c == null || c.getUsedQty() >= c.getTotalQty()) return ResponseEntity.badRequest().body(Map.of("message","已领完"));
        CouponUser cu = new CouponUser(); cu.setCouponId(id); cu.setUsername(user());
        couponUserRepo.save(cu);
        c.setUsedQty(c.getUsedQty() + 1); couponRepo.save(c);
        return ResponseEntity.ok(Map.of("success", true));
    }
    @GetMapping("/api/coupons/my") public ResponseEntity<?> myCoupons() {
        return ResponseEntity.ok(couponUserRepo.findByUsername(user()));
    }

    // ===== Shipments (admin) =====
    @GetMapping("/api/shipments") public ResponseEntity<?> shipments() { return ResponseEntity.ok(shipRepo.findAll()); }
    @GetMapping("/api/orders/{id}/shipment") public ResponseEntity<?> shipment(@PathVariable Long id) { return ResponseEntity.ok(shipRepo.findByOrderId(id)); }
    @PostMapping("/api/orders/{id}/ship") public ResponseEntity<?> ship(@PathVariable Long id, @RequestBody Map<String,String> body) {
        OrderShipment s = new OrderShipment(); s.setOrderId(id);
        s.setCarrier(body.get("carrier")); s.setTrackingNo(body.get("trackingNo"));
        s.setShippedAt(LocalDateTime.now());
        return ResponseEntity.ok(shipRepo.save(s));
    }

    // ===== Addresses =====
    @GetMapping("/api/addresses") public ResponseEntity<?> addresses() { return ResponseEntity.ok(addrRepo.findByUsernameOrderByIsDefaultDesc(user())); }
    @PostMapping("/api/addresses") public ResponseEntity<?> addAddress(@RequestBody UserAddress a) { a.setUsername(user()); return ResponseEntity.ok(addrRepo.save(a)); }
    @PutMapping("/api/addresses/{id}") public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody UserAddress body) {
        return addrRepo.findById(id).map(a -> {
            if (body.getName() != null) a.setName(body.getName());
            if (body.getPhone() != null) a.setPhone(body.getPhone());
            if (body.getProvince() != null) a.setProvince(body.getProvince());
            if (body.getCity() != null) a.setCity(body.getCity());
            if (body.getDistrict() != null) a.setDistrict(body.getDistrict());
            if (body.getDetail() != null) a.setDetail(body.getDetail());
            if (body.getIsDefault() != null) {
                if (Boolean.TRUE.equals(body.getIsDefault())) {
                    addrRepo.findByUsernameOrderByIsDefaultDesc(user()).stream()
                        .filter(UserAddress::getIsDefault).forEach(x -> { x.setIsDefault(false); addrRepo.save(x); });
                }
                a.setIsDefault(body.getIsDefault());
            }
            return ResponseEntity.ok(addrRepo.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/api/addresses/{id}") public ResponseEntity<?> delAddress(@PathVariable Long id) { addrRepo.deleteById(id); return ResponseEntity.ok().build(); }
}
