package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.config.TokenStore;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "服务端推送", description = "SSE服务端推送")
@RestController
@RequestMapping("/api/sse")
public class SseController {

    private final TokenStore tokenStore;
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseController(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @GetMapping(value = "/dashboard", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // 发送初始数据
        try {
            emitter.send(SseEmitter.event().name("stats").data(currentStats()));
        } catch (Exception ignored) {}

        return emitter;
    }

    @PostMapping("/trigger")
    public void trigger() {
        Map<String, Object> stats = currentStats();
        for (SseEmitter e : emitters) {
            try { e.send(SseEmitter.event().name("stats").data(stats)); } catch (Exception ex) {
                emitters.remove(e);
            }
        }
    }

    private Map<String, Object> currentStats() {
        var runtime = Runtime.getRuntime();
        var mem = ManagementFactory.getMemoryMXBean();
        return Map.<String, Object>of(
                "timestamp", System.currentTimeMillis(),
                "usedMemory", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + "MB",
                "maxMemory", runtime.maxMemory() / (1024 * 1024) + "MB",
                "heapUsed", mem.getHeapMemoryUsage().getUsed() / (1024 * 1024) + "MB",
                "activeSessions", tokenStore.count()
        );
    }
}
