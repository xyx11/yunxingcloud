package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.OrderHead;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderHeadRepository extends JpaRepository<OrderHead, Long> {
    List<OrderHead> findByUsernameOrderByCreatedAtDesc(String username);
    List<OrderHead> findByStatusOrderByCreatedAtDesc(String status);
    List<OrderHead> findByStatusAndExpireAtBefore(String status, java.time.LocalDateTime time);
}
