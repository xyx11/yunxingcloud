package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {
    List<FlashSale> findByStatusAndStartTimeBefore(String status, java.time.LocalDateTime time);
}