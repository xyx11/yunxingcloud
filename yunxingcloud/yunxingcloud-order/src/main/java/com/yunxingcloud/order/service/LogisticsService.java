package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.LogisticsTrace;
import com.yunxingcloud.order.repository.LogisticsTraceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LogisticsService {

    private final LogisticsTraceRepository repo;

    public LogisticsService(LogisticsTraceRepository repo) { this.repo = repo; }

    public List<LogisticsTrace> tracesByOrder(Long orderId) {
        return repo.findByOrderIdOrderByTraceTimeDesc(orderId);
    }

    public List<LogisticsTrace> track(String trackingNo) {
        return repo.findByTrackingNoOrderByTraceTimeDesc(trackingNo);
    }

    @Transactional
    public LogisticsTrace add(LogisticsTrace trace) { return repo.save(trace); }
}
