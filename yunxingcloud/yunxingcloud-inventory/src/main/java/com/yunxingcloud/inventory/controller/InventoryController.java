package com.yunxingcloud.inventory.controller;

import com.yunxingcloud.inventory.dto.WarehouseDTO;
import com.yunxingcloud.inventory.entity.Warehouse;
import com.yunxingcloud.inventory.repository.*;
import com.yunxingcloud.inventory.service.InventoryService;
import jakarta.validation.Valid;
import com.yunxingcloud.inventory.service.WarehouseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Tag(name = "库存管理", description = "入库/出库/库存查询")
@RestController
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private final WarehouseService whService;
    private final StockRepository stockRepo;
    private final StockLogRepository logRepo;
    private final InventoryService service;

    public InventoryController(WarehouseService whService, StockRepository stockRepo,
                               StockLogRepository logRepo, InventoryService service) {
        this.whService = whService; this.stockRepo = stockRepo; this.logRepo = logRepo; this.service = service;
    }

    @GetMapping("/api/warehouses")
    public ResponseEntity<?> warehouses() { return ResponseEntity.ok(whService.findAll()); }

    @PostMapping("/api/warehouses")
    public ResponseEntity<?> addWarehouse(@Valid @RequestBody WarehouseDTO dto) { Warehouse wh = new Warehouse(); wh.setName(dto.getName()); wh.setAddress(dto.getAddress()); wh.setStatus(dto.getStatus()); return ResponseEntity.ok(whService.create(wh)); }

    @Operation(summary = "查询库存")
    @GetMapping("/api/inventory")
    public ResponseEntity<?> list(@RequestParam(required = false) Long warehouseId) {
        if (warehouseId != null) return ResponseEntity.ok(stockRepo.findByWarehouseId(warehouseId));
        return ResponseEntity.ok(stockRepo.findAll());
    }

    @Operation(summary = "入库")
    @PostMapping("/api/inventory/stock-in")
    public ResponseEntity<?> stockIn(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long warehouseId = Long.valueOf(body.get("warehouseId").toString());
        int qty = Integer.parseInt(body.get("quantity").toString());
        String name = (String) body.getOrDefault("productName", "");
        String remark = (String) body.getOrDefault("remark", "手动入库");
        log.info("入库: productId={}, warehouseId={}, qty={}, remark={}", productId, warehouseId, qty, remark);
        return ResponseEntity.ok(service.stockIn(productId, name, warehouseId, qty, remark));
    }

    @Operation(summary = "出库")
    @PostMapping("/api/inventory/stock-out")
    public ResponseEntity<?> stockOut(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long warehouseId = Long.valueOf(body.get("warehouseId").toString());
        int qty = Integer.parseInt(body.get("quantity").toString());
        String remark = (String) body.getOrDefault("remark", "手动出库");
        try { log.info("出库: productId={}, warehouseId={}, qty={}, remark={}", productId, warehouseId, qty, remark); return ResponseEntity.ok(service.stockOut(productId, warehouseId, qty, remark)); }
        catch (IllegalArgumentException e) { log.warn("出库操作失败: {}", e.getMessage()); return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @Operation(summary = "库存预警")
    @GetMapping("/api/inventory/alerts")
    public ResponseEntity<?> alerts() { return ResponseEntity.ok(service.alerts()); }

    @GetMapping("/api/inventory/product/{id}")
    public ResponseEntity<?> byProduct(@PathVariable Long id) { return ResponseEntity.ok(stockRepo.findByProductId(id)); }

    @GetMapping("/api/inventory/logs")
    public ResponseEntity<?> logs(@RequestParam(required = false) Long productId) {
        if (productId != null) return ResponseEntity.ok(logRepo.findByProductIdOrderByCreatedAtDesc(productId));
        return ResponseEntity.ok(logRepo.findAll());
    }

    @PostMapping("/api/inventory/order-out")
    public ResponseEntity<?> orderOut(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long warehouseId = Long.valueOf(body.get("warehouseId").toString());
        int qty = Integer.parseInt(body.get("quantity").toString());
        Long orderId = body.containsKey("orderId") ? Long.valueOf(body.get("orderId").toString()) : null;
        String name = (String) body.getOrDefault("productName", "");
        try { log.info("订单出库: productId={}, warehouseId={}, qty={}, orderId={}", productId, warehouseId, qty, orderId); service.orderOut(productId, warehouseId, qty, orderId, name); return ResponseEntity.ok(Map.of("success", true)); }
        catch (Exception e) { log.warn("订单出库操作失败: {}", e.getMessage()); return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PostMapping("/api/inventory/order-back")
    public ResponseEntity<?> orderBack(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long warehouseId = Long.valueOf(body.get("warehouseId").toString());
        int qty = Integer.parseInt(body.get("quantity").toString());
        Long orderId = body.containsKey("orderId") ? Long.valueOf(body.get("orderId").toString()) : null;
        log.info("订单退货入库: productId={}, warehouseId={}, qty={}, orderId={}", productId, warehouseId, qty, orderId);
        service.orderBack(productId, warehouseId, qty, orderId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping(value = "/api/inventory/alerts/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter alertStream() {
        SseEmitter emitter = new SseEmitter(0L);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable push = () -> {
            try {
                emitter.send(SseEmitter.event().name("alerts").data(service.alerts()));
            } catch (Exception e) {
                scheduler.shutdown();
                try { emitter.completeWithError(e); } catch (Exception ignored) { log.warn("操作异常: {}", ignored.getMessage()); }
            }
        };

        push.run(); // 初始立即推送
        scheduler.scheduleAtFixedRate(push, 30, 30, TimeUnit.SECONDS);

        emitter.onCompletion(scheduler::shutdown);
        emitter.onTimeout(scheduler::shutdown);
        emitter.onError(e -> scheduler.shutdown());

        return emitter;
    }
}
