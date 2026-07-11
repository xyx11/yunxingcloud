package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.entity.GroupBuy;
import com.yunxingcloud.order.repository.GroupBuyRepository;
import com.yunxingcloud.order.service.GroupBuyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Tag(name = "拼团管理", description = "拼团活动管理")
@RestController
@RequestMapping("/api/group-buy")
public class GroupBuyController {

    private static final Logger log = LoggerFactory.getLogger(GroupBuyController.class);

    private final GroupBuyRepository groupBuyRepo;
    private final GroupBuyService service;

    public GroupBuyController(GroupBuyRepository groupBuyRepo, GroupBuyService service) {
        this.groupBuyRepo = groupBuyRepo;
        this.service = service;
    }

    private String user() { return SecurityContextHolder.getContext().getAuthentication().getName(); }

    @GetMapping
    public ResponseEntity<?> list() { return ResponseEntity.ok(groupBuyRepo.findAll()); }

    @PreAuthorize("hasAuthority('ticket:write')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody GroupBuy gb) {
        var saved = groupBuyRepo.save(gb);
        log.info("Group buy created: id={}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<?> openGroup(@PathVariable Long id, @RequestParam Long orderId) {
        var group = service.createGroup(id, orderId, user());
        log.info("User {} opened group buy {} with order {}", user(), id, orderId);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> join(@PathVariable Long groupId, @RequestParam Long orderId) {
        var result = service.joinGroup(groupId, orderId, user());
        log.info("User {} joined group buy group {} with order {}", user(), groupId, orderId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/expire")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> expireTimeout() {
        service.expireTimeoutGroups();
        log.info("Admin expired timeout group buys");
        return ResponseEntity.ok(Map.of("success", true));
    }
}