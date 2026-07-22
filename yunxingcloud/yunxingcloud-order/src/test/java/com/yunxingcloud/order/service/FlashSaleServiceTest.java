package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.FlashSale;
import com.yunxingcloud.order.repository.FlashSaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FlashSaleServiceTest {

    @Mock private FlashSaleRepository flashRepo;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @InjectMocks private FlashSaleService flashSaleService;

    private FlashSale activeFlash;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        activeFlash = new FlashSale();
        activeFlash.setId(1L);
        activeFlash.setProductId(100L);
        activeFlash.setFlashPrice(5000L);
        activeFlash.setStock(10);
        activeFlash.setStartTime(LocalDateTime.now().minusHours(1));
        activeFlash.setEndTime(LocalDateTime.now().plusHours(1));
    }

    @Test
    void shouldAllowBuyWhenStockAvailable() {
        when(flashRepo.findById(1L)).thenReturn(Optional.of(activeFlash));
        when(valueOps.decrement("flash:stock:1")).thenReturn(9L);

        boolean result = flashSaleService.tryBuy(1L, 100L, "user1");

        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectWhenOutOfStock() {
        activeFlash.setStock(0);
        when(flashRepo.findById(1L)).thenReturn(Optional.of(activeFlash));
        when(valueOps.decrement("flash:stock:1")).thenReturn(-1L);

        assertThatThrownBy(() -> flashSaleService.tryBuy(1L, 100L, "user1")).isInstanceOf(IllegalStateException.class).hasMessageContaining("售罄");
    }

    @Test
    void shouldRejectBeforeStartTime() {
        activeFlash.setStartTime(LocalDateTime.now().plusHours(2));
        activeFlash.setEndTime(LocalDateTime.now().plusHours(4));
        when(flashRepo.findById(1L)).thenReturn(Optional.of(activeFlash));

        assertThatThrownBy(() -> flashSaleService.tryBuy(1L, 100L, "user1")).isInstanceOf(IllegalStateException.class).hasMessageContaining("未开始");
    }

    @Test
    void shouldRejectAfterEndTime() {
        activeFlash.setStartTime(LocalDateTime.now().minusHours(2));
        activeFlash.setEndTime(LocalDateTime.now().minusHours(1));
        when(flashRepo.findById(1L)).thenReturn(Optional.of(activeFlash));

        assertThatThrownBy(() -> flashSaleService.tryBuy(1L, 100L, "user1")).isInstanceOf(IllegalStateException.class).hasMessageContaining("已结束");
    }

    @Test
    void shouldEnforcePerUserLimit() {
        activeFlash.setLimitPerUser(1);
        when(flashRepo.findById(1L)).thenReturn(Optional.of(activeFlash));
        when(valueOps.increment("flash:user:1:user1")).thenReturn(2L);

        assertThatThrownBy(() -> flashSaleService.tryBuy(1L, 100L, "user1")).isInstanceOf(IllegalStateException.class).hasMessageContaining("限购");
    }
}
