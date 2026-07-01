package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByStatusAndStartTimeBeforeAndEndTimeAfter(String status, LocalDateTime now1, LocalDateTime now2);
}