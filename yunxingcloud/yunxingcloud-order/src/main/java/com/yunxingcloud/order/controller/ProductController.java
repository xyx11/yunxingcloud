package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.dto.ProductDTO;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.ProductRepository;
import com.yunxingcloud.order.repository.ProductReviewRepository;
import com.yunxingcloud.order.repository.ProductSkuRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository repo;
    private final ProductSkuRepository skuRepo;
    private final ProductReviewRepository reviewRepo;

    public ProductController(ProductRepository repo, ProductSkuRepository skuRepo, ProductReviewRepository reviewRepo) {
        this.repo = repo; this.skuRepo = skuRepo; this.reviewRepo = reviewRepo;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) Long brandId,
                                  @RequestParam(required = false) Long minPrice,
                                  @RequestParam(required = false) Long maxPrice,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        Sort s;
        if ("price_asc".equals(sort)) s = Sort.by("price");
        else if ("price_desc".equals(sort)) s = Sort.by(Sort.Direction.DESC, "price");
        else if ("sales".equals(sort)) s = Sort.by(Sort.Direction.DESC, "sales");
        else if ("newest".equals(sort)) s = Sort.by(Sort.Direction.DESC, "createdAt");
        else s = Sort.by(Sort.Direction.DESC, "createdAt");

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "0"));
            if (categoryId != null) predicates.add(cb.equal(root.get("categoryId"), categoryId));
            if (brandId != null) predicates.add(cb.equal(root.get("brandId"), brandId));
            if (minPrice != null) predicates.add(cb.ge(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.le(root.get("price"), maxPrice));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> result = repo.findAll(spec, PageRequest.of(page, size, s));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/hot")
    public ResponseEntity<?> hot() {
        return ResponseEntity.ok(repo.findByIsHotTrueAndStatus("0", Sort.by(Sort.Direction.DESC, "sales")));
    }

    @GetMapping("/new")
    public ResponseEntity<?> newArrivals() {
        return ResponseEntity.ok(repo.findByIsNewTrueAndStatus("0",
                Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) Long minPrice,
                                    @RequestParam(required = false) Long maxPrice,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "0"));
            if (q != null && !q.isBlank())
                predicates.add(cb.like(root.get("name"), "%" + q.trim() + "%"));
            if (categoryId != null) predicates.add(cb.equal(root.get("categoryId"), categoryId));
            if (minPrice != null) predicates.add(cb.ge(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.le(root.get("price"), maxPrice));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return ResponseEntity.ok(repo.findAll(spec, PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "sales"))));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<?> related(@PathVariable Long id) {
        Product p = repo.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        List<Product> related = new ArrayList<>();
        if (p.getCategoryId() != null) {
            related = repo.findByCategoryIdAndIdNot(p.getCategoryId(), id,
                    PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "sales")));
        }
        if (related.size() < 4) {
            related.addAll(repo.findByStatus("0", PageRequest.of(0, 8 - related.size(),
                    Sort.by(Sort.Direction.DESC, "sales"))));
        }
        return ResponseEntity.ok(related.stream().distinct().limit(8).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        Product p = repo.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        var skus = skuRepo.findByProductId(id);
        var reviews = reviewRepo.findByProductIdOrderByCreatedAtDesc(id);
        var related = repo.findByCategoryIdAndIdNot(p.getCategoryId() != null ? p.getCategoryId() : 0L, id,
                PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "sales")));
        return ResponseEntity.ok(Map.of(
            "product", p, "skus", skus, "reviews", reviews, "related", related
        ));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProductDTO dto) {
        Product p = new Product();
        p.setName(dto.getName()); p.setDescription(dto.getDescription()); p.setPrice(dto.getPrice());
        p.setStock(dto.getStock()); p.setCategoryId(dto.getCategoryId()); p.setBrandId(dto.getBrandId());
        p.setImages(dto.getImages()); p.setImageUrl(dto.getImageUrl());
        p.setIsNew(dto.getIsNew()); p.setIsHot(dto.getIsHot()); p.setTags(dto.getTags()); p.setStatus(dto.getStatus());
        return ResponseEntity.ok(repo.save(p));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Product body) {
        return repo.findById(id).map(p -> {
            if (body.getName() != null) p.setName(body.getName());
            if (body.getDescription() != null) p.setDescription(body.getDescription());
            if (body.getPrice() != null) p.setPrice(body.getPrice());
            if (body.getStock() != null) p.setStock(body.getStock());
            if (body.getImageUrl() != null) p.setImageUrl(body.getImageUrl());
            if (body.getImages() != null) p.setImages(body.getImages());
            if (body.getCategoryId() != null) p.setCategoryId(body.getCategoryId());
            if (body.getBrandId() != null) p.setBrandId(body.getBrandId());
            if (body.getSales() != null) p.setSales(body.getSales());
            if (body.getIsNew() != null) p.setIsNew(body.getIsNew());
            if (body.getIsHot() != null) p.setIsHot(body.getIsHot());
            if (body.getTags() != null) p.setTags(body.getTags());
            if (body.getStatus() != null) p.setStatus(body.getStatus());
            return ResponseEntity.ok(repo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (repo.existsById(id)) { repo.deleteById(id); return ResponseEntity.ok().build(); }
        return ResponseEntity.notFound().build();
    }
}
