package com.yunxingcloud.common.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class AppMonitor {

    private static final Logger log = LoggerFactory.getLogger(AppMonitor.class);
    private final LocalDateTime startTime = LocalDateTime.now();

    public Map<String, Object> snapshot() {
        Runtime rt = Runtime.getRuntime();
        var mem = ManagementFactory.getMemoryMXBean();
        var threads = ManagementFactory.getThreadMXBean();

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("uptime", Duration.between(startTime, LocalDateTime.now()).toMinutes() + "min");
        m.put("heapUsedMB", mem.getHeapMemoryUsage().getUsed() / 1048576);
        m.put("heapMaxMB", mem.getHeapMemoryUsage().getMax() / 1048576);
        m.put("nonHeapMB", mem.getNonHeapMemoryUsage().getUsed() / 1048576);
        m.put("threads", threads.getThreadCount());
        m.put("daemonThreads", threads.getDaemonThreadCount());
        m.put("cpuCores", rt.availableProcessors());
        m.put("loadAvg", ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
        return m;
    }

    public void logStatus() {
        Map<String, Object> s = snapshot();
        log.info("系统状态: heap={}/{}MB threads={} uptime={}",
                s.get("heapUsedMB"), s.get("heapMaxMB"),
                s.get("threads"), s.get("uptime"));
    }
}