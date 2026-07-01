package com.yunxingcloud.order.repository;
import com.yunxingcloud.order.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {}
