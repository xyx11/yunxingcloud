package com.yunxingcloud.payment.repository;

import com.yunxingcloud.payment.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByOrderNo(String orderNo);
    List<PaymentOrder> findByStatusOrderByCreatedAtDesc(String status);
    List<PaymentOrder> findByChannelOrderByCreatedAtDesc(String channel);
}
