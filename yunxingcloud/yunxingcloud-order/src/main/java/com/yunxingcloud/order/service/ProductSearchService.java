package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品全文搜索 (JPA Criteria + SQL LIKE — 轻量替代 Hibernate Search)
 * 生产环境可用 Hibernate Search + Elasticsearch 替代
 */
@Service
public class ProductSearchService {

    @PersistenceContext
    private EntityManager em;

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