package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.entity.SysNotice;
import com.yunxingcloud.yunxingcloud.service.NoticeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Tag(name = "通知公告", description = "系统通知公告的增删改查")
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ResponseEntity<List<SysNotice>> list() {
        return ResponseEntity.ok(noticeService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SysNotice> get(@PathVariable Long id) {
        return noticeService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/latest")
    public ResponseEntity<List<SysNotice>> latest() {
        return ResponseEntity.ok(noticeService.latest());
    }

    @PreAuthorize("hasAuthority('notice:write')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<SysNotice> create(@RequestBody SysNotice notice) {
        return ResponseEntity.ok(noticeService.create(notice));
    }

    @PreAuthorize("hasAuthority('notice:write')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SysNotice body) {
        try {
            return ResponseEntity.ok(noticeService.update(id, body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAuthority('notice:write')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            noticeService.delete(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        String csv = noticeService.exportCsv();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=notices.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
