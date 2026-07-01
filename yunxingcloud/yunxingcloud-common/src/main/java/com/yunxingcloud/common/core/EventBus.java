package com.yunxingcloud.common.core;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * 本地事件总线: 发布-订阅模式, 解耦模块间通信
 */
@Component
public class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);
    private final Map<String, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "eventbus-");
        t.setDaemon(true);
        return t;
    });

    /** 订阅 */
    public <T> void subscribe(String eventType, Consumer<T> handler) {
        @SuppressWarnings("unchecked")
        Consumer<Object> wrapped = (Object o) -> handler.accept((T) o);
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(wrapped);
    }

    /** 发布 (异步) */
    public void publish(String eventType, Object event) {
        List<Consumer<Object>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            for (Consumer<Object> h : handlers) {
                executor.submit(() -> {
                    try { h.accept(event); }
                    catch (Exception e) { log.error("事件处理异常: {}", e.getMessage()); }
                });
            }
        }
    }

    /** 同步发布 */
    public void publishSync(String eventType, Object event) {
        List<Consumer<Object>> handlers = subscribers.get(eventType);
        if (handlers != null) handlers.forEach(h -> h.accept(event));
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try { executor.awaitTermination(10, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
    }
}