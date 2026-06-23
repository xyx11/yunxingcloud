package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.SysPost;
import com.yunxingcloud.yunxingcloud.repository.SysPostRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "岗位管理", description = "系统岗位/职位的增删改查")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final SysPostRepository postRepository;
    private final JdbcTemplate jdbc;
    private final I18nService i18n;

    public PostController(SysPostRepository postRepository, JdbcTemplate jdbc, I18nService i18n) {
        this.postRepository = postRepository;
        this.jdbc = jdbc;
        this.i18n = i18n;
    }

    @PreAuthorize("hasAuthority('post:read')")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<Map<String, Object>> posts = jdbc.queryForList(
            "SELECT p.id, p.post_code as postCode, p.post_name as postName, p.sort_order as sortOrder, p.status, p.remark, p.created_at as createdAt, COUNT(u.id) as user_count FROM sys_post p LEFT JOIN users u ON p.id = u.post_id GROUP BY p.id ORDER BY p.sort_order");
        return ResponseEntity.ok(posts);
    }

    @PreAuthorize("hasAuthority('post:read')")
    @GetMapping("/{id}")
    public ResponseEntity<SysPost> get(@PathVariable Long id) {
        return postRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('post:write')")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SysPost post) {
        if (postRepository.existsByPostCode(post.getPostCode())) {
            return ResponseEntity.badRequest().body(Map.of("message", i18n.msg("post.code_exists")));
        }
        return ResponseEntity.ok(postRepository.save(post));
    }

    @PreAuthorize("hasAuthority('post:write')")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SysPost body) {
        return postRepository.findById(id).map(post -> {
            post.setPostCode(body.getPostCode());
            post.setPostName(body.getPostName());
            post.setSortOrder(body.getSortOrder());
            post.setStatus(body.getStatus());
            post.setRemark(body.getRemark());
            return ResponseEntity.ok(postRepository.save(post));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('post:write')")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        return postRepository.findById(id).map(post -> {
            postRepository.delete(post);
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('post:read')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        StringBuilder sb = new StringBuilder("岗位编码,岗位名称,排序,状态\n");
        postRepository.findAll().forEach(p -> sb.append(String.format("%s,%s,%d,%s\n", p.getPostCode(), p.getPostName(), p.getSortOrder() != null ? p.getSortOrder() : 0, "0".equals(p.getStatus()) ? "正常" : "停用")));
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=posts.csv").header("Content-Type", "text/csv; charset=UTF-8").body(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
