package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.OrderSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单搜索", description = "订单高级搜索")
@RestController
@RequestMapping("/api/orders")
public class OrderSearchController {

    private final OrderSearchService orderSearchService;

    public OrderSearchController(OrderSearchService orderSearchService) {
        this.orderSearchService = orderSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderSearchService.search(keyword, status, username,
                startDate, endDate, page, size));
    }
}
