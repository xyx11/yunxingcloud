package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.LiveRoom;
import com.yunxingcloud.order.entity.Product;
import com.yunxingcloud.order.repository.LiveRoomRepository;
import com.yunxingcloud.order.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LiveService {

    private final LiveRoomRepository liveRoomRepo;
    private final ProductRepository productRepo;

    public LiveService(LiveRoomRepository liveRoomRepo, ProductRepository productRepo) {
        this.liveRoomRepo = liveRoomRepo;
        this.productRepo = productRepo;
    }

    public List<LiveRoom> rooms() {
        return liveRoomRepo.findByStatus("1", Sort.by(Sort.Direction.DESC, "startTime"));
    }

    public Map<String, Object> roomDetail(Long id) {
        LiveRoom room = liveRoomRepo.findById(id).orElse(null);
        if (room == null) return null;
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
        return result;
    }

    @Transactional
    public int addView(Long id) {
        LiveRoom room = liveRoomRepo.findById(id).orElse(null);
        if (room == null) return -1;
        room.setViewerCount(room.getViewerCount() == null ? 1 : room.getViewerCount() + 1);
        liveRoomRepo.save(room);
        return room.getViewerCount();
    }
}
