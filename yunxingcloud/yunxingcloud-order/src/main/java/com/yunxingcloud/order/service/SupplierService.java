package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Supplier;
import com.yunxingcloud.order.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository repo;

    public SupplierService(SupplierRepository repo) { this.repo = repo; }

    public List<Supplier> list() { return repo.findByStatus("1"); }

    @Transactional
    public Supplier create(Supplier s) { return repo.save(s); }

    @Transactional
    public Supplier update(Long id, Supplier s) { s.setId(id); return repo.save(s); }
}
