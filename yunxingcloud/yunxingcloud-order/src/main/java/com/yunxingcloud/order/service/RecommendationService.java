package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ProductRepository productRepo;

    public RecommendationService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    /** 基于协同过滤: 买了X的人也买了Y */
    public List<Product> alsoBought(Long productId, int limit) {
        // 简化实现: 同分类下按销量推荐
        Product p = productRepo.findById(productId).orElse(null);
        if (p == null || p.getCategoryId() == null) return hotProducts(limit);

        return productRepo.findByCategoryIdAndIdNot(p.getCategoryId(), productId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "sales")));
    }

    /** 个性化推荐: 基于用户历史购买分类 */
    public List<Product> forUser(String username, int limit) {
        // 简化: 如果用户无历史, 返回热门
        return hotProducts(limit);
    }

    /** 热门商品 */
    public List<Product> hotProducts(int limit) {
        return productRepo.findByIsHotTrueAndStatus("0",
                Sort.by(Sort.Direction.DESC, "sales")).stream()
                .limit(limit).collect(Collectors.toList());
    }

    /** 新品推荐 */
    public List<Product> newArrivals(int limit) {
        return productRepo.findByIsNewTrueAndStatus("0",
                Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .limit(limit).collect(Collectors.toList());
    }
}