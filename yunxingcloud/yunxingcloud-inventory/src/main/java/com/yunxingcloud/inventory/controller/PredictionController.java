package com.yunxingcloud.inventory.controller;

import com.yunxingcloud.inventory.service.InventoryPredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "库存预测", description = "库存补货建议")
@RestController
@RequestMapping("/api/inventory")
public class PredictionController {

    private final InventoryPredictionService predictionService;

    public PredictionController(InventoryPredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @Operation(summary = "补货建议")
    @GetMapping("/reorder-suggestions")
    public ResponseEntity<?> reorderSuggestions() {
        return ResponseEntity.ok(predictionService.reorderSuggestions());
    }
}