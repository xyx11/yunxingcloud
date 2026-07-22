package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.PriceAlert;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.PriceAlertRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PriceAlertServiceTest {

    @Mock private PriceAlertRepository alertRepo;
    @Mock private ProductRepository productRepo;
    @InjectMocks private PriceAlertService service;

    @BeforeEach
    void setUp() {
        when(alertRepo.existsByUsernameAndProductId(anyString(), anyLong())).thenReturn(false);
        when(alertRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void shouldCreateAlert() {
        PriceAlert result = service.create("user1", 100L, 5000L);

        assertThat(result.getUsername()).isEqualTo("user1");
        assertThat(result.getProductId()).isEqualTo(100L);
        assertThat(result.getTargetPrice()).isEqualTo(5000L);
        verify(alertRepo).save(any());
    }

    @Test
    void shouldRejectDuplicateAlert() {
        when(alertRepo.existsByUsernameAndProductId("user1", 100L)).thenReturn(true);

        assertThatThrownBy(() -> service.create("user1", 100L, 5000L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("已设置提醒");
    }

    @Test
    void shouldListAlerts() {
        PriceAlert a = new PriceAlert();
        a.setId(1L); a.setUsername("user1"); a.setProductId(100L); a.setTargetPrice(5000L);
        when(alertRepo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(List.of(a));

        List<PriceAlert> result = service.list("user1");
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldCheckAndNotifyWhenPriceDrops() {
        PriceAlert a = new PriceAlert();
        a.setId(1L); a.setUsername("user1"); a.setProductId(100L); a.setTargetPrice(5000L);
        when(alertRepo.findByUsernameAndNotifiedFalse("user1")).thenReturn(List.of(a));
        Product p = new Product();
        p.setId(100L); p.setName("Test"); p.setPrice(4000L);
        when(productRepo.findById(100L)).thenReturn(Optional.of(p));

        List<Map<String, Object>> triggered = service.checkAndNotify("user1");

        assertThat(triggered).hasSize(1);
        assertThat(triggered.get(0).get("productName")).isEqualTo("Test");
        verify(alertRepo).save(argThat(al -> al.getNotified() != null && al.getNotified()));
    }

    @Test
    void shouldNotNotifyWhenPriceNotDropped() {
        PriceAlert a = new PriceAlert();
        a.setId(1L); a.setUsername("user1"); a.setProductId(100L); a.setTargetPrice(5000L);
        when(alertRepo.findByUsernameAndNotifiedFalse("user1")).thenReturn(List.of(a));
        Product p = new Product();
        p.setId(100L); p.setPrice(6000L);
        when(productRepo.findById(100L)).thenReturn(Optional.of(p));

        List<Map<String, Object>> triggered = service.checkAndNotify("user1");
        assertThat(triggered).isEmpty();
    }
}