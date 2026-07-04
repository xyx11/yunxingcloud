package com.yunxingcloud.order.repository.es;

import com.yunxingcloud.order.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    List<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description);

    List<ProductDocument> findByNameContaining(String keyword);

    List<ProductDocument> findByCategoryName(String categoryName);

    List<ProductDocument> findByBrandName(String brandName);

    List<ProductDocument> findByPriceBetween(Double min, Double max);

    List<ProductDocument> findByIsHotTrue();

    List<ProductDocument> findByIsNewTrue();
}
