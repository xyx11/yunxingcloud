package com.yunxingcloud.inventory.service;

import com.yunxingcloud.inventory.entity.Warehouse;
import com.yunxingcloud.inventory.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository whRepo;

    public WarehouseService(WarehouseRepository whRepo) {
        this.whRepo = whRepo;
    }

    public List<Warehouse> findAll() {
        return whRepo.findAll();
    }

    @Transactional
    public Warehouse create(Warehouse wh) {
        return whRepo.save(wh);
    }

    @Transactional
    public Warehouse update(Long id, Warehouse wh) {
        Warehouse existing = whRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("warehouse.not_found"));
        existing.setName(wh.getName());
        existing.setAddress(wh.getAddress());
        return whRepo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!whRepo.existsById(id)) {
            throw new IllegalArgumentException("warehouse.not_found");
        }
        whRepo.deleteById(id);
    }
}