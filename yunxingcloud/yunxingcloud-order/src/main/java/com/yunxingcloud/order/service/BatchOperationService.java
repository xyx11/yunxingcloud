package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.OrderHead;
import com.yunxingcloud.order.repository.OrderHeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BatchOperationService {

    private final OrderHeadRepository orderRepo;

    public BatchOperationService(OrderHeadRepository orderRepo) { this.orderRepo = orderRepo; }

    @Transactional
    public Map<String, Object> batchShip(List<Integer> ids, String carrier) {
        int count = 0;
        for (Integer id : ids) {
            OrderHead order = orderRepo.findById(Long.valueOf(id)).orElse(null);
            if (order != null && "1".equals(order.getStatus())) {
                order.setStatus("2");
                order.setRemark("批量发货: " + carrier);
                orderRepo.save(order);
                count++;
            }
        }
        return Map.of("success", true, "shipped", count, "total", ids.size());
    }

    @Transactional
    public Map<String, Object> batchCancel(List<Integer> ids, String reason) {
        int count = 0;
        for (Integer id : ids) {
            OrderHead order = orderRepo.findById(Long.valueOf(id)).orElse(null);
            if (order != null && ("0".equals(order.getStatus()) || "1".equals(order.getStatus()))) {
                order.setStatus("4");
                order.setRemark(reason);
                orderRepo.save(order);
                count++;
            }
        }
        return Map.of("success", true, "canceled", count, "total", ids.size());
    }
}
