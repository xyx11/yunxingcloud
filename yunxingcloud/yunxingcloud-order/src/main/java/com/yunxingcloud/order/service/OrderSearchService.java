package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.OrderHead;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderSearchService {

    @PersistenceContext
    private EntityManager em;

    public Map<String, Object> search(String keyword, String status, String username,
                                       String startDate, String endDate, int page, int size) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<OrderHead> query = cb.createQuery(OrderHead.class);
        Root<OrderHead> root = query.from(OrderHead.class);
        List<Predicate> predicates = buildPredicates(cb, root, keyword, status, username, startDate, endDate);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("createdAt")));

        List<OrderHead> results = em.createQuery(query)
                .setFirstResult(page * size).setMaxResults(size).getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<OrderHead> countRoot = countQuery.from(OrderHead.class);
        countQuery.select(cb.count(countRoot));
        List<Predicate> countPreds = buildPredicates(cb, countRoot, keyword, status, username, startDate, endDate);
        countQuery.where(countPreds.toArray(new Predicate[0]));
        long total = em.createQuery(countQuery).getSingleResult();

        return Map.of("content", results, "page", page, "size", size, "total", total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<OrderHead> root,
                                             String keyword, String status, String username,
                                             String startDate, String endDate) {
        List<Predicate> predicates = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            predicates.add(cb.or(
                    cb.like(root.get("orderNo"), "%" + keyword + "%"),
                    cb.like(root.get("username"), "%" + keyword + "%")));
        }
        if (status != null && !status.isBlank()) predicates.add(cb.equal(root.get("status"), status));
        if (username != null && !username.isBlank()) predicates.add(cb.equal(root.get("username"), username));
        if (startDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                LocalDateTime.parse(startDate + "T00:00:00")));
        if (endDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                LocalDateTime.parse(endDate + "T23:59:59")));
        return predicates;
    }
}
