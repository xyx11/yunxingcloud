package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.ProductReview;
import com.yunxingcloud.order.repository.ProductReviewRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReviewAnalyticsService {

    private final ProductReviewRepository repo;

    public ReviewAnalyticsService(ProductReviewRepository repo) { this.repo = repo; }

    /** 商品评价概览 */
    public Map<String, Object> summary(Long productId) {
        List<ProductReview> reviews = repo.findByProductIdOrderByCreatedAtDesc(productId);
        int total = reviews.size();
        if (total == 0) return Map.of("total", 0);

        double avgRating = reviews.stream().mapToInt(ProductReview::getRating).average().orElse(0);
        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            final int star = i;
            distribution.put(i, reviews.stream().filter(r -> r.getRating() == star).count());
        }

        return Map.of(
                "total", total,
                "avgRating", Math.round(avgRating * 10) / 10.0,
                "distribution", distribution,
                "recent", reviews.stream().limit(5).map(r -> Map.of(
                        "username", r.getUsername(),
                        "rating", r.getRating(),
                        "content", r.getContent() != null ? r.getContent() : "",
                        "createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : ""
                )).toList()
        );
    }

    /** 全站评价统计 */
    public Map<String, Object> siteStats() {
        List<ProductReview> all = repo.findAll();
        long total = all.size();
        double avg = all.stream().mapToInt(ProductReview::getRating).average().orElse(0);
        long fiveStar = all.stream().filter(r -> r.getRating() == 5).count();

        return Map.of(
                "totalReviews", total,
                "avgRating", Math.round(avg * 10) / 10.0,
                "fiveStarRate", total > 0 ? Math.round(100.0 * fiveStar / total) : 0
        );
    }
}