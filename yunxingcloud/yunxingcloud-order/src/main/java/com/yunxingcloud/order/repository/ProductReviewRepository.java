package com.yunxingcloud.order.repository;
import com.yunxingcloud.order.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);
}
