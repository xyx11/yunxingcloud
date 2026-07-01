package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.LogisticsTrace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogisticsTraceRepository extends JpaRepository<LogisticsTrace, Long> {
    List<LogisticsTrace> findByOrderIdOrderByTraceTimeDesc(Long orderId);
    List<LogisticsTrace> findByTrackingNoOrderByTraceTimeDesc(String trackingNo);
}