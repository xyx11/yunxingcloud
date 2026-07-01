package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.entity.SysTicket;
import com.yunxingcloud.yunxingcloud.repository.SysTicketRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "工单管理", description = "工单的增删改查")
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final SysTicketRepository ticketRepository;
    private final JdbcTemplate jdbc;

    public TicketController(SysTicketRepository ticketRepository, JdbcTemplate jdbc) {
        this.ticketRepository = ticketRepository;
        this.jdbc = jdbc;
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
    public ResponseEntity<List<SysTicket>> list() {
        if (isAdmin()) {
            return ResponseEntity.ok(ticketRepository.findAll());
        }
        String user = currentUser();
        List<SysTicket> result = new ArrayList<>(ticketRepository.findByApplicantOrderByCreatedAtDesc(user));
        ticketRepository.findByAssigneeOrderByCreatedAtDesc(user).forEach(t -> {
            if (result.stream().noneMatch(r -> r.getId().equals(t.getId()))) result.add(t);
        });
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('ticket:read')")
    @GetMapping("/{id}")
    public ResponseEntity<SysTicket> get(@PathVariable Long id) {
        return ticketRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @Log(title = "工单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SysTicket ticket) {
        ticket.setApplicant(currentUser());
        ticket.setStatus("0");
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "TK" + today;
        String maxNo = jdbc.queryForObject(
                "SELECT MAX(ticket_no) FROM sys_ticket WHERE ticket_no LIKE ?",
                String.class, prefix + "%");
        int seq = 1;
        if (maxNo != null && maxNo.length() >= 10) {
            try { seq = Integer.parseInt(maxNo.substring(10)) + 1; } catch (NumberFormatException ignored) {}
        }
        ticket.setTicketNo(prefix + String.format("%05d", seq));
        return ResponseEntity.ok(ticketRepository.save(ticket));
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SysTicket body) {
        return ticketRepository.findById(id).map(ticket -> {
            if ("3".equals(ticket.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "已关闭的工单无法修改"));
            }
            if (body.getTitle() != null) ticket.setTitle(body.getTitle());
            if (body.getContent() != null) ticket.setContent(body.getContent());
            if (body.getType() != null) ticket.setType(body.getType());
            if (body.getPriority() != null) ticket.setPriority(body.getPriority());
            return ResponseEntity.ok(ticketRepository.save(ticket));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @Log(title = "工单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        return ticketRepository.findById(id).map(ticket -> {
            ticketRepository.delete(ticket);
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/assign")
    public ResponseEntity<Map<String, Object>> assign(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ticketRepository.findById(id).map(ticket -> {
            ticket.setAssignee(body.get("assignee"));
            ticketRepository.save(ticket);
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ticketRepository.findById(id).map(ticket -> {
            ticket.setStatus(body.get("status"));
            ticketRepository.save(ticket);
            return ResponseEntity.ok(Map.of("success", (Object) true));
        }).orElse(ResponseEntity.notFound().build());
    }
}
