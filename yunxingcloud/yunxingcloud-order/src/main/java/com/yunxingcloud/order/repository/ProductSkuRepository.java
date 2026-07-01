package com.yunxingcloud.order.repository;
import com.yunxingcloud.order.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {
    List<ProductSku> findByProductId(Long productId);
}
