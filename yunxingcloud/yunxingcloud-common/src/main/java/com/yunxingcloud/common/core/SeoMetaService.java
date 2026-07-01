package com.yunxingcloud.common.core;

import java.util.*;

/**
 * SEO 元数据管理服务
 * 动态生成 title/description/canonical/og 标签
 */
public final class SeoMetaService {

    private SeoMetaService() {}

    public static Map<String, String> productPage(String name, String description, String imageUrl, String url) {
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("title", name + " — YXCLOUD 商城");
        meta.put("description", description != null ? description.substring(0, Math.min(description.length(), 160)) : name);
        meta.put("canonical", url);
        meta.put("og:title", name);
        meta.put("og:description", meta.get("description"));
        meta.put("og:image", imageUrl != null ? imageUrl : "/default-product.png");
        meta.put("og:type", "product");
        meta.put("og:url", url);
        return meta;
    }

    public static Map<String, String> categoryPage(String name, String url) {
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("title", name + " — 分类 — YXCLOUD 商城");
        meta.put("description", "浏览 " + name + " 分类下的精选商品");
        meta.put("canonical", url);
        return meta;
    }

    public static Map<String, String> homePage() {
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("title", "YXCLOUD 商城 — 品质生活，从这里开始");
        meta.put("description", "YXCLOUD 分布式微服务电商平台，提供全品类商品、秒杀、拼团、积分商城");
        meta.put("og:title", "YXCLOUD 商城");
        meta.put("og:description", meta.get("description"));
        meta.put("og:type", "website");
        return meta;
    }

    public static String toHtmlMeta(Map<String, String> meta) {
        StringBuilder sb = new StringBuilder();
        sb.append("<title>").append(meta.getOrDefault("title", "YXCLOUD")).append("</title>\n");
        sb.append("<meta name=\"description\" content=\"").append(meta.getOrDefault("description", "")).append("\">\n");
        if (meta.containsKey("canonical")) {
            sb.append("<link rel=\"canonical\" href=\"").append(meta.get("canonical")).append("\">\n");
        }
        meta.forEach((k, v) -> {
            if (k.startsWith("og:")) {
                sb.append("<meta property=\"").append(k).append("\" content=\"").append(v).append("\">\n");
            }
        });
        return sb.toString();
    }
}