package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.OrderHead;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderSearchController {

    @PersistenceContext private EntityManager em;

    /** 高级搜索: 多条件组合查询 */
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderHead> query = cb.createQuery(OrderHead.class);
        Root<OrderHead> root = query.from(OrderHead.class);
        List<Predicate> predicates = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            predicates.add(cb.or(
                    cb.like(root.get("orderNo"), "%" + keyword + "%"),
                    cb.like(root.get("username"), "%" + keyword + "%")));
        }
        if (status != null && !status.isBlank()) predicates.add(cb.equal(root.get("status"), status));
        if (username != null && !username.isBlank()) predicates.add(cb.equal(root.get("username"), username));
        if (startDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                java.time.LocalDateTime.parse(startDate + "T00:00:00")));
        if (endDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                java.time.LocalDateTime.parse(endDate + "T23:59:59")));

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("createdAt")));

        List<OrderHead> results = em.createQuery(query)
                .setFirstResult(page * size).setMaxResults(size).getResultList();

        // Count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<OrderHead> countRoot = countQuery.from(OrderHead.class);
        countQuery.select(cb.count(countRoot));
        List<Predicate> countPreds = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            countPreds.add(cb.or(
                    cb.like(countRoot.get("orderNo"), "%" + keyword + "%"),
                    cb.like(countRoot.get("username"), "%" + keyword + "%")));
        }
        if (status != null && !status.isBlank()) countPreds.add(cb.equal(countRoot.get("status"), status));
        if (username != null && !username.isBlank()) countPreds.add(cb.equal(countRoot.get("username"), username));
        countQuery.where(countPreds.toArray(new Predicate[0]));
        long total = em.createQuery(countQuery).getSingleResult();

        return ResponseEntity.ok(Map.of("content", results, "page", page, "size", size, "total", total));
    }
}