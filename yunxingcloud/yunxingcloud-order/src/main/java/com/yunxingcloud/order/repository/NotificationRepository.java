package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUsernameOrUsernameOrderByCreatedAtDesc(String username, String all);
    long countByUsernameAndIsReadFalse(String username);
}