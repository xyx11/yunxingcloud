package com.yunxingcloud.inventory.config;

import com.yunxingcloud.common.core.BaseExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(com.alibaba.csp.sentinel.slots.block.BlockException.class)
    public ResponseEntity<?> handleBlock(com.alibaba.csp.sentinel.slots.block.BlockException e) {
        log.warn("Sentinel 流控/降级: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("success", false, "message", "请求过于频繁"));
    }
}
