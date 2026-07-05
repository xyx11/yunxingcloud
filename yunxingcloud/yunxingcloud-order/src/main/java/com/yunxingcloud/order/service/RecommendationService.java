package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final ProductRepository productRepo;
    private final JdbcTemplate jdbc;

    public RecommendationService(ProductRepository productRepo, JdbcTemplate jdbc) {
        this.productRepo = productRepo;
        this.jdbc = jdbc;
    }

    /** 协同过滤: 买了X的人也买了Y */
    public List<Product> alsoBought(Long productId, int limit) {
        Product p = productRepo.findById(productId).orElse(null);
        if (p == null || p.getCategoryId() == null) return hotProducts(limit);

        // 查找购买了该商品的用户还买了什么
        try {
            List<Long> cfIds = jdbc.queryForList(
                "SELECT DISTINCT ol2.product_id FROM order_line ol1 " +
                "JOIN order_line ol2 ON ol1.order_id = ol2.order_id AND ol1.product_id != ol2.product_id " +
                "WHERE ol1.product_id = ? GROUP BY ol2.product_id ORDER BY COUNT(*) DESC LIMIT ?",
                Long.class, productId, limit
            );
            if (!cfIds.isEmpty()) {
                return productRepo.findAllById(cfIds);
            }
        } catch (Exception ignored) { log.warn("操作异常: {}", ignored.getMessage()); }

        return productRepo.findByCategoryIdAndIdNot(p.getCategoryId(), productId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "sales")));
    }

    /** 个性化推荐: 用户协同过滤 + 内容推荐 */
    public List<Product> forUser(String username, int limit) {
        Set<Long> boughtIds = new HashSet<>();
        Set<Long> categoryIds = new HashSet<>();

        try {
            // 获取用户购买过的商品ID和分类
            List<Map<String, Object>> history = jdbc.queryForList(
                "SELECT DISTINCT ol.product_id, p.category_id FROM order_line ol " +
                "JOIN order_head oh ON ol.order_id = oh.id " +
                "JOIN product p ON ol.product_id = p.id " +
                "WHERE oh.username = ? AND oh.status IN ('1','2','3') LIMIT 50",
                username
            );
            for (Map<String, Object> row : history) {
                boughtIds.add(((Number) row.get("product_id")).longValue());
                Object cid = row.get("category_id");
                if (cid != null) categoryIds.add(((Number) cid).longValue());
            }
        } catch (Exception ignored) { log.warn("操作异常: {}", ignored.getMessage()); }

        List<Product> results = new ArrayList<>();

        // 1. 协同过滤: 相似用户还买了什么
        if (!boughtIds.isEmpty()) {
            try {
                List<Long> cfIds = jdbc.queryForList(
                    "SELECT DISTINCT ol2.product_id FROM order_line ol1 " +
                    "JOIN order_line ol2 ON ol1.order_id = ol2.order_id AND ol1.product_id != ol2.product_id " +
                    "WHERE ol1.product_id IN (" + boughtIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") " +
                    "AND ol2.product_id NOT IN (" + boughtIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") " +
                    "GROUP BY ol2.product_id ORDER BY COUNT(*) DESC LIMIT ?",
                    Long.class, limit / 2
                );
                if (!cfIds.isEmpty()) results.addAll(productRepo.findAllById(cfIds));
            } catch (Exception ignored) { log.warn("操作异常: {}", ignored.getMessage()); }
        }

        // 2. 内容推荐: 同分类热门商品
        int remaining = limit - results.size();
        if (!categoryIds.isEmpty() && remaining > 0) {
            List<Product> content = productRepo.findByCategoryIdInAndIdNotInAndStatus(
                new ArrayList<>(categoryIds), new ArrayList<>(boughtIds), "0",
                PageRequest.of(0, remaining, Sort.by(Sort.Direction.DESC, "sales")));
            results.addAll(content);
        }

        // 3. 热度混排: 不够则补热门
        remaining = limit - results.size();
        if (remaining > 0) {
            Set<Long> seen = results.stream().map(Product::getId).collect(Collectors.toSet());
            seen.addAll(boughtIds);
            List<Product> hot = hotProducts(remaining + seen.size()).stream()
                .filter(p -> !seen.contains(p.getId())).limit(remaining).toList();
            results.addAll(hot);
        }

        return results.stream().limit(limit).collect(Collectors.toList());
    }

    /** 基于内容相似度的推荐 (同品牌/同标签) */
    public List<Product> similarProducts(Long productId, int limit) {
        Product p = productRepo.findById(productId).orElse(null);
        if (p == null) return hotProducts(limit);

        List<Product> results = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        seen.add(productId);

        // 同品牌
        if (p.getBrandId() != null) {
            List<Product> brand = productRepo.findByBrandIdAndIdNot(p.getBrandId(), productId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "sales")));
            results.addAll(brand);
            brand.forEach(b -> seen.add(b.getId()));
        }

        // 同标签
        int remaining = limit - results.size();
        if (p.getTags() != null && !p.getTags().isBlank() && remaining > 0) {
            String[] tags = p.getTags().split("[,，]");
            for (String tag : tags) {
                if (remaining <= 0) break;
                List<Product> tagged = productRepo.findByTagsContainingAndIdNot(
                    tag.trim(), productId, PageRequest.of(0, remaining, Sort.by(Sort.Direction.DESC, "sales")));
                for (Product tp : tagged) {
                    if (!seen.contains(tp.getId()) && remaining > 0) {
                        results.add(tp);
                        seen.add(tp.getId());
                        remaining--;
                    }
                }
            }
        }

        return results.stream().limit(limit).collect(Collectors.toList());
    }

    /** 混合推荐: 热度 + 新品 + 个性化权重 */
    public List<Product> hybridRecommend(String username, int limit) {
        Map<Long, Double> scores = new HashMap<>();

        // 热度分 (0-1归一化)
        List<Product> hot = hotProducts(50);
        int maxSales = hot.stream().mapToInt(p -> p.getSales() != null ? p.getSales() : 0).max().orElse(1);
        for (Product p : hot) {
            scores.merge(p.getId(), 0.4 * (p.getSales() != null ? p.getSales() : 0) / maxSales, Double::sum);
        }

        // 协同过滤分
        try {
            List<Map<String, Object>> cfScores = jdbc.queryForList(
                "SELECT ol2.product_id, COUNT(*) as cnt FROM order_line ol1 " +
                "JOIN order_line ol2 ON ol1.order_id = ol2.order_id AND ol1.product_id != ol2.product_id " +
                "GROUP BY ol2.product_id ORDER BY cnt DESC LIMIT 100"
            );
            int maxCf = cfScores.stream().mapToInt(r -> ((Number) r.get("cnt")).intValue()).max().orElse(1);
            for (Map<String, Object> row : cfScores) {
                long pid = ((Number) row.get("product_id")).longValue();
                double cf = ((Number) row.get("cnt")).doubleValue();
                scores.merge(pid, 0.35 * cf / maxCf, Double::sum);
            }
        } catch (Exception ignored) { log.warn("操作异常: {}", ignored.getMessage()); }

        // 新品加分
        List<Product> news = newArrivals(50);
        for (Product p : news) {
            scores.merge(p.getId(), 0.25, Double::sum);
        }

        // 排序取top
        List<Long> ranked = scores.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (ranked.isEmpty()) return hotProducts(limit);
        List<Product> result = productRepo.findAllById(ranked);
        result.sort(Comparator.comparingInt(p -> ranked.indexOf(p.getId())));
        return result;
    }

    public List<Product> hotProducts(int limit) {
        return productRepo.findByIsHotTrueAndStatus("0",
                Sort.by(Sort.Direction.DESC, "sales")).stream()
                .limit(limit).collect(Collectors.toList());
    }

    public List<Product> newArrivals(int limit) {
        return productRepo.findByIsNewTrueAndStatus("0",
                Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .limit(limit).collect(Collectors.toList());
    }
}
