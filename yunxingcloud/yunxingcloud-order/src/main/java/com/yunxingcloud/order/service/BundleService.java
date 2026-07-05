package com.yunxingcloud.order.service;

import com.yunxingcloud.order.dto.ProductBundleDTO;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.entity.ProductBundle;
import com.yunxingcloud.order.repository.ProductBundleRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BundleService {

    private final ProductBundleRepository bundleRepo;
    private final ProductRepository productRepo;

    public BundleService(ProductBundleRepository bundleRepo, ProductRepository productRepo) {
        this.bundleRepo = bundleRepo; this.productRepo = productRepo;
    }

    public List<ProductBundle> list() { return bundleRepo.findByStatus("1"); }

    public Map<String, Object> detail(Long id) {
        ProductBundle b = bundleRepo.findById(id).orElse(null);
        if (b == null) return null;
        List<Product> items = new ArrayList<>();
        for (String pid : b.getProductIds().split(",")) {
            productRepo.findById(Long.parseLong(pid.trim())).ifPresent(items::add);
        }
        return Map.of("bundle", b, "items", items,
                "savings", (b.getOriginalPrice() != null ? b.getOriginalPrice() : 0) - b.getBundlePrice());
    }

    @Transactional
    public ProductBundle create(ProductBundleDTO dto) {
        ProductBundle b = new ProductBundle();
        b.setName(dto.getName()); b.setBundlePrice(dto.getBundlePrice()); b.setOriginalPrice(dto.getOriginalPrice());
        b.setProductIds(dto.getProductIds()); b.setImageUrl(dto.getImageUrl()); b.setStatus(dto.getStatus());
        return bundleRepo.save(b);
    }
}
