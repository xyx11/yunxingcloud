package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchOperationServiceTest {

    @Mock private OrderHeadRepository orderRepo;
    @InjectMocks private BatchOperationService service;

    @Test
    void shouldBatchShip() {
        OrderHead o = new OrderHead(); o.setId(1L); o.setStatus("1");
        when(orderRepo.findById(1L)).thenReturn(Optional.of(o));
        when(orderRepo.save(any())).thenReturn(o);

        Map<String, Object> result = service.batchShip(List.of(1), "SF Express");
        assertThat(result.get("shipped")).isEqualTo(1);
        assertThat(o.getStatus()).isEqualTo("2");
    }

    @Test
    void shouldSkipNonPaidOrdersInBatchShip() {
        OrderHead o = new OrderHead(); o.setId(2L); o.setStatus("0");
        when(orderRepo.findById(2L)).thenReturn(Optional.of(o));

        Map<String, Object> result = service.batchShip(List.of(2), "SF Express");
        assertThat(result.get("shipped")).isEqualTo(0);
    }

    @Test
    void shouldBatchCancel() {
        OrderHead o = new OrderHead(); o.setId(1L); o.setStatus("0");
        when(orderRepo.findById(1L)).thenReturn(Optional.of(o));
        when(orderRepo.save(any())).thenReturn(o);

        Map<String, Object> result = service.batchCancel(List.of(1), "库存不足");
        assertThat(result.get("canceled")).isEqualTo(1);
        assertThat(o.getStatus()).isEqualTo("4");
    }

    @Test
    void shouldSkipCompletedOrdersInCancel() {
        OrderHead o = new OrderHead(); o.setId(3L); o.setStatus("3");
        when(orderRepo.findById(3L)).thenReturn(Optional.of(o));

        Map<String, Object> result = service.batchCancel(List.of(3), "test");
        assertThat(result.get("canceled")).isEqualTo(0);
    }
}