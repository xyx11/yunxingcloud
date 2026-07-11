package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Article;
import com.yunxingcloud.order.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "文章管理", description = "帮助文章/公告管理")
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) { this.articleService = articleService; }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "1") String status) {
        return ResponseEntity.ok(articleService.list(status));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> byCategory(@PathVariable String category) {
        return ResponseEntity.ok(articleService.byCategory(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return articleService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Article article) { return ResponseEntity.ok(articleService.create(article)); }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Article article) {
        return ResponseEntity.ok(articleService.update(id, article));
    }
}
