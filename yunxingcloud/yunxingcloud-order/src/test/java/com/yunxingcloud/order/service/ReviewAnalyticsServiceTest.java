package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.ProductReview;
import com.yunxingcloud.order.repository.ProductReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewAnalyticsServiceTest {

    @Mock private ProductReviewRepository repo;
    @InjectMocks private ReviewAnalyticsService service;

    @Test
    void shouldReturnEmptyForNoReviews() {
        when(repo.findByProductIdOrderByCreatedAtDesc(100L)).thenReturn(List.of());

        Map<String, Object> result = service.summary(100L);
        assertThat(result.get("total")).isEqualTo(0);
    }

    @Test
    void shouldCalculateAvgRating() {
        ProductReview r1 = new ProductReview(); r1.setRating(5); r1.setUsername("user1");
        ProductReview r2 = new ProductReview(); r2.setRating(3); r2.setUsername("user2");
        when(repo.findByProductIdOrderByCreatedAtDesc(100L)).thenReturn(List.of(r1, r2));

        Map<String, Object> result = service.summary(100L);
        assertThat(result.get("total")).isEqualTo(2);
        assertThat(result.get("avgRating")).isEqualTo(4.0);
    }

    @Test
    void shouldCalculateDistribution() {
        ProductReview r = new ProductReview(); r.setRating(5); r.setUsername("u1");
        when(repo.findByProductIdOrderByCreatedAtDesc(100L)).thenReturn(List.of(r));

        @SuppressWarnings("unchecked")
        Map<Integer, Long> dist = (Map<Integer, Long>) service.summary(100L).get("distribution");
        assertThat(dist.get(5)).isEqualTo(1L);
        assertThat(dist.get(4)).isEqualTo(0L);
    }

    @Test
    void shouldGetSiteStats() {
        ProductReview r1 = new ProductReview(); r1.setRating(5);
        ProductReview r2 = new ProductReview(); r2.setRating(5);
        ProductReview r3 = new ProductReview(); r3.setRating(4);
        when(repo.findAll()).thenReturn(List.of(r1, r2, r3));

        Map<String, Object> result = service.siteStats();
        assertThat(result.get("totalReviews")).isEqualTo(3L);
        assertThat(result.get("fiveStarRate")).isEqualTo(67L);
    }
}