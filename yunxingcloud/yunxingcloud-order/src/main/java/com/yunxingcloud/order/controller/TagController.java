package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.dto.TagDTO;
import com.yunxingcloud.order.entity.ProductTag;
import com.yunxingcloud.order.entity.*;
import com.yunxingcloud.order.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final ProductTagRepository tagRepo;
    private final ProductTagRelationRepository relationRepo;
    private final ProductRepository productRepo;

    public TagController(ProductTagRepository tagRepo, ProductTagRelationRepository relationRepo,
                         ProductRepository productRepo) {
        this.tagRepo = tagRepo; this.relationRepo = relationRepo; this.productRepo = productRepo;
    }

    @GetMapping
    public ResponseEntity<?> list() { return ResponseEntity.ok(tagRepo.findAllByOrderBySortOrderAsc()); }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TagDTO dto) { ProductTag t = new ProductTag(); t.setName(dto.getName()); return ResponseEntity.ok(tagRepo.save(t)); }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> tagsOfProduct(@PathVariable Long productId) {
        List<ProductTagRelation> rels = relationRepo.findByProductId(productId);
        List<ProductTag> tags = new ArrayList<>();
        for (ProductTagRelation r : rels) tagRepo.findById(r.getTagId()).ifPresent(tags::add);
        return ResponseEntity.ok(tags);
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping("/product/{productId}/{tagId}")
    public ResponseEntity<?> addTag(@PathVariable Long productId, @PathVariable Long tagId) {
        if (relationRepo.existsByProductIdAndTagId(productId, tagId))
            return ResponseEntity.ok(Map.of("message", "已存在"));
        ProductTagRelation r = new ProductTagRelation();
        r.setProductId(productId); r.setTagId(tagId);
        return ResponseEntity.ok(relationRepo.save(r));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @DeleteMapping("/product/{productId}/{tagId}")
    public ResponseEntity<?> removeTag(@PathVariable Long productId, @PathVariable Long tagId) {
        relationRepo.deleteByProductIdAndTagId(productId, tagId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/filter/{tagId}")
    public ResponseEntity<?> productsByTag(@PathVariable Long tagId) {
        List<ProductTagRelation> rels = relationRepo.findByTagId(tagId);
        List<Product> products = new ArrayList<>();
        for (ProductTagRelation r : rels) productRepo.findById(r.getProductId()).ifPresent(products::add);
        return ResponseEntity.ok(products);
    }
}