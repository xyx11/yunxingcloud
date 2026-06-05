package com.yunxingcloud.yunxingcloud.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    @Async
    @EventListener
    public void onAuditEvent(AuditEvent event) {
        log.info("[AUDIT] {} | user={} | ip={} | time={}",
                event.getType(), event.getUsername(), event.getIp(), event.getTime());
        // 可扩展：写入独立的审计日志表
    }
}
