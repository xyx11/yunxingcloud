package com.yunxingcloud.order.repository;
import com.yunxingcloud.order.entity.OrderShipment;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderShipmentRepository extends JpaRepository<OrderShipment, Long> {
    OrderShipment findByOrderId(Long orderId);
}
