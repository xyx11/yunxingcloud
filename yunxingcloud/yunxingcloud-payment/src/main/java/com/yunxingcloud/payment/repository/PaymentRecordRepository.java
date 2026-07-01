package com.yunxingcloud.payment.repository;

import com.yunxingcloud.payment.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    List<PaymentRecord> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
