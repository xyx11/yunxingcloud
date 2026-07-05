package com.yunxingcloud.order.service;

import com.yunxingcloud.order.dto.ProductDTO;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.ProductRepository;
import com.yunxingcloud.order.repository.ProductReviewRepository;
import com.yunxingcloud.order.repository.ProductSkuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProductAdminService {

    private final ProductRepository repo;
    private final ProductSkuRepository skuRepo;
    private final ProductReviewRepository reviewRepo;

    public ProductAdminService(ProductRepository repo, ProductSkuRepository skuRepo,
                               ProductReviewRepository reviewRepo) {
        this.repo = repo; this.skuRepo = skuRepo; this.reviewRepo = reviewRepo;
    }

    public Page<Product> list(Specification<Product> spec, int page, int size, Sort sort) {
        return repo.findAll(spec, PageRequest.of(page, size, sort));
    }

    public Page<Product> search(Specification<Product> spec, int page, int size) {
        return repo.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sales")));
    }

    public List<Product> hot() {
        return repo.findByIsHotTrueAndStatus("0", Sort.by(Sort.Direction.DESC, "sales"));
    }

    public List<Product> newArrivals() {
        return repo.findByIsNewTrueAndStatus("0", Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Optional<Product> get(Long id) {
        return repo.findById(id);
    }

    public Map<String, Object> detail(Long id) {
        Product p = repo.findById(id).orElse(null);
        if (p == null) return null;
        var skus = skuRepo.findByProductId(id);
        var reviews = reviewRepo.findByProductIdOrderByCreatedAtDesc(id);
        var related = repo.findByCategoryIdAndIdNot(
                p.getCategoryId() != null ? p.getCategoryId() : 0L, id,
                PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "sales")));
        return Map.of("product", p, "skus", skus, "reviews", reviews, "related", related);
    }

    public List<Product> related(Long id) {
        Product p = repo.findById(id).orElse(null);
        if (p == null) return List.of();
        List<Product> related = new ArrayList<>();
        if (p.getCategoryId() != null) {
            related = repo.findByCategoryIdAndIdNot(p.getCategoryId(), id,
                    PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "sales")));
        }
        if (related.size() < 4) {
            related.addAll(repo.findByStatus("0", PageRequest.of(0, 8 - related.size(),
                    Sort.by(Sort.Direction.DESC, "sales"))));
        }
        return related.stream().distinct().limit(8).toList();
    }

    @Transactional
    public Product create(ProductDTO dto) {
        Product p = new Product();
        p.setName(dto.getName()); p.setDescription(dto.getDescription()); p.setPrice(dto.getPrice());
        p.setStock(dto.getStock()); p.setCategoryId(dto.getCategoryId()); p.setBrandId(dto.getBrandId());
        p.setImages(dto.getImages()); p.setImageUrl(dto.getImageUrl());
        p.setIsNew(dto.getIsNew()); p.setIsHot(dto.getIsHot()); p.setTags(dto.getTags()); p.setStatus(dto.getStatus());
        return repo.save(p);
    }

    @Transactional
    public Optional<Product> update(Long id, Product body) {
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
            return repo.save(p);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (repo.existsById(id)) { repo.deleteById(id); return true; }
        return false;
    }
}
