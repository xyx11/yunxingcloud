package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.entity.OrderShipment;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import com.yunxingcloud.order.repository.OrderShipmentRepository;
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
class OrderFulfillmentServiceTest {

    @Mock private OrderHeadRepository orderRepo;
    @Mock private OrderShipmentRepository shipmentRepo;
    @InjectMocks private OrderFulfillmentService service;

    @Test
    void shouldShipOrder() {
        OrderHead o = new OrderHead(); o.setId(1L); o.setStatus("1");
        when(orderRepo.findById(1L)).thenReturn(Optional.of(o));
        OrderShipment s = new OrderShipment(); s.setId(1L);
        when(shipmentRepo.save(any())).thenReturn(s);

        OrderShipment result = service.ship(1L, "SF", "SF123");
        assertThat(result).isNotNull();
        assertThat(o.getStatus()).isEqualTo("2");
    }

    @Test
    void shouldRejectShipNonPaidOrder() {
        OrderHead o = new OrderHead(); o.setId(1L); o.setStatus("0");
        when(orderRepo.findById(1L)).thenReturn(Optional.of(o));

        assertThatThrownBy(() -> service.ship(1L, "SF", "SF123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("仅已支付");
    }

    @Test
    void shouldConfirmReceive() {
        OrderHead o = new OrderHead(); o.setId(1L); o.setStatus("2");
        when(orderRepo.findById(1L)).thenReturn(Optional.of(o));

        service.confirmReceive(1L);
        assertThat(o.getStatus()).isEqualTo("3");
    }

    @Test
    void shouldRejectConfirmNonShippedOrder() {
        OrderHead o = new OrderHead(); o.setId(1L); o.setStatus("1");
        when(orderRepo.findById(1L)).thenReturn(Optional.of(o));

        assertThatThrownBy(() -> service.confirmReceive(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("仅已发货");
    }
}