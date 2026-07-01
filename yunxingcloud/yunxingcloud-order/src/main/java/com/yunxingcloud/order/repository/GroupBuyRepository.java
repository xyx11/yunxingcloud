package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.GroupBuy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long> {
    List<GroupBuy> findByStatusAndEndTimeBefore(String status, LocalDateTime time);
}