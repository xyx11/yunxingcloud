package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.Message;
import com.yunxingcloud.yunxingcloud.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository msgRepo;

    public MessageService(MessageRepository msgRepo) {
        this.msgRepo = msgRepo;
    }

    public List<Message> inbox(String username) {
        return msgRepo.findByToUserOrderByCreatedAtDesc(username);
    }

    public List<Message> sent(String username) {
        return msgRepo.findByFromUserOrderByCreatedAtDesc(username);
    }

    public long unreadCount(String username) {
        return msgRepo.countUnread(username);
    }

    @Transactional
    public Message send(Message msg) {
        return msgRepo.save(msg);
    }

    @Transactional
    public Optional<Message> markRead(Long id, String username) {
        return msgRepo.findById(id).map(m -> {
            if (!m.getToUser().equals(username)) return null;
            m.setIsRead(true);
            msgRepo.save(m);
            return m;
        });
    }

    @Transactional
    public Optional<Boolean> delete(Long id, String username) {
        return msgRepo.findById(id).map(m -> {
            if (!m.getToUser().equals(username) && !m.getFromUser().equals(username))
                return false;
            msgRepo.deleteById(id);
            return true;
        });
    }

    public Optional<Message> get(Long id) {
        return msgRepo.findById(id);
    }
}
