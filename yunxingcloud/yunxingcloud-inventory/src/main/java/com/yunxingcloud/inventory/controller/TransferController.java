package com.yunxingcloud.inventory.controller;

import com.yunxingcloud.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@Tag(name = "库存调拨", description = "仓库间库存调拨")
@RestController
@RequestMapping("/api/inventory")
public class TransferController {

    private final InventoryService service;

    public TransferController(InventoryService service) { this.service = service; }

    @PreAuthorize("hasAuthority('ticket:write')")
    @Operation(summary = "库存调拨")
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long fromWh = Long.valueOf(body.get("fromWarehouseId").toString());
        Long toWh = Long.valueOf(body.get("toWarehouseId").toString());
        int qty = Integer.parseInt(body.get("quantity").toString());
        String productName = (String) body.getOrDefault("productName", "");
        String remark = (String) body.getOrDefault("remark", "库存调拨");

        try {
            return ResponseEntity.ok(service.transfer(productId, productName, fromWh, toWh, qty, remark));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}