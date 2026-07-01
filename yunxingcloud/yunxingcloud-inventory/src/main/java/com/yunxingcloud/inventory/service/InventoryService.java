package com.yunxingcloud.inventory.service;

import com.yunxingcloud.inventory.entity.*;
import com.yunxingcloud.inventory.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class InventoryService {

    private final StockRepository stockRepo;
    private final StockLogRepository logRepo;

    public InventoryService(StockRepository stockRepo, StockLogRepository logRepo) {
        this.stockRepo = stockRepo;
        this.logRepo = logRepo;
    }

    @Transactional
    public Stock stockIn(Long productId, String productName, Long warehouseId, int quantity, String remark) {
        Stock stock = stockRepo.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseGet(() -> { Stock s = new Stock(); s.setProductId(productId); s.setWarehouseId(warehouseId); return s; });
        stock.setProductName(productName);
        stock.setQuantity(stock.getQuantity() + quantity);
        stock = stockRepo.save(stock);
        log(productId, warehouseId, "in", quantity, null, remark);
        return stock;
    }

    @Transactional
    public Stock stockOut(Long productId, Long warehouseId, int quantity, String remark) {
        Stock stock = stockRepo.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("库存不足"));
        if (stock.getQuantity() < quantity) throw new IllegalArgumentException("库存不足，当前库存:" + stock.getQuantity());
        stock.setQuantity(stock.getQuantity() - quantity);
        stock = stockRepo.save(stock);
        log(productId, warehouseId, "out", quantity, null, remark);
        return stock;
    }

    @Transactional
    public void orderOut(Long productId, Long warehouseId, int quantity, Long orderId, String productName) {
        Stock stock = stockRepo.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("库存不存在"));
        if (stock.getQuantity() < quantity) throw new IllegalArgumentException("库存不足");
        stock.setProductName(productName);
        stock.setQuantity(stock.getQuantity() - quantity);
        stock.setReserved(stock.getReserved() + quantity);
        stockRepo.save(stock);
        log(productId, warehouseId, "order_out", quantity, orderId, "订单预占");
    }

    @Transactional
    public void orderBack(Long productId, Long warehouseId, int quantity, Long orderId) {
        Stock stock = stockRepo.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseGet(() -> { Stock s = new Stock(); s.setProductId(productId); s.setWarehouseId(warehouseId); return s; });
        stock.setQuantity(stock.getQuantity() + quantity);
        stock.setReserved(Math.max(0, stock.getReserved() - quantity));
        stockRepo.save(stock);
        log(productId, warehouseId, "order_back", quantity, orderId, "订单回退");
    }

    public List<Stock> alerts() {
        return stockRepo.findAll().stream()
                .filter(s -> s.getQuantity() <= s.getMinQuantity()).toList();
    }

    private void log(Long pid, Long wid, String type, int qty, Long orderId, String remark) {
        StockLog l = new StockLog();
        l.setProductId(pid); l.setWarehouseId(wid); l.setType(type);
        l.setQuantity(qty); l.setOrderId(orderId); l.setRemark(remark);
        logRepo.save(l);
    }
}
