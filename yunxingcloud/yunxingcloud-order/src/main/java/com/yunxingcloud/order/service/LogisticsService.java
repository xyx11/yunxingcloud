package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.LogisticsTrace;
import com.yunxingcloud.order.repository.LogisticsTraceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    /** 获取最近的物流追踪记录 */
    public List<LogisticsTrace> latestTraces(int limit) {
        return repo.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "traceTime"))).getContent();
    }

    public long count() { return repo.count(); }
}
