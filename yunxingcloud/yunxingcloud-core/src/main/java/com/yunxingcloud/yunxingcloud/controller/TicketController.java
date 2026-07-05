package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.entity.SysTicket;
import com.yunxingcloud.yunxingcloud.service.TicketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "工单管理", description = "工单的增删改查")
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("admin"::equals);
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PreAuthorize("hasAuthority('ticket:read')")
    @GetMapping
    public ResponseEntity<java.util.List<SysTicket>> list() {
        return ResponseEntity.ok(ticketService.list(isAdmin(), currentUser()));
    }

    @PreAuthorize("hasAuthority('ticket:read')")
    @GetMapping("/{id}")
    public ResponseEntity<SysTicket> get(@PathVariable Long id) {
        return ticketService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @Log(title = "工单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<SysTicket> create(@RequestBody SysTicket ticket) {
        ticket.setApplicant(currentUser());
        ticket.setStatus("0");
        return ResponseEntity.ok(ticketService.create(ticket));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(ticketService.update(id, body));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @Log(title = "工单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
