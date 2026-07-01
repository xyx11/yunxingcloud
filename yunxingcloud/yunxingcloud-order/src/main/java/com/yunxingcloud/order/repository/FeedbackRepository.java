package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUsernameOrderByCreatedAtDesc(String username);
    List<Feedback> findByOrderByCreatedAtDesc();
}