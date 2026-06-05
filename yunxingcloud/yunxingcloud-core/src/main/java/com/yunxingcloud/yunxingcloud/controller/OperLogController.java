package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.entity.SysOperLog;
import com.yunxingcloud.yunxingcloud.repository.SysOperLogRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operlog")
public class OperLogController {

    private final SysOperLogRepository logRepository;

    public OperLogController(SysOperLogRepository logRepository) { this.logRepository = logRepository; }

    @GetMapping
    public ResponseEntity<List<SysOperLog>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String user) {
        List<SysOperLog> all = logRepository.findAll();
        if (type != null && !type.isEmpty()) {
            all = all.stream().filter(l -> type.equals(l.getBusinessType())).toList();
        }
        if (user != null && !user.isEmpty()) {
            all = all.stream().filter(l -> l.getOperName() != null && l.getOperName().contains(user)).toList();
        }
        all = all.stream()
                .sorted(Comparator.comparing(SysOperLog::getOperTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        List<SysOperLog> logs = logRepository.findAll();
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
