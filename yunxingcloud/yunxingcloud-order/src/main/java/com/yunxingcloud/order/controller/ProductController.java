package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.dto.ProductDTO;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.service.ProductAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品管理", description = "商品查询与管理")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductAdminService productService;

    public ProductController(ProductAdminService productService) { this.productService = productService; }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) Long brandId,
                                  @RequestParam(required = false) Long minPrice,
                                  @RequestParam(required = false) Long maxPrice,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.list(categoryId, brandId, minPrice, maxPrice, sort, page, size));
    }

    @GetMapping("/hot")
    public ResponseEntity<?> hot() { return ResponseEntity.ok(productService.hot()); }

    @GetMapping("/new")
    public ResponseEntity<?> newArrivals() { return ResponseEntity.ok(productService.newArrivals()); }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) Long minPrice,
                                    @RequestParam(required = false) Long maxPrice,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.search(q, categoryId, minPrice, maxPrice, page, size));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<?> related(@PathVariable Long id) {
        return ResponseEntity.ok(productService.related(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return productService.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        var result = productService.detail(id);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.create(dto));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Product body) {
        return productService.update(id, body)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return productService.delete(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
