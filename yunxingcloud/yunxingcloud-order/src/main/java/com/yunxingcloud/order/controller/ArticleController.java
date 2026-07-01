package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.Article;
import com.yunxingcloud.order.repository.ArticleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleRepository repo;

    public ArticleController(ArticleRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "1") String status) {
        return ResponseEntity.ok(repo.findByStatusOrderByPublishAtDesc(status));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> byCategory(@PathVariable String category) {
        return ResponseEntity.ok(repo.findByCategoryAndStatus(category, "1"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Article a = repo.findById(id).orElse(null);
        if (a != null && "1".equals(a.getStatus())) {
            a.setViewCount(a.getViewCount() + 1);
            repo.save(a);
        }
        return a != null ? ResponseEntity.ok(a) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Article article) { return ResponseEntity.ok(repo.save(article)); }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id); return ResponseEntity.ok(repo.save(article));
    }
}