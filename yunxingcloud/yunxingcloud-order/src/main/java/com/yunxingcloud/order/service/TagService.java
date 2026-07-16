package com.yunxingcloud.order.service;

import com.yunxingcloud.order.dto.TagDTO;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.entity.ProductTag;
import com.yunxingcloud.order.entity.ProductTagRelation;
import com.yunxingcloud.order.repository.ProductRepository;
import com.yunxingcloud.order.repository.ProductTagRelationRepository;
import com.yunxingcloud.order.repository.ProductTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TagService {

    private final ProductTagRepository tagRepo;
    private final ProductTagRelationRepository relationRepo;
    private final ProductRepository productRepo;

    public TagService(ProductTagRepository tagRepo, ProductTagRelationRepository relationRepo,
                      ProductRepository productRepo) {
        this.tagRepo = tagRepo;
        this.relationRepo = relationRepo;
        this.productRepo = productRepo;
    }

    public List<ProductTag> list() { return tagRepo.findAllByOrderBySortOrderAsc(); }

    @Transactional
    public ProductTag create(TagDTO dto) {
        ProductTag t = new ProductTag();
        t.setName(dto.getName());
        return tagRepo.save(t);
    }

    public List<ProductTag> tagsOfProduct(Long productId) {
        List<ProductTagRelation> rels = relationRepo.findByProductId(productId);
        if (rels.isEmpty()) return Collections.emptyList();
        List<Long> ids = rels.stream().map(ProductTagRelation::getTagId).toList();
        return tagRepo.findAllById(ids);
    }

    @Transactional
    public ProductTagRelation addTag(Long productId, Long tagId) {
        if (relationRepo.existsByProductIdAndTagId(productId, tagId))
            return null;
        ProductTagRelation r = new ProductTagRelation();
        r.setProductId(productId);
        r.setTagId(tagId);
        return relationRepo.save(r);
    }

    @Transactional
    public void removeTag(Long productId, Long tagId) {
        relationRepo.deleteByProductIdAndTagId(productId, tagId);
    }

    public List<Product> productsByTag(Long tagId) {
        List<ProductTagRelation> rels = relationRepo.findByTagId(tagId);
        if (rels.isEmpty()) return Collections.emptyList();
        List<Long> ids = rels.stream().map(ProductTagRelation::getProductId).toList();
        return productRepo.findAllById(ids);
    }
}
