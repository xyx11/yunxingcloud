package com.yunxingcloud.common.core;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A/B 测试分流服务
 * 用法: String variant = abTest.assign("homepage-redesign", "A", "B")
 */
@Component
public class ABTestService {

    private final SecureRandom random = new SecureRandom();
    private final Map<String, ABTest> tests = new ConcurrentHashMap<>();

    /** 注册实验 */
    public void register(String name, String... variants) {
        tests.put(name, new ABTest(name, variants));
    }

    /** 分配变体 (基于userId一致性哈希) */
    public String assign(String testName, String userId, String... defaultVariants) {
        ABTest test = tests.get(testName);
        String[] variants = test != null ? test.variants : defaultVariants;
        if (variants.length == 0) return "control";

        // 一致性哈希: 同一用户始终分配到同一变体
        int hash = Math.abs(userId.hashCode());
        return variants[hash % variants.length];
    }

    /** 加权分配 */
    public String assignWeighted(String testName, String userId, Map<String, Integer> variantWeights) {
        int hash = Math.abs(userId.hashCode());
        int totalWeight = variantWeights.values().stream().mapToInt(Integer::intValue).sum();
        int bucket = hash % totalWeight;
        int cumulative = 0;
        for (var entry : variantWeights.entrySet()) {
            cumulative += entry.getValue();
            if (bucket < cumulative) return entry.getKey();
        }
        return variantWeights.keySet().iterator().next();
    }

    private record ABTest(String name, String[] variants) {}
}