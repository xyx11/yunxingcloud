package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PersonalizationService {

    private final OrderHeadRepository orderRepo;
    private final OrderLineRepository lineRepo;
    private final ProductRepository productRepo;

    public PersonalizationService(OrderHeadRepository orderRepo, OrderLineRepository lineRepo,
                                   ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.lineRepo = lineRepo;
        this.productRepo = productRepo;
    }

    /** 个性化首页: 基于用户历史 + 热门 + 新品混合推荐 */
    public Map<String, Object> personalizedHome(String username) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 用户购买过的品类
        Set<Long> boughtCategoryIds = new HashSet<>();
        List<OrderHead> orders = orderRepo.findByUsernameOrderByCreatedAtDesc(username);
        for (OrderHead order : orders) {
            for (OrderLine line : lineRepo.findByOrderId(order.getId())) {
                productRepo.findById(line.getProductId()).ifPresent(p -> {
                    if (p.getCategoryId() != null) boughtCategoryIds.add(p.getCategoryId());
                });
            }
        }

        // 同类推荐 (买过的品类中的热门商品)
        List<Product> categoryRecs = new ArrayList<>();
        for (Long catId : boughtCategoryIds.stream().limit(3).toList()) {
            productRepo.findByCategoryIdAndStatus(catId, "0",
                    PageRequest.of(0, 4)).forEach(categoryRecs::add);
        }
        result.put("categoryRecommend", categoryRecs.stream().distinct().limit(8).toList());

        // 热门
        result.put("hot", productRepo.findByIsHotTrueAndStatus("0",
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "sales"))
                .stream().limit(8).toList());

        // 新品
        result.put("news", productRepo.findByIsNewTrueAndStatus("0",
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                .stream().limit(8).toList());

        // 猜你喜欢 (随机热门 + 新品混合)
        Set<Product> guessYouLike = new LinkedHashSet<>();
        guessYouLike.addAll(productRepo.findByIsHotTrueAndStatus("0",
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "sales"))
                .stream().limit(6).toList());
        guessYouLike.addAll(productRepo.findByIsNewTrueAndStatus("0",
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                .stream().limit(6).toList());
        result.put("guessYouLike", guessYouLike.stream().limit(12).toList());

        return result;
    }
}