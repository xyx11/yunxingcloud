package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.CompareList;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.CompareListRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CompareService {

    private final CompareListRepository compareRepo;
    private final ProductRepository productRepo;

    public CompareService(CompareListRepository compareRepo, ProductRepository productRepo) {
        this.compareRepo = compareRepo; this.productRepo = productRepo;
    }

    public List<Map<String, Object>> list(String username) {
        List<CompareList> items = compareRepo.findByUsernameOrderByCreatedAtDesc(username);
        List<Product> products = new ArrayList<>();
        for (CompareList c : items) productRepo.findById(c.getProductId()).ifPresent(products::add);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Product p : products) {
            result.add(Map.of("id", p.getId(), "name", p.getName(),
                    "price", p.getPrice(), "stock", p.getStock(),
                    "sales", p.getSales() != null ? p.getSales() : 0,
                    "description", p.getDescription() != null ? p.getDescription() : ""));
        }
        return result;
    }

    @Transactional
    public CompareList add(String username, Long productId) {
        List<CompareList> existing = compareRepo.findByUsernameOrderByCreatedAtDesc(username);
        if (existing.size() >= 4) throw new IllegalStateException("最多对比4个商品");
        if (existing.stream().anyMatch(c -> c.getProductId().equals(productId))) return null;
        CompareList c = new CompareList(); c.setUsername(username); c.setProductId(productId);
        return compareRepo.save(c);
    }

    @Transactional
    public void remove(String username, Long productId) {
        compareRepo.deleteByUsernameAndProductId(username, productId);
    }

    @Transactional
    public void clear(String username) {
        compareRepo.deleteByUsername(username);
    }
}
