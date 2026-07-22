package com.yunxingcloud.order.repository;
import com.yunxingcloud.order.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);
    Page<ProductReview> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
}
