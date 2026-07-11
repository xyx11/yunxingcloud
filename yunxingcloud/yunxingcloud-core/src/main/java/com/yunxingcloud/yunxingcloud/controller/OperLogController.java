package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.service.OperLogService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "操作日志", description = "操作日志查询与导出")
@RestController
@RequestMapping("/api/operlog")
public class OperLogController {

    private final OperLogService operLogService;

    public OperLogController(OperLogService operLogService) {
        this.operLogService = operLogService;
    }

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
        return ResponseEntity.ok(operLogService.list(type, user, keyword, method,
                startTime, endTime, page, pageSize));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        byte[] bytes = operLogService.exportCsv(type, user, method, startTime, endTime);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=operlog.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @PreAuthorize("hasAuthority('operlog:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        operLogService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('operlog:write')")
    @DeleteMapping("/clean")
    public ResponseEntity<Map<String, Object>> clean() {
        return ResponseEntity.ok(operLogService.clean());
    }

    @PreAuthorize("hasAuthority('operlog:write')")
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.getOrDefault("ids", List.of());
        return ResponseEntity.ok(operLogService.batchDelete(ids));
    }
}
