package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.entity.Approval;
import com.yunxingcloud.yunxingcloud.service.ApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "审批管理", description = "审批流程处理")
@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) { this.approvalService = approvalService; }

    private String currentUser() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping
    public ResponseEntity<List<Approval>> list() {
        return ResponseEntity.ok(approvalService.list(currentUser()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Approval>> pending() {
        return ResponseEntity.ok(approvalService.pending(currentUser()));
    }

    @PostMapping
    public ResponseEntity<Approval> create(@RequestBody Approval approval) {
        approval.setApplicant(currentUser());
        return ResponseEntity.ok(approvalService.create(approval));
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return approvalService.approve(id, body.get("remark"))
                .map(a -> ResponseEntity.ok(Map.of("success", (Object) true)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return approvalService.reject(id, body.get("remark"))
                .map(a -> ResponseEntity.ok(Map.of("success", (Object) true)))
                .orElse(ResponseEntity.notFound().build());
    }
}
