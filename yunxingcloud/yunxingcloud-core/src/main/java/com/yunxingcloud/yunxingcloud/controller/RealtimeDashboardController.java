package com.yunxingcloud.yunxingcloud.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "实时大屏", description = "实时数据大屏")
@RestController
@RequestMapping("/api/realtime")
public class RealtimeDashboardController {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(value = "/dashboard", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        try { emitter.send(SseEmitter.event().name("init").data(currentMetrics())); } catch (Exception ignored) {}

        return emitter;
    }

    /** 每 5s 推送一次 */
    {
        scheduler.scheduleAtFixedRate(() -> {
            Map<String, Object> metrics = currentMetrics();
            for (SseEmitter e : emitters) {
                try { e.send(SseEmitter.event().name("metrics").data(metrics)); }
                catch (Exception ex) { emitters.remove(e); }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private Map<String, Object> currentMetrics() {
        Runtime rt = Runtime.getRuntime();
        var mem = ManagementFactory.getMemoryMXBean();
        var os = ManagementFactory.getOperatingSystemMXBean();

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", System.currentTimeMillis());
        m.put("heapUsed", mem.getHeapMemoryUsage().getUsed() / 1048576);
        m.put("heapMax", mem.getHeapMemoryUsage().getMax() / 1048576);
        m.put("nonHeapUsed", mem.getNonHeapMemoryUsage().getUsed() / 1048576);
        m.put("cpuLoad", os.getSystemLoadAverage());
        m.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
        m.put("availableProcessors", rt.availableProcessors());
        return m;
    }
}