package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.entity.SysOperLog;
import com.yunxingcloud.yunxingcloud.repository.SysOperLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operlog")
public class OperLogController {

    private final SysOperLogRepository logRepository;

    public OperLogController(SysOperLogRepository logRepository) { this.logRepository = logRepository; }

    @GetMapping
    public ResponseEntity<List<SysOperLog>> list() { return ResponseEntity.ok(logRepository.findAll()); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        logRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/clean")
    public ResponseEntity<Map<String, Object>> clean() {
        logRepository.deleteAll();
        return ResponseEntity.ok(Map.of("success", true, "message", "日志已清空"));
    }
}
