package com.yunxingcloud.yunxingcloud.config;

import com.yunxingcloud.yunxingcloud.controller.SseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SseScheduler {

    @Autowired
    private SseController sseController;

    @Scheduled(fixedRate = 5000)
    public void pushStats() {
        sseController.trigger();
    }
}
