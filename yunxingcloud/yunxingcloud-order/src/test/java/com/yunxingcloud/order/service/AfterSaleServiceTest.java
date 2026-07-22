package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.AfterSale;
import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.AfterSaleRepository;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfterSaleServiceTest {

    @Mock private AfterSaleRepository afterSaleRepo;
    @Mock private OrderHeadRepository orderRepo;
    @InjectMocks private AfterSaleService afterSaleService;

    private OrderHead paidOrder;

    @BeforeEach
    void setUp() {
        paidOrder = new OrderHead();
        paidOrder.setId(100L);
        paidOrder.setOrderNo("ORD-TEST-001");
        paidOrder.setUsername("user1");
        paidOrder.setStatus("1");
        paidOrder.setTotalAmount(9900L);
    }

    @Test
    void shouldCreateRefundRequest() {
        when(orderRepo.findById(100L)).thenReturn(Optional.of(paidOrder));
        when(afterSaleRepo.save(any())).thenAnswer(inv -> { AfterSale a = inv.getArgument(0); a.setId(1L); return a; });

        AfterSale result = afterSaleService.apply(100L, "user1", "refund", "不喜欢", 0L, "");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("0");
        verify(afterSaleRepo).save(any());
    }

    @Test
    void shouldRejectWhenOrderNotBelongToUser() {
        when(orderRepo.findById(100L)).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() -> afterSaleService.apply(100L, "hacker", "refund", "", 0L, "")).isInstanceOf(IllegalStateException.class).hasMessageContaining("无权操作");
    }

    @Test
    void shouldRejectWhenOrderNotPaid() {
        paidOrder.setStatus("0");
        when(orderRepo.findById(100L)).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() -> afterSaleService.apply(100L, "user1", "refund", "", 0L, "")).isInstanceOf(IllegalStateException.class).hasMessageContaining("仅已支付");
    }

    @Test
    void shouldApproveRefund() {
        AfterSale afterSale = new AfterSale();
        afterSale.setId(1L);
        afterSale.setOrderId(100L);
        afterSale.setType("refund");
        afterSale.setStatus("0");
        when(afterSaleRepo.findById(1L)).thenReturn(Optional.of(afterSale));

        afterSaleService.approve(1L, "");

        verify(afterSaleRepo).save(argThat(a -> !"0".equals(a.getStatus())));
    }

    @Test
    void shouldRejectDuplicateApprove() {
        AfterSale afterSale = new AfterSale();
        afterSale.setId(1L);
        afterSale.setStatus("4");
        when(afterSaleRepo.findById(1L)).thenReturn(Optional.of(afterSale));

        assertThatThrownBy(() -> afterSaleService.approve(1L, "")).isInstanceOf(IllegalStateException.class).hasMessageContaining("状态异常");
    }
}
