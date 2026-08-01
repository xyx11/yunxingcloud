package com.yunxingcloud.order.service;

import com.yunxingcloud.api.client.InventoryClient;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock private OrderHeadRepository orderRepo;
    @Mock private OrderLineRepository lineRepo;
    @Mock private CartItemRepository cartRepo;
    @Mock private ProductRepository productRepo;
    @Mock private CouponRepository couponRepo;
    @Mock private CouponUserRepository couponUserRepo;
    @Mock private InventoryClient inventoryClient;
    @Mock private I18nService i18n;
    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private OrderService orderService;

    private List<CartItem> sampleCart;

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(1L);
        when(i18n.msg(anyString())).thenAnswer(inv -> {
            String key = inv.getArgument(0);
            return switch (key) {
                case "cart.empty" -> "购物车为空";
                case "order.stock_insufficient" -> "库存不足";
                case "coupon.unavailable" -> "优惠券不可用";
                default -> key;
            };
        });
        when(i18n.msg(anyString(), any())).thenAnswer(inv -> {
            String key = inv.getArgument(0);
            return switch (key) {
                case "coupon.below_threshold" -> "未达到最低消费";
                default -> key;
            };
        });

        CartItem c1 = new CartItem();
        c1.setId(1L); c1.setProductId(1L); c1.setProductName("商品A");
        c1.setPrice(9900L); c1.setQuantity(2);
        CartItem c2 = new CartItem();
        c2.setId(2L); c2.setProductId(2L); c2.setProductName("商品B");
        c2.setPrice(5000L); c2.setQuantity(1);
        sampleCart = List.of(c1, c2);
    }

    @Test
    void shouldSubmitOrder() {
        when(cartRepo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(sampleCart);
        when(orderRepo.save(any())).thenAnswer(inv -> { OrderHead o = inv.getArgument(0); o.setId(100L); return o; });

        OrderHead result = orderService.submit("user1", Map.of("name", "张三", "phone", "13800138000", "address", "北京市"));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTotalAmount()).isEqualTo(24800L);
        assertThat(result.getActualAmount()).isEqualTo(24800L);
        verify(cartRepo).deleteByUsername("user1");
        verify(lineRepo, times(2)).save(any());
    }

    @Test
    void shouldThrowWhenCartEmpty() {
        when(cartRepo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(List.of());
        assertThatThrownBy(() -> orderService.submit("user1", Map.of())).isInstanceOf(IllegalStateException.class).hasMessageContaining("购物车为空");
    }

    @Test
    void shouldThrowWhenCouponUnavailable() {
        CouponUser uc = new CouponUser(); uc.setCouponId(10L); uc.setStatus("1");
        when(cartRepo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(sampleCart);
        when(couponUserRepo.findById(99L)).thenReturn(Optional.of(uc));

        assertThatThrownBy(() -> orderService.submit("user1", Map.of(), 99L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("不可用");
    }

    @Test
    void shouldThrowWhenCouponThresholdNotMet() {
        CouponUser uc = new CouponUser(); uc.setCouponId(10L); uc.setStatus("0");
        Coupon coupon = new Coupon(); coupon.setId(10L); coupon.setThreshold(100000L); coupon.setAmount(1000L);
        when(cartRepo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(sampleCart);
        when(couponUserRepo.findById(99L)).thenReturn(Optional.of(uc));
        when(couponRepo.findById(10L)).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> orderService.submit("user1", Map.of(), 99L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("最低消费");
    }

    @Test
    void shouldApplyCouponToOrder() {
        CouponUser uc = new CouponUser(); uc.setCouponId(10L); uc.setStatus("0");
        Coupon coupon = new Coupon(); coupon.setId(10L); coupon.setThreshold(20000L); coupon.setAmount(5000L);
        when(cartRepo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(sampleCart);
        when(couponUserRepo.findById(99L)).thenReturn(Optional.of(uc));
        when(couponRepo.findById(10L)).thenReturn(Optional.of(coupon));
        when(orderRepo.save(any())).thenAnswer(inv -> { OrderHead o = inv.getArgument(0); o.setId(100L); return o; });

        OrderHead result = orderService.submit("user1", Map.of("name", "张三", "phone", "13800138000", "address", "北京市"), 99L);

        assertThat(result.getCouponAmount()).isEqualTo(5000L);
        assertThat(result.getActualAmount()).isEqualTo(24800L - 5000L);
        verify(couponUserRepo).save(argThat(cu -> "1".equals(cu.getStatus())));
    }

    @Test
    void shouldCancelOrderAndRestoreCoupon() {
        CouponUser uc = new CouponUser(); uc.setId(1L); uc.setStatus("1"); uc.setCouponId(10L);
        OrderHead order = new OrderHead(); order.setId(100L); order.setStatus("0"); order.setCouponId(1L);
        order.setUsername("user1"); order.setTotalAmount(9900L); order.setActualAmount(9900L);

        when(orderRepo.findById(100L)).thenReturn(Optional.of(order));
        when(lineRepo.findByOrderId(100L)).thenReturn(List.of());
        when(couponUserRepo.findById(1L)).thenReturn(Optional.of(uc));

        orderService.cancelOrder(order);

        verify(orderRepo).save(argThat(o -> "4".equals(o.getStatus())));
        verify(couponUserRepo).save(argThat(cu -> "0".equals(cu.getStatus())));
    }
}
