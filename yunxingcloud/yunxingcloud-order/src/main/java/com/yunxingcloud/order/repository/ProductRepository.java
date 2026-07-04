package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByStatus(String status, Pageable pageable);
    List<Product> findByStatusOrderByCreatedAtDesc(String status);
    List<Product> findByIsHotTrueAndStatus(String status, Sort sort);
    List<Product> findByIsNewTrueAndStatus(String status, Sort sort);
    List<Product> findByCategoryIdAndIdNot(Long categoryId, Long id, Pageable pageable);
    List<Product> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);
    List<Product> findByBrandIdAndStatus(Long brandId, String status, Pageable pageable);
    List<Product> findByBrandIdAndIdNot(Long brandId, Long id, Pageable pageable);
    List<Product> findByCategoryIdInAndIdNotInAndStatus(List<Long> categoryIds, List<Long> excludeIds, String status, Pageable pageable);
    List<Product> findByTagsContainingAndIdNot(String tag, Long id, Pageable pageable);
}
