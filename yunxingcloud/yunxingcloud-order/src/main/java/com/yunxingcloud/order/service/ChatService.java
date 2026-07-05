package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.ChatMessage;
import com.yunxingcloud.order.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ChatService {

    private final ChatMessageRepository repo;

    public ChatService(ChatMessageRepository repo) { this.repo = repo; }

    @Transactional
    public ChatMessage send(ChatMessage msg) {
        if (msg.getSessionId() == null) msg.setSessionId(UUID.randomUUID().toString().substring(0, 8));
        return repo.save(msg);
    }

    public List<ChatMessage> messages(String sessionId) {
        return repo.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    public Set<String> sessions(String username) {
        List<ChatMessage> msgs = repo.findBySenderOrReceiverOrderByCreatedAtDesc(username, username);
        Set<String> sessions = new LinkedHashSet<>();
        for (ChatMessage m : msgs) if (m.getSessionId() != null) sessions.add(m.getSessionId());
        return sessions;
    }
}
