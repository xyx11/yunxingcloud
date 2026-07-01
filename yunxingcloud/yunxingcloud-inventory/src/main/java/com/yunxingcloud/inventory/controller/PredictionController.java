package com.yunxingcloud.inventory.controller;

import com.yunxingcloud.inventory.service.InventoryPredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class PredictionController {

    private final InventoryPredictionService predictionService;

    public PredictionController(InventoryPredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/reorder-suggestions")
    public ResponseEntity<?> reorderSuggestions() {
        return ResponseEntity.ok(predictionService.reorderSuggestions());
    }
}