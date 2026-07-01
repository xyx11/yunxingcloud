package com.yunxingcloud.common.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class AuditTrail {

    private static final Logger log = LoggerFactory.getLogger("AUDIT");
    private final Deque<Map<String, Object>> events = new ConcurrentLinkedDeque<>();
    private static final int MAX_EVENTS = 1000;

    public void record(String entity, String action, String user, String before, String after) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("entity", entity); event.put("action", action);
        event.put("user", user); event.put("before", before);
        event.put("after", after); event.put("time", LocalDateTime.now().toString());
        events.addFirst(event);
        if (events.size() > MAX_EVENTS) events.removeLast();

        log.info("AUDIT | {} | {} | {} | {} → {}", entity, action, user, before, after);
    }

    public void crud(String action, String entity, String user, Object data) {
        record(entity, action, user, "", data != null ? data.toString() : "null");
    }

    public List<Map<String, Object>> recent(int limit) {
        return events.stream().limit(limit).toList();
    }
}