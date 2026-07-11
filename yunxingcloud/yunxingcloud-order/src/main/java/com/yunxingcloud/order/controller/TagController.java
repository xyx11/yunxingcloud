package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.dto.TagDTO;
import com.yunxingcloud.order.entity.ProductTagRelation;
import com.yunxingcloud.order.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "标签管理", description = "商品标签管理")
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) { this.tagService = tagService; }

    @GetMapping
    public ResponseEntity<?> list() { return ResponseEntity.ok(tagService.list()); }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TagDTO dto) { return ResponseEntity.ok(tagService.create(dto)); }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> tagsOfProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(tagService.tagsOfProduct(productId));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping("/product/{productId}/{tagId}")
    public ResponseEntity<?> addTag(@PathVariable Long productId, @PathVariable Long tagId) {
        ProductTagRelation r = tagService.addTag(productId, tagId);
        if (r == null) return ResponseEntity.ok(Map.of("message", "已存在"));
        return ResponseEntity.ok(r);
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @DeleteMapping("/product/{productId}/{tagId}")
    public ResponseEntity<?> removeTag(@PathVariable Long productId, @PathVariable Long tagId) {
        tagService.removeTag(productId, tagId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/filter/{tagId}")
    public ResponseEntity<?> productsByTag(@PathVariable Long tagId) {
        return ResponseEntity.ok(tagService.productsByTag(tagId));
    }
}
