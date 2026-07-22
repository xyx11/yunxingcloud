package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Notification;
import com.yunxingcloud.order.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) { this.repo = repo; }

    public List<Notification> get(String username) {
        return repo.findByUsernameOrUsernameOrderByCreatedAtDesc(username, "ALL");
    }

    public long unreadCount(String username) {
        return repo.countByUsernameAndIsReadFalse(username);
    }

    @Transactional
    public void markRead(Long id) {
        repo.findById(id).ifPresent(n -> { n.setIsRead(true); n.setReadAt(LocalDateTime.now()); repo.save(n); });
    }

    @Transactional
    public void markAllRead(String username) {
        var unread = repo.findByUsernameOrUsernameOrderByCreatedAtDesc(username, "ALL").stream()
                .filter(n -> !n.getIsRead()).toList();
        unread.forEach(n -> { n.setIsRead(true); n.setReadAt(LocalDateTime.now()); });
        if (!unread.isEmpty()) repo.saveAll(unread);
    }

    @Transactional
    public Notification send(Notification notif) { return repo.save(notif); }

    public List<Notification> getByType(String username, String type) {
        return repo.findByUsernameOrUsernameOrderByCreatedAtDesc(username, "ALL").stream()
            .filter(n -> type.equals(n.getType())).toList();
    }

    @Transactional
    public void delete(Long id) { repo.deleteById(id); }

    /** 批量清理已读通知 */
    @Transactional
    public int cleanRead(String username) {
        var read = repo.findByUsernameOrUsernameOrderByCreatedAtDesc(username, "ALL").stream()
            .filter(n -> n.getIsRead()).toList();
        read.forEach(n -> repo.delete(n));
        return read.size();
    }
}
