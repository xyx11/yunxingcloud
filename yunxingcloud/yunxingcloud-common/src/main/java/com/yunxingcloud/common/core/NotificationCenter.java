package com.yunxingcloud.common.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一通知中心: 邮件/站内信/短信/App推送的抽象层
 */
@Service
public class NotificationCenter {

    private static final Logger log = LoggerFactory.getLogger(NotificationCenter.class);
    private final Map<String, List<Map<String, Object>>> history = new ConcurrentHashMap<>();

    @Async
    public void notify(String username, String title, String content, String... channels) {
        for (String channel : channels) {
            log.info("[通知] user={} channel={} title={}", username, channel, title);
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("username", username);
            record.put("channel", channel);
            record.put("title", title);
            record.put("content", content);
            record.put("time", LocalDateTime.now().toString());
            history.computeIfAbsent(username, k -> new ArrayList<>()).add(record);
        }
    }

    public void orderCreated(String username, String orderNo, long amount) {
        notify(username, "订单已确认",
                String.format("订单 %s 已确认，应付 ¥%.2f", orderNo, amount / 100.0),
                "inapp", "email");
    }

    public void orderPaid(String username, String orderNo) {
        notify(username, "支付成功",
                String.format("订单 %s 已支付", orderNo),
                "inapp", "email");
    }

    public void orderShipped(String username, String orderNo, String carrier, String trackingNo) {
        notify(username, "订单已发货",
                String.format("订单 %s 已发货 [%s: %s]", orderNo, carrier, trackingNo),
                "inapp", "email", "sms");
    }

    public List<Map<String, Object>> getHistory(String username) {
        return history.getOrDefault(username, List.of());
    }
}