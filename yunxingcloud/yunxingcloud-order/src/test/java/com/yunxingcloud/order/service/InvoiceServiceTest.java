package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Invoice;
import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.InvoiceRepository;
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
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepo;
    @Mock private OrderHeadRepository orderRepo;
    @InjectMocks private InvoiceService service;

    private OrderHead doneOrder;

    @BeforeEach
    void setUp() {
        doneOrder = new OrderHead();
        doneOrder.setId(100L);
        doneOrder.setOrderNo("ORD-001");
        doneOrder.setUsername("user1");
        doneOrder.setStatus("3");
        doneOrder.setTotalAmount(9900L);
    }

    @Test
    void shouldApplyPersonalInvoice() {
        when(orderRepo.findById(100L)).thenReturn(Optional.of(doneOrder));
        when(invoiceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = service.apply(100L, "user1", "personal", "", "", "test@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("0");
        verify(invoiceRepo).save(any());
    }

    @Test
    void shouldApplyCompanyInvoice() {
        when(orderRepo.findById(100L)).thenReturn(Optional.of(doneOrder));
        when(invoiceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = service.apply(100L, "user1", "company", "XX公司", "9111000", "");

        assertThat(result).isNotNull();
    }

    @Test
    void shouldRejectWhenOrderNotDone() {
        doneOrder.setStatus("0");
        when(orderRepo.findById(100L)).thenReturn(Optional.of(doneOrder));

        assertThatThrownBy(() -> service.apply(100L, "user1", "personal", "", "", ""))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("已完成订单");
    }
}