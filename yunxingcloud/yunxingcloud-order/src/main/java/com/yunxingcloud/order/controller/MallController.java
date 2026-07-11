package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.dto.BrandDTO;
import com.yunxingcloud.order.dto.CategoryDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.yunxingcloud.order.dto.ProductReviewDTO;
import com.yunxingcloud.order.dto.ProductSkuDTO;
import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.service.MallService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "商城前台", description = "商城首页/分类/品牌/优惠券/收藏/地址等")
@RestController
public class MallController {

    private final MallService mallService;

    public MallController(MallService mallService) {
        this.mallService = mallService;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping("/api/home")
    public ResponseEntity<?> home() { return ResponseEntity.ok(mallService.home()); }

    // ===== Banners =====
    @GetMapping("/api/banners") public ResponseEntity<?> banners() { return ResponseEntity.ok(mallService.listBanners()); }
    @PostMapping("/api/banners") public ResponseEntity<?> addBanner(@RequestBody Banner b) { return ResponseEntity.ok(mallService.createBanner(b)); }
    @PutMapping("/api/banners/{id}") public ResponseEntity<?> updateBanner(@PathVariable Long id, @RequestBody Banner body) {
        return mallService.updateBanner(id, body).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/api/banners/{id}") public ResponseEntity<?> delBanner(@PathVariable Long id) { mallService.deleteBanner(id); return ResponseEntity.ok().build(); }

    // ===== Favorites =====
    @GetMapping("/api/favorites") public ResponseEntity<?> favorites() { return ResponseEntity.ok(mallService.listFavorites(user())); }
    @PostMapping("/api/favorites/{productId}") public ResponseEntity<?> addFavorite(@PathVariable Long productId) {
        mallService.addFavorite(user(), productId); return ResponseEntity.ok(Map.of("success", true));
    }
    @DeleteMapping("/api/favorites/{productId}") public ResponseEntity<?> removeFavorite(@PathVariable Long productId) {
        mallService.removeFavorite(user(), productId); return ResponseEntity.ok(Map.of("success", true));
    }
    @GetMapping("/api/favorites/{productId}/check") public ResponseEntity<?> checkFavorite(@PathVariable Long productId) {
        return ResponseEntity.ok(Map.of("favorited", mallService.isFavorited(user(), productId)));
    }

    // ===== Categories =====
    @GetMapping("/api/categories") public ResponseEntity<?> categories() { return ResponseEntity.ok(mallService.listCategories()); }
    @PostMapping("/api/categories") public ResponseEntity<?> addCategory(@Valid @RequestBody CategoryDTO dto) { return ResponseEntity.ok(mallService.createCategory(dto)); }
    @PutMapping("/api/categories/{id}") public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        return mallService.updateCategory(id, dto).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/api/categories/{id}") public ResponseEntity<?> delCategory(@PathVariable Long id) { mallService.deleteCategory(id); return ResponseEntity.ok().build(); }

    // ===== Brands =====
    @GetMapping("/api/brands") public ResponseEntity<?> brands() { return ResponseEntity.ok(mallService.listBrands()); }
    @PostMapping("/api/brands") public ResponseEntity<?> addBrand(@Valid @RequestBody BrandDTO dto) { return ResponseEntity.ok(mallService.createBrand(dto)); }
    @PutMapping("/api/brands/{id}") public ResponseEntity<?> updateBrand(@PathVariable Long id, @Valid @RequestBody BrandDTO dto) {
        return mallService.updateBrand(id, dto).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/api/brands/{id}") public ResponseEntity<?> delBrand(@PathVariable Long id) { mallService.deleteBrand(id); return ResponseEntity.ok().build(); }

    // ===== SKU =====
    @GetMapping("/api/products/skus/all") public ResponseEntity<?> allSkus(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return ResponseEntity.ok(mallService.listAllSkus(page, size)); }
    @GetMapping("/api/products/{id}/skus") public ResponseEntity<?> skus(@PathVariable Long id) { return ResponseEntity.ok(mallService.listSkusByProduct(id)); }
    @PostMapping("/api/products/{id}/skus") public ResponseEntity<?> addSku(@PathVariable Long id, @Valid @RequestBody ProductSkuDTO dto) { return ResponseEntity.ok(mallService.createSku(id, dto)); }
    @DeleteMapping("/api/skus/{id}") public ResponseEntity<?> delSku(@PathVariable Long id) { mallService.deleteSku(id); return ResponseEntity.ok().build(); }

    // ===== Reviews =====
    @GetMapping("/api/products/{id}/reviews") public ResponseEntity<?> reviews(@PathVariable Long id) { return ResponseEntity.ok(mallService.listReviewsByProduct(id)); }
    @GetMapping("/api/products/reviews/all") public ResponseEntity<?> allReviews(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return ResponseEntity.ok(mallService.listAllReviews(page, size)); }
    @DeleteMapping("/api/products/reviews/{id}") public ResponseEntity<?> delReview(@PathVariable Long id) { mallService.deleteReview(id); return ResponseEntity.ok(Map.of("success", true)); }
    @PostMapping("/api/products/{id}/reviews") public ResponseEntity<?> addReview(@PathVariable Long id, @Valid @RequestBody ProductReviewDTO dto) {
        return ResponseEntity.ok(mallService.createReview(id, user(), dto));
    }

    // ===== Coupons =====
    @GetMapping("/api/coupons") public ResponseEntity<?> coupons() { return ResponseEntity.ok(mallService.listCoupons()); }
    @PostMapping("/api/coupons") public ResponseEntity<?> addCoupon(@RequestBody Coupon c) { return ResponseEntity.ok(mallService.createCoupon(c)); }
    @DeleteMapping("/api/coupons/{id}") public ResponseEntity<?> delCoupon(@PathVariable Long id) { mallService.deleteCoupon(id); return ResponseEntity.ok().build(); }
    @GetMapping("/api/coupons/available") public ResponseEntity<?> availableCoupons() { return ResponseEntity.ok(mallService.listAvailableCoupons()); }
    @PostMapping("/api/coupons/{id}/claim") public ResponseEntity<?> claim(@PathVariable Long id) {
        String error = mallService.claimCoupon(id, user());
        if (error != null) return ResponseEntity.badRequest().body(Map.of("message", error));
        return ResponseEntity.ok(Map.of("success", true));
    }
    @GetMapping("/api/coupons/my") public ResponseEntity<?> myCoupons() { return ResponseEntity.ok(mallService.listMyCoupons(user())); }

    // ===== Shipments =====
    @GetMapping("/api/shipments/all") public ResponseEntity<?> allShipments(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return ResponseEntity.ok(mallService.listAllShipments(page, size)); }
    @GetMapping("/api/shipments") public ResponseEntity<?> shipments() { return ResponseEntity.ok(mallService.listShipments()); }
    @GetMapping("/api/orders/{id}/shipment") public ResponseEntity<?> shipment(@PathVariable Long id) { return ResponseEntity.ok(mallService.getShipmentByOrder(id)); }
    @PostMapping("/api/orders/{id}/ship") public ResponseEntity<?> ship(@PathVariable Long id, @RequestBody Map<String,String> body) {
        return ResponseEntity.ok(mallService.ship(id, body.get("carrier"), body.get("trackingNo")));
    }

    // ===== Addresses =====
    @GetMapping("/api/addresses") public ResponseEntity<?> addresses() { return ResponseEntity.ok(mallService.listAddresses(user())); }
    @PostMapping("/api/addresses") public ResponseEntity<?> addAddress(@RequestBody UserAddress a) { return ResponseEntity.ok(mallService.createAddress(user(), a)); }
    @PutMapping("/api/addresses/{id}") public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody UserAddress body) {
        return mallService.updateAddress(user(), id, body).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/api/addresses/{id}") public ResponseEntity<?> delAddress(@PathVariable Long id) { mallService.deleteAddress(id); return ResponseEntity.ok().build(); }
}
