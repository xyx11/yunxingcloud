package com.yunxingcloud.order.service;

import com.yunxingcloud.order.dto.BrandDTO;
import com.yunxingcloud.order.dto.CategoryDTO;
import com.yunxingcloud.order.dto.ProductReviewDTO;
import com.yunxingcloud.order.dto.ProductSkuDTO;
import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MallServiceTest {

    @Mock private ProductCategoryRepository catRepo;
    @Mock private ProductBrandRepository brandRepo;
    @Mock private ProductSkuRepository skuRepo;
    @Mock private ProductReviewRepository reviewRepo;
    @Mock private CouponRepository couponRepo;
    @Mock private CouponUserRepository couponUserRepo;
    @Mock private OrderShipmentRepository shipRepo;
    @Mock private UserAddressRepository addrRepo;
    @Mock private BannerRepository bannerRepo;
    @Mock private ProductRepository productRepo;
    @Mock private ProductCacheService cacheService;
    @Mock private UserFavoriteRepository favRepo;
    @Mock private Page<ProductSku> mockSkuPage;
    @Mock private Page<ProductReview> mockReviewPage;
    @Mock private Page<OrderShipment> mockShipmentPage;

    @InjectMocks private MallService mallService;

    /* ============== Home ============== */

    @Test
    void shouldReturnHomeData() {
        when(productRepo.findByStatus(eq("0"), any(Pageable.class)))
                .thenReturn(List.of());
        when(cacheService.getBanners()).thenReturn(List.of());
        when(cacheService.getHotProducts()).thenReturn(List.of());
        when(cacheService.getNewProducts()).thenReturn(List.of());
        when(cacheService.getCategories()).thenReturn(List.of());

        Map<String, Object> result = mallService.home();

        assertThat(result).containsOnlyKeys("banners", "hotProducts", "newProducts", "categories", "recommended");
    }

    /* ============== Banners ============== */

    @Test
    void shouldListBanners() {
        when(bannerRepo.findByStatus(eq("0"), any(Sort.class)))
                .thenReturn(List.of());

        assertThat(mallService.listBanners()).isEmpty();
    }

    @Test
    void shouldCreateBanner() {
        Banner b = new Banner();
        b.setTitle("测试横幅");
        when(bannerRepo.save(b)).thenReturn(b);

        Banner result = mallService.createBanner(b);

        assertThat(result.getTitle()).isEqualTo("测试横幅");
        verify(cacheService).evictCatalog();
    }

    @Test
    void shouldDeleteBanner() {
        mallService.deleteBanner(1L);

        verify(bannerRepo).deleteById(1L);
        verify(cacheService).evictCatalog();
    }

    /* ============== Favorites ============== */

    @Test
    void shouldListFavorites() {
        UserFavorite fav = new UserFavorite();
        fav.setProductId(1L);
        when(favRepo.findByUsernameOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of(fav));

        List<UserFavorite> result = mallService.listFavorites("user1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(1L);
    }

    @Test
    void shouldAddFavorite() {
        when(favRepo.existsByUsernameAndProductId("user1", 1L)).thenReturn(false);

        mallService.addFavorite("user1", 1L);

        verify(favRepo).save(any(UserFavorite.class));
    }

    @Test
    void shouldNotAddDuplicateFavorite() {
        when(favRepo.existsByUsernameAndProductId("user1", 1L)).thenReturn(true);

        mallService.addFavorite("user1", 1L);

        verify(favRepo, never()).save(any());
    }

    @Test
    void shouldRemoveFavorite() {
        mallService.removeFavorite("user1", 1L);

        verify(favRepo).deleteByUsernameAndProductId("user1", 1L);
    }

    @Test
    void shouldCheckIsFavorited() {
        when(favRepo.existsByUsernameAndProductId("user1", 1L)).thenReturn(true);

        assertThat(mallService.isFavorited("user1", 1L)).isTrue();
    }

    /* ============== Categories ============== */

    @Test
    void shouldListCategories() {
        when(catRepo.findAll(any(Sort.class))).thenReturn(List.of());

        assertThat(mallService.listCategories()).isEmpty();
    }

    @Test
    void shouldCreateCategory() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("电子产品");
        dto.setIcon("icon.png");
        dto.setSortOrder(1);
        dto.setStatus("0");
        dto.setParentId(0L);

        ProductCategory saved = new ProductCategory();
        saved.setId(1L);
        saved.setName("电子产品");
        when(catRepo.save(any())).thenReturn(saved);

        ProductCategory result = mallService.createCategory(dto);

        assertThat(result.getName()).isEqualTo("电子产品");
        verify(cacheService).evictCatalog();
    }

    @Test
    void shouldDeleteCategory() {
        mallService.deleteCategory(1L);

        verify(catRepo).deleteById(1L);
        verify(cacheService).evictCatalog();
    }

    /* ============== Brands ============== */

    @Test
    void shouldListBrands() {
        when(brandRepo.findAll()).thenReturn(List.of());

        assertThat(mallService.listBrands()).isEmpty();
    }

    @Test
    void shouldCreateBrand() {
        BrandDTO dto = new BrandDTO();
        dto.setName("苹果");
        dto.setLogo("apple.png");

        ProductBrand saved = new ProductBrand();
        saved.setId(1L);
        saved.setName("苹果");
        when(brandRepo.save(any())).thenReturn(saved);

        ProductBrand result = mallService.createBrand(dto);

        assertThat(result.getName()).isEqualTo("苹果");
        verify(cacheService).evictCatalog();
    }

    @Test
    void shouldDeleteBrand() {
        mallService.deleteBrand(1L);

        verify(brandRepo).deleteById(1L);
        verify(cacheService).evictCatalog();
    }

    /* ============== SKUs ============== */

    @Test
    void shouldListAllSkus() {
        when(skuRepo.findAll(any(Pageable.class))).thenReturn(mockSkuPage);

        assertThat(mallService.listAllSkus(0, 10)).isSameAs(mockSkuPage);
    }

    @Test
    void shouldListSkusByProduct() {
        when(skuRepo.findByProductId(1L)).thenReturn(List.of());

        assertThat(mallService.listSkusByProduct(1L)).isEmpty();
    }

    @Test
    void shouldCreateSku() {
        ProductSkuDTO dto = new ProductSkuDTO();
        dto.setName("红色");
        dto.setPrice(10000L);
        dto.setStock(10);
        dto.setSkuCode("R-001");

        ProductSku saved = new ProductSku();
        saved.setId(1L);
        saved.setProductId(1L);
        saved.setName("红色");
        when(skuRepo.save(any())).thenReturn(saved);

        ProductSku result = mallService.createSku(1L, dto);

        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("红色");
    }

    @Test
    void shouldDeleteSku() {
        mallService.deleteSku(1L);

        verify(skuRepo).deleteById(1L);
    }

    /* ============== Reviews ============== */

    @Test
    void shouldListReviewsByProduct() {
        when(reviewRepo.findByProductIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        assertThat(mallService.listReviewsByProduct(1L)).isEmpty();
    }

    @Test
    void shouldListAllReviews() {
        when(reviewRepo.findAll(any(Pageable.class))).thenReturn(mockReviewPage);

        assertThat(mallService.listAllReviews(0, 10)).isSameAs(mockReviewPage);
    }

    @Test
    void shouldCreateReview() {
        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setRating(5);
        dto.setContent("很好");

        ProductReview saved = new ProductReview();
        saved.setId(1L);
        saved.setProductId(1L);
        saved.setUsername("user1");
        saved.setRating(5);
        when(reviewRepo.save(any())).thenReturn(saved);

        ProductReview result = mallService.createReview(1L, "user1", dto);

        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("user1");
        assertThat(result.getRating()).isEqualTo(5);
    }

    @Test
    void shouldDeleteReview() {
        mallService.deleteReview(1L);

        verify(reviewRepo).deleteById(1L);
    }

    /* ============== Coupons ============== */

    @Test
    void shouldListCoupons() {
        Coupon c = new Coupon();
        c.setId(1L);
        when(couponRepo.findAll()).thenReturn(List.of(c));

        assertThat(mallService.listCoupons()).hasSize(1);
    }

    @Test
    void shouldListAvailableCoupons() {
        Coupon available = new Coupon();
        available.setId(1L);
        available.setStatus("0");
        available.setTotalQty(100);
        available.setUsedQty(0);
        available.setEndTime(LocalDateTime.now().plusDays(1));

        Coupon expired = new Coupon();
        expired.setId(2L);
        expired.setStatus("0");
        expired.setTotalQty(100);
        expired.setUsedQty(0);
        expired.setEndTime(LocalDateTime.now().minusDays(1));

        Coupon full = new Coupon();
        full.setId(3L);
        full.setStatus("0");
        full.setTotalQty(100);
        full.setUsedQty(100);

        Coupon inactive = new Coupon();
        inactive.setId(4L);
        inactive.setStatus("1");

        when(couponRepo.findAll()).thenReturn(List.of(available, expired, full, inactive));

        List<Coupon> result = mallService.listAvailableCoupons();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void shouldClaimCouponSuccessfully() {
        Coupon c = new Coupon();
        c.setId(1L);
        c.setStatus("0");
        c.setTotalQty(100);
        c.setUsedQty(0);
        when(couponRepo.findById(1L)).thenReturn(Optional.of(c));
        when(couponUserRepo.existsByCouponIdAndUsername(1L, "user1")).thenReturn(false);

        String result = mallService.claimCoupon(1L, "user1");

        assertThat(result).isNull();
        verify(couponUserRepo).save(any(CouponUser.class));
        verify(couponRepo).save(c);
        assertThat(c.getUsedQty()).isEqualTo(1);
    }

    @Test
    void shouldReturnErrorWhenCouponNotFound() {
        when(couponRepo.findById(99L)).thenReturn(Optional.empty());

        assertThat(mallService.claimCoupon(99L, "user1")).isEqualTo("优惠券不存在");
    }

    @Test
    void shouldReturnErrorWhenCouponInactive() {
        Coupon c = new Coupon();
        c.setId(1L);
        c.setStatus("1");
        when(couponRepo.findById(1L)).thenReturn(Optional.of(c));

        assertThat(mallService.claimCoupon(1L, "user1")).isEqualTo("优惠券已失效");
    }

    @Test
    void shouldReturnErrorWhenCouponExpired() {
        Coupon c = new Coupon();
        c.setId(1L);
        c.setStatus("0");
        c.setEndTime(LocalDateTime.now().minusDays(1));
        when(couponRepo.findById(1L)).thenReturn(Optional.of(c));

        assertThat(mallService.claimCoupon(1L, "user1")).isEqualTo("优惠券已过期");
    }

    @Test
    void shouldReturnErrorWhenCouponNotStarted() {
        Coupon c = new Coupon();
        c.setId(1L);
        c.setStatus("0");
        c.setStartTime(LocalDateTime.now().plusDays(1));
        when(couponRepo.findById(1L)).thenReturn(Optional.of(c));

        assertThat(mallService.claimCoupon(1L, "user1")).isEqualTo("优惠券未开始");
    }

    @Test
    void shouldReturnErrorWhenCouponFullyClaimed() {
        Coupon c = new Coupon();
        c.setId(1L);
        c.setStatus("0");
        c.setTotalQty(100);
        c.setUsedQty(100);
        when(couponRepo.findById(1L)).thenReturn(Optional.of(c));

        assertThat(mallService.claimCoupon(1L, "user1")).isEqualTo("已领完");
    }

    @Test
    void shouldReturnErrorWhenAlreadyClaimed() {
        Coupon c = new Coupon();
        c.setId(1L);
        c.setStatus("0");
        c.setTotalQty(100);
        c.setUsedQty(0);
        when(couponRepo.findById(1L)).thenReturn(Optional.of(c));
        when(couponUserRepo.existsByCouponIdAndUsername(1L, "user1")).thenReturn(true);

        assertThat(mallService.claimCoupon(1L, "user1")).isEqualTo("已领取过");
    }

    @Test
    void shouldListMyCoupons() {
        CouponUser cu = new CouponUser();
        cu.setCouponId(1L);
        cu.setUsername("user1");
        when(couponUserRepo.findByUsername("user1")).thenReturn(List.of(cu));

        List<CouponUser> result = mallService.listMyCoupons("user1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCouponId()).isEqualTo(1L);
    }

    /* ============== Shipments ============== */

    @Test
    void shouldListAllShipments() {
        when(shipRepo.findAll(any(Pageable.class))).thenReturn(mockShipmentPage);

        assertThat(mallService.listAllShipments(0, 10)).isSameAs(mockShipmentPage);
    }

    @Test
    void shouldListShipments() {
        when(shipRepo.findAll()).thenReturn(List.of());

        assertThat(mallService.listShipments()).isEmpty();
    }

    @Test
    void shouldGetShipmentByOrder() {
        OrderShipment s = new OrderShipment();
        s.setOrderId(1L);
        when(shipRepo.findByOrderId(1L)).thenReturn(s);

        assertThat(mallService.getShipmentByOrder(1L).getOrderId()).isEqualTo(1L);
    }

    @Test
    void shouldCreateShipment() {
        when(shipRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderShipment result = mallService.ship(1L, "顺丰", "SF123456");

        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getCarrier()).isEqualTo("顺丰");
        assertThat(result.getTrackingNo()).isEqualTo("SF123456");
        assertThat(result.getShippedAt()).isNotNull();
    }

    /* ============== Addresses ============== */

    @Test
    void shouldListAddresses() {
        UserAddress addr = new UserAddress();
        addr.setId(1L);
        addr.setName("张三");
        when(addrRepo.findByUsernameOrderByIsDefaultDesc("user1"))
                .thenReturn(List.of(addr));

        List<UserAddress> result = mallService.listAddresses("user1");

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldCreateAddress() {
        UserAddress addr = new UserAddress();
        addr.setName("张三");
        addr.setPhone("13800138000");

        UserAddress saved = new UserAddress();
        saved.setId(1L);
        saved.setUsername("user1");
        saved.setName("张三");
        when(addrRepo.save(any())).thenReturn(saved);

        UserAddress result = mallService.createAddress("user1", addr);

        assertThat(result.getUsername()).isEqualTo("user1");
    }

    @Test
    void shouldUpdateAddress() {
        UserAddress existing = new UserAddress();
        existing.setId(1L);
        existing.setUsername("user1");
        existing.setName("旧姓名");
        existing.setPhone("13800138000");
        existing.setIsDefault(false);

        UserAddress body = new UserAddress();
        body.setName("新姓名");
        body.setPhone("13900139000");

        when(addrRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(addrRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<UserAddress> result = mallService.updateAddress("user1", 1L, body);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("新姓名");
        assertThat(result.get().getPhone()).isEqualTo("13900139000");
        assertThat(result.get().getIsDefault()).isFalse();
        verify(addrRepo).save(existing);
    }

    @Test
    void shouldUpdateAddressWithDefaultManagement() {
        UserAddress existing = new UserAddress();
        existing.setId(1L);
        existing.setUsername("user1");
        existing.setIsDefault(false);

        UserAddress oldDefault = new UserAddress();
        oldDefault.setId(2L);
        oldDefault.setUsername("user1");
        oldDefault.setIsDefault(true);

        UserAddress body = new UserAddress();
        body.setIsDefault(true);

        when(addrRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(addrRepo.findByUsernameOrderByIsDefaultDesc("user1"))
                .thenReturn(List.of(oldDefault, existing));
        when(addrRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<UserAddress> result = mallService.updateAddress("user1", 1L, body);

        assertThat(result).isPresent();
        assertThat(result.get().getIsDefault()).isTrue();
        assertThat(oldDefault.getIsDefault()).isFalse();
        verify(addrRepo, times(2)).save(any());
    }

    @Test
    void shouldReturnEmptyWhenAddressNotFound() {
        when(addrRepo.findById(99L)).thenReturn(Optional.empty());

        assertThat(mallService.updateAddress("user1", 99L, new UserAddress()))
                .isNotPresent();
    }

    @Test
    void shouldDeleteAddress() {
        mallService.deleteAddress(1L);

        verify(addrRepo).deleteById(1L);
    }
}
