package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.LiveRoom;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.LiveRoomRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/live")
public class LiveController {

    private final LiveRoomRepository liveRoomRepo;
    private final ProductRepository productRepo;

    public LiveController(LiveRoomRepository liveRoomRepo, ProductRepository productRepo) {
        this.liveRoomRepo = liveRoomRepo;
        this.productRepo = productRepo;
    }

    /** 直播间列表 */
    @GetMapping("/rooms")
    public ResponseEntity<Map<String, Object>> rooms() {
        List<LiveRoom> rooms = liveRoomRepo.findByStatus("1", Sort.by(Sort.Direction.DESC, "startTime"));
        return ResponseEntity.ok(Map.of("rooms", rooms));
    }

    /** 直播间详情+关联商品 */
    @GetMapping("/rooms/{id}")
    public ResponseEntity<Map<String, Object>> roomDetail(@PathVariable Long id) {
        LiveRoom room = liveRoomRepo.findById(id).orElse(null);
        if (room == null) return ResponseEntity.notFound().build();

        List<Product> products = Collections.emptyList();
        if (room.getProductIds() != null && !room.getProductIds().isBlank()) {
            List<Long> ids = Arrays.stream(room.getProductIds().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).map(Long::parseLong)
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) products = productRepo.findAllById(ids);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("room", room);
        result.put("products", products);
        return ResponseEntity.ok(result);
    }

    /** 增加观看人数 */
    @PostMapping("/rooms/{id}/view")
    public ResponseEntity<Map<String, Object>> addView(@PathVariable Long id) {
        LiveRoom room = liveRoomRepo.findById(id).orElse(null);
        if (room == null) return ResponseEntity.notFound().build();

        room.setViewerCount(room.getViewerCount() == null ? 1 : room.getViewerCount() + 1);
        liveRoomRepo.save(room);

        return ResponseEntity.ok(Map.of("success", true, "viewerCount", room.getViewerCount()));
    }
}
