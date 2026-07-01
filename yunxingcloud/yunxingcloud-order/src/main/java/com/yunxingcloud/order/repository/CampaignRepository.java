package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByStatusAndStartTimeBeforeAndEndTimeAfter(String status, LocalDateTime now1, LocalDateTime now2);

    @Modifying
    @Query("UPDATE Campaign c SET c.usedCount = c.usedCount + 1 WHERE c.id = :id AND c.totalStock IS NULL OR c.usedCount < c.totalStock")
    int incrementUsedCount(Long id);
}