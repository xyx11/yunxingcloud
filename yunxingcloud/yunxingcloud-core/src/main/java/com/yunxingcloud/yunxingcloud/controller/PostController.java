package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.SysPost;
import com.yunxingcloud.yunxingcloud.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Tag(name = "岗位管理", description = "系统岗位/职位的增删改查")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final I18nService i18n;

    public PostController(PostService postService, I18nService i18n) {
        this.postService = postService;
        this.i18n = i18n;
    }

    @PreAuthorize("hasAuthority('post:read')")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(postService.list());
    }

    @PreAuthorize("hasAuthority('post:read')")
    @GetMapping("/{id}")
    public ResponseEntity<SysPost> get(@PathVariable Long id) {
        return postService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('post:write')")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SysPost post) {
        try {
            return ResponseEntity.ok(postService.create(post));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", i18n.msg(e.getMessage())));
        }
    }

    @PreAuthorize("hasAuthority('post:write')")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SysPost body) {
        try {
            return ResponseEntity.ok(postService.update(id, body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAuthority('post:write')")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            postService.delete(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAuthority('post:read')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        String csv = postService.exportCsv();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=posts.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
