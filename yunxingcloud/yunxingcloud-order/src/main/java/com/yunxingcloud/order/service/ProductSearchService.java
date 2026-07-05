package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品全文搜索
 * - Elasticsearch 模式（默认）：中文分词 + 高性能搜索
 * - JPA Criteria + SQL LIKE 模式（回退）：无需 ES 即可运行
 */
@Service
public class ProductSearchService {

    @PersistenceContext
    private EntityManager em;

    private final com.yunxingcloud.order.repository.es.ProductSearchRepository esRepo;
    private final org.springframework.data.elasticsearch.core.ElasticsearchOperations esOps;
    private final boolean esAvailable;

    public ProductSearchService(
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            com.yunxingcloud.order.repository.es.ProductSearchRepository esRepo,
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            org.springframework.data.elasticsearch.core.ElasticsearchOperations esOps) {
        this.esRepo = esRepo;
        this.esOps = esOps;
        this.esAvailable = esRepo != null && esOps != null;
    }

    /**
     * Elasticsearch search with automatic fallback to JPA LIKE
     */
    public List<Product> searchWithES(String keyword, Long categoryId, Long brandId,
                                       Integer minPrice, Integer maxPrice,
                                       String sort, int page, int size) {
        if (esAvailable && keyword != null && !keyword.isBlank()) {
            try {
                var query = (org.springframework.data.elasticsearch.core.query.Query)
                    new org.springframework.data.elasticsearch.core.query.CriteriaQuery(
                        buildESCriteria(keyword, categoryId, brandId, minPrice, maxPrice))
                        .setPageable(org.springframework.data.domain.PageRequest.of(page, size));
                var products = esOps.search(query,
                    com.yunxingcloud.order.document.ProductDocument.class
                );
                if (products.hasSearchHits()) {
                    List<Long> ids = products.getSearchHits().stream()
                            .map(h -> h.getContent().getId()).toList();
                    return em.createQuery(
                            "SELECT p FROM Product p WHERE p.id IN :ids", Product.class)
                            .setParameter("ids", ids)
                            .getResultList();
                }
                return List.of();
            } catch (Exception e) {
                log.warn("ES search failed, falling back to JPA: {}", e.getMessage());
            }
        }
        return search(keyword, categoryId, brandId, minPrice, maxPrice, sort, page, size);
    }

    private org.springframework.data.elasticsearch.core.query.Criteria buildESCriteria(
            String keyword, Long categoryId, Long brandId, Integer minPrice, Integer maxPrice) {
        var criteria = new org.springframework.data.elasticsearch.core.query.Criteria("name").contains(keyword)
                .or(new org.springframework.data.elasticsearch.core.query.Criteria("description").contains(keyword));
        return criteria;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProductSearchService.class);

    public List<Product> search(String keyword, Long categoryId, Long brandId,
                                 Integer minPrice, Integer maxPrice,
                                 String sort, int page, int size) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        List<Predicate> predicates = new ArrayList<>();

        // 全文搜索 (name LIKE %keyword% OR description LIKE %keyword%)
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim() + "%";
            predicates.add(cb.or(
                    cb.like(root.get("name"), pattern),
                    cb.like(root.get("description"), pattern)
            ));
        }
        if (categoryId != null) predicates.add(cb.equal(root.get("categoryId"), categoryId));
        if (brandId != null) predicates.add(cb.equal(root.get("brandId"), brandId));
        if (minPrice != null) predicates.add(cb.ge(root.get("price"), minPrice));
        if (maxPrice != null) predicates.add(cb.le(root.get("price"), maxPrice));
        predicates.add(cb.equal(root.get("status"), "0")); // 只查上架商品

        query.where(predicates.toArray(new Predicate[0]));

        // 排序
        if ("sales".equals(sort)) query.orderBy(cb.desc(root.get("sales")));
        else if ("price_asc".equals(sort)) query.orderBy(cb.asc(root.get("price")));
        else if ("price_desc".equals(sort)) query.orderBy(cb.desc(root.get("price")));
        else if ("newest".equals(sort)) query.orderBy(cb.desc(root.get("createdAt")));
        else query.orderBy(cb.desc(root.get("sales")), cb.desc(root.get("createdAt")));

        return em.createQuery(query)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long count(String keyword, Long categoryId, Long brandId,
                       Integer minPrice, Integer maxPrice) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Product> root = query.from(Product.class);
        List<Predicate> predicates = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim() + "%";
            predicates.add(cb.or(
                    cb.like(root.get("name"), pattern),
                    cb.like(root.get("description"), pattern)
            ));
        }
        if (categoryId != null) predicates.add(cb.equal(root.get("categoryId"), categoryId));
        if (brandId != null) predicates.add(cb.equal(root.get("brandId"), brandId));
        if (minPrice != null) predicates.add(cb.ge(root.get("price"), minPrice));
        if (maxPrice != null) predicates.add(cb.le(root.get("price"), maxPrice));
        predicates.add(cb.equal(root.get("status"), "0"));

        query.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));
        return em.createQuery(query).getSingleResult();
    }
}