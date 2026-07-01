package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
    List<OrderLine> findByOrderId(Long orderId);
}
