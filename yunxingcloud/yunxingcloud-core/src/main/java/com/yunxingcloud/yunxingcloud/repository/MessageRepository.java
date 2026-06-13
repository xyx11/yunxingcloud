package com.yunxingcloud.yunxingcloud.repository;

import com.yunxingcloud.yunxingcloud.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByToUserOrderByCreatedAtDesc(String toUser);
    List<Message> findByFromUserOrderByCreatedAtDesc(String fromUser);
    @Query("SELECT COUNT(m) FROM Message m WHERE m.toUser = ?1 AND m.isRead = false")
    long countUnread(String toUser);
}
