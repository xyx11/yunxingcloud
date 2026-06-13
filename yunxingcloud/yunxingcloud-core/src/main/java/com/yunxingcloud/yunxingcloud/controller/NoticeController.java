package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.entity.SysNotice;
import com.yunxingcloud.yunxingcloud.repository.SysNoticeRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "通知公告", description = "系统通知公告的增删改查")
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final SysNoticeRepository noticeRepository;

    public NoticeController(SysNoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @GetMapping
    public ResponseEntity<List<SysNotice>> list() {
        return ResponseEntity.ok(noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SysNotice> get(@PathVariable Long id) {
        return noticeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/latest")
    public ResponseEntity<List<SysNotice>> latest() {
        return ResponseEntity.ok(
                noticeRepository.findTop5ByStatusAndNoticeTypeOrderByCreatedAtDesc("0", "2"));
    }

    @PreAuthorize("hasAuthority('notice:write')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<SysNotice> create(@RequestBody SysNotice notice) {
        return ResponseEntity.ok(noticeRepository.save(notice));
    }

    @PreAuthorize("hasAuthority('notice:write')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SysNotice body) {
        return noticeRepository.findById(id).map(notice -> {
            notice.setNoticeTitle(body.getNoticeTitle());
            notice.setNoticeType(body.getNoticeType());
            notice.setNoticeContent(body.getNoticeContent());
            notice.setStatus(body.getStatus());
            notice.setRemark(body.getRemark());
            return ResponseEntity.ok(noticeRepository.save(notice));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('notice:write')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        return noticeRepository.findById(id).map(notice -> {
            noticeRepository.delete(notice);
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        StringBuilder sb = new StringBuilder("标题,类型,状态,创建时间\n");
        noticeRepository.findAll().forEach(n -> sb.append(String.format("%s,%s,%s,%s\n", n.getNoticeTitle(), "1".equals(n.getNoticeType()) ? "通知" : "公告", "0".equals(n.getStatus()) ? "正常" : "关闭", n.getCreatedAt())));
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=notices.csv").header("Content-Type", "text/csv; charset=UTF-8").body(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
