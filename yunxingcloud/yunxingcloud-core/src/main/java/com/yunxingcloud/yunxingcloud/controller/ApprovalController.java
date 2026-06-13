package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.entity.Approval;
import com.yunxingcloud.yunxingcloud.repository.ApprovalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    private final ApprovalRepository repo;

    public ApprovalController(ApprovalRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<List<Approval>> list() {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(repo.findByApplicantOrderByCreatedAtDesc(user));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Approval>> pending() {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(repo.findByApproverOrderByCreatedAtDesc(user));
    }

    @PostMapping
    public ResponseEntity<Approval> create(@RequestBody Approval approval) {
        approval.setApplicant(SecurityContextHolder.getContext().getAuthentication().getName());
        return ResponseEntity.ok(repo.save(approval));
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return repo.findById(id).map(a -> {
            a.setStatus("1"); a.setRemark(body.getOrDefault("remark", "已通过")); repo.save(a);
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return repo.findById(id).map(a -> {
            a.setStatus("2"); a.setRemark(body.getOrDefault("remark", "已驳回")); repo.save(a);
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }
}
