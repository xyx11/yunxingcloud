package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {
    List<FlashSale> findByStatusAndStartTimeBefore(String status, java.time.LocalDateTime time);

    @Modifying
    @Query("UPDATE FlashSale f SET f.sold = f.sold + 1 WHERE f.id = :id AND f.sold < f.stock")
    int incrementSold(Long id);
}