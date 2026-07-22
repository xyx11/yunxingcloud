package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

    @Mock private OrderHeadRepository orderRepo;
    @InjectMocks private RevenueService service;

    @Test
    void shouldReturnOverview() {
        when(orderRepo.totalRevenue()).thenReturn(100000L);
        when(orderRepo.paidRevenue()).thenReturn(80000L);
        when(orderRepo.totalOrderCount()).thenReturn(50L);
        when(orderRepo.paidOrderCount()).thenReturn(40L);

        Map<String, Object> result = service.overview();
        assertThat(result.get("totalRevenue")).isEqualTo(100000L);
        assertThat(result.get("paidRevenue")).isEqualTo(80000L);
        assertThat(result.get("conversionRate")).isEqualTo(80L);
    }

    @Test
    void shouldReturnDailyRevenue() {
        OrderHead o = new OrderHead(); o.setId(1L); o.setTotalAmount(9900L);
        o.setCreatedAt(LocalDateTime.now());
        when(orderRepo.findAll()).thenReturn(List.of(o));

        List<Map<String, Object>> result = service.dailyRevenue();
        assertThat(result).hasSize(30);
    }
}