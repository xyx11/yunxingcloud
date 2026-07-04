package com.yunxingcloud.order.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class AIChatController {

    private static final Logger log = LoggerFactory.getLogger(AIChatController.class);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${app.ai.endpoint:}")
    private String aiEndpoint;

    @Value("${app.ai.api-key:}")
    private String aiApiKey;

    @Value("${app.ai.model:gpt-4o-mini}")
    private String aiModel;

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        String message = (String) body.getOrDefault("message", "");
        if (message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("reply", "请输入您的问题"));
        }

        if (!aiEnabled || aiEndpoint.isBlank()) {
            return ResponseEntity.ok(Map.of("reply", getFallbackReply(message)));
        }

        try {
            String systemPrompt = """
                你是YXCLOUD商城的AI客服助手。你的职责：
                1. 回答关于商品、订单、配送、售后等问题
                2. 帮助用户查找商品、推荐商品
                3. 处理投诉和建议
                请用友好、专业的语气回复，每次回复控制在100字以内。
                """;

            Map<String, Object> reqBody = Map.of(
                "model", aiModel,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", message)
                ),
                "max_tokens", 300
            );

            String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(reqBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiEndpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiApiKey)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Object> result = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(response.body(), Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
                    return ResponseEntity.ok(Map.of("reply", msg.getOrDefault("content", "抱歉，我没理解您的问题")));
                }
            }
            log.warn("AI API returned status: {}", response.statusCode());
        } catch (Exception e) {
            log.error("AI chat error", e);
        }
        return ResponseEntity.ok(Map.of("reply", getFallbackReply(message)));
    }

    private String getFallbackReply(String message) {
        String q = message.toLowerCase();
        if (q.contains("订单") || q.contains("物流")) return "请提供您的订单号，我会帮您查询订单状态和物流信息。您也可以在「我的订单」页面查看。";
        if (q.contains("退货") || q.contains("退款") || q.contains("售后")) return "我们支持7天无理由退货。请在「售后」页面提交申请，审核通过后1-3个工作日内退款。";
        if (q.contains("配送") || q.contains("发货") || q.contains("运费")) return "全国包邮，1-3个工作日送达。您可以在订单详情页查看实时物流信息。";
        if (q.contains("优惠") || q.contains("折扣") || q.contains("券")) return "请前往「优惠券中心」领取专属优惠券，新用户注册即送50元礼包！";
        if (q.contains("支付")) return "我们支持微信支付和支付宝，安全便捷。如有支付问题，请检查网络连接或更换支付方式。";
        return "感谢您的咨询！我们的客服团队会在工作时间（9:00-21:00）尽快回复您。您也可以拨打客服热线 400-xxx-xxxx。";
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("aiEnabled", aiEnabled, "model", aiModel));
    }
}
