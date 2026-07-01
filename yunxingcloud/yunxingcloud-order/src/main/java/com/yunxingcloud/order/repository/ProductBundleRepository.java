package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.ProductBundle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductBundleRepository extends JpaRepository<ProductBundle, Long> {
    List<ProductBundle> findByStatus(String status);
}