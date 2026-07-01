package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.ProductTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductTagRelationRepository extends JpaRepository<ProductTagRelation, Long> {
    List<ProductTagRelation> findByProductId(Long productId);
    List<ProductTagRelation> findByTagId(Long tagId);
    boolean existsByProductIdAndTagId(Long productId, Long tagId);
    void deleteByProductIdAndTagId(Long productId, Long tagId);
}