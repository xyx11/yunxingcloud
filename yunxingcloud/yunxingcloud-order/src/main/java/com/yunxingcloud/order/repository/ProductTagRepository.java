package com.yunxingcloud.order.repository;

import com.yunxingcloud.order.entity.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {
    List<ProductTag> findAllByOrderBySortOrderAsc();
}