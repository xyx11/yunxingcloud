package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.entity.SysOperLog;
import com.yunxingcloud.yunxingcloud.repository.SysOperLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/operlog")
public class OperLogController {

    private final SysOperLogRepository logRepository;

    public OperLogController(SysOperLogRepository logRepository) { this.logRepository = logRepository; }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "operTime"));
        Page<SysOperLog> result;
        if (keyword != null && !keyword.isEmpty()) {
            result = logRepository.findByTitleContaining(keyword, pageable);
        } else if ((type != null && !type.isEmpty()) && (user != null && !user.isEmpty())) {
            result = logRepository.findByBusinessTypeAndOperNameContaining(type, user, pageable);
        } else if (type != null && !type.isEmpty()) {
            result = logRepository.findByBusinessType(type, pageable);
        } else if (user != null && !user.isEmpty()) {
            result = logRepository.findByOperNameContaining(user, pageable);
        } else {
            result = logRepository.findAll(pageable);
        }

        // 日期范围 + 方法筛选(Java侧过滤，操作日志量通常不大)
        var items = result.getContent().stream().filter(log -> {
            if (method != null && !method.isEmpty() && !method.equals(log.getMethod())) return false;
            if (startTime != null && log.getOperTime() != null && log.getOperTime().isBefore(java.time.LocalDateTime.parse(startTime))) return false;
            if (endTime != null && log.getOperTime() != null && log.getOperTime().isAfter(java.time.LocalDateTime.parse(endTime))) return false;
            return true;
        }).toList();

        return ResponseEntity.ok(Map.of(
            "items", items,
            "total", result.getTotalElements(),
            "page", page,
            "pageSize", pageSize
        ));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageRequest limit = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "operTime"));
        List<SysOperLog> logs;
        if (type != null && !type.isEmpty() && user != null && !user.isEmpty()) {
            logs = logRepository.findByBusinessTypeAndOperNameContaining(type, user, limit).getContent();
        } else if (type != null && !type.isEmpty()) {
            logs = logRepository.findByBusinessType(type, limit).getContent();
        } else if (user != null && !user.isEmpty()) {
            logs = logRepository.findByOperNameContaining(user, limit).getContent();
        } else {
            logs = logRepository.findAll(limit).getContent();
        }
        // 方法/日期范围过滤
        if (method != null && !method.isEmpty()) {
            logs = logs.stream().filter(l -> method.equals(l.getMethod())).collect(Collectors.toList());
        }
        if (startTime != null && !startTime.isEmpty()) {
            LocalDateTime s = LocalDateTime.parse(startTime);
            LocalDateTime e = endTime != null && !endTime.isEmpty() ? LocalDateTime.parse(endTime) : null;
            logs = logs.stream().filter(l -> l.getOperTime() != null && !l.getOperTime().isBefore(s) && (e == null || !l.getOperTime().isAfter(e))).collect(Collectors.toList());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ID,标题,业务类型,操作人,IP,URL,状态,耗时(ms),操作时间\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (SysOperLog log : logs) {
            sb.append(String.join(",",
                String.valueOf(log.getId()),
                csvEscape(log.getTitle()),
                csvEscape(log.getBusinessType()),
                csvEscape(log.getOperName()),
                csvEscape(log.getOperIp()),
                csvEscape(log.getOperUrl()),
                log.getStatus() != null && log.getStatus() == 0 ? "成功" : "失败",
                String.valueOf(log.getCostTime() != null ? log.getCostTime() : 0),
                log.getOperTime() != null ? log.getOperTime().format(fmt) : ""
            )).append("\n");
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=operlog.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    private String csvEscape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

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

    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.getOrDefault("ids", List.of());
        logRepository.deleteAllById(ids);
        return ResponseEntity.ok(Map.of("success", true, "message", "已删除 " + ids.size() + " 条日志"));
    }
}
