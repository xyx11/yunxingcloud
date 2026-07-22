package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private OrderHeadRepository orderRepo;
    @Mock private ProductRepository productRepo;
    @InjectMocks private AnalyticsService service;

    @Test
    void shouldReturnSalesOverview() {
        when(orderRepo.count()).thenReturn(100L);
        when(productRepo.count()).thenReturn(500L);

        Map<String, Object> result = service.salesOverview();
        assertThat(result.get("totalOrders")).isEqualTo(100L);
        assertThat(result.get("totalProducts")).isEqualTo(500L);
    }

    @Test
    void shouldReturnOrderTrend() {
        List<Map<String, Object>> result = service.orderTrend();
        assertThat(result).hasSize(7);
    }

    @Test
    void shouldReturnTopProducts() {
        Product p = new Product(); p.setId(1L); p.setName("热卖"); p.setSales(1000);
        p.setPrice(9900L);
        when(productRepo.findByStatus(any(), any(PageRequest.class))).thenReturn(List.of(p));

        List<Map<String, Object>> result = service.topProducts(5);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("name")).isEqualTo("热卖");
    }

    @Test
    void shouldReturnWeeklyStats() {
        Map<String, Object> result = service.weeklyStats();
        assertThat(result).containsKeys("weekStart", "weekEnd", "dailyStats");
    }

    @Test
    void shouldReturnDashboard() {
        when(orderRepo.count()).thenReturn(100L);
        when(productRepo.count()).thenReturn(500L);
        when(productRepo.findByStatus(any(), any(PageRequest.class))).thenReturn(List.of());

        Map<String, Object> result = service.dashboard();
        assertThat(result).containsKeys("totalOrders", "totalProducts", "topProducts", "weekly");
    }
}