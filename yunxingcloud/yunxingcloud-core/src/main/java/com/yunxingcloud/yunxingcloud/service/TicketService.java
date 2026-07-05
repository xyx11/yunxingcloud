package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysTicket;
import com.yunxingcloud.yunxingcloud.repository.SysTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TicketService {

    private final SysTicketRepository ticketRepository;

    public TicketService(SysTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<SysTicket> list(boolean isAdmin, String username) {
        if (isAdmin) {
            return ticketRepository.findAll();
        }
        List<SysTicket> result = new ArrayList<>(ticketRepository.findByApplicantOrderByCreatedAtDesc(username));
        ticketRepository.findByAssigneeOrderByCreatedAtDesc(username).forEach(t -> {
            if (result.stream().noneMatch(r -> r.getId().equals(t.getId()))) result.add(t);
        });
        return result;
    }

    public Optional<SysTicket> get(Long id) {
        return ticketRepository.findById(id);
    }

    @Transactional
    public SysTicket create(SysTicket ticket) {
        return ticketRepository.save(ticket);
    }

    @Transactional
    public SysTicket update(Long id, Map<String, Object> body) {
        return ticketRepository.findById(id).map(t -> {
            if ("3".equals(t.getStatus())) throw new IllegalStateException("已关闭的工单不可修改");
            if (body.containsKey("title")) t.setTitle((String) body.get("title"));
            if (body.containsKey("content")) t.setContent((String) body.get("content"));
            if (body.containsKey("priority")) t.setPriority((String) body.get("priority"));
            if (body.containsKey("status")) t.setStatus((String) body.get("status"));
            if (body.containsKey("assignee")) t.setAssignee((String) body.get("assignee"));
            return ticketRepository.save(t);
        }).orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        ticketRepository.deleteById(id);
    }
}
