package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Feedback;
import com.yunxingcloud.order.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository repo;

    public FeedbackService(FeedbackRepository repo) { this.repo = repo; }

    @Transactional
    public Feedback submit(Feedback fb) { return repo.save(fb); }

    public List<Feedback> list(boolean isAdmin, String username) {
        if (isAdmin) return repo.findByOrderByCreatedAtDesc();
        return repo.findByUsernameOrderByCreatedAtDesc(username);
    }

    @Transactional
    public void reply(Long id, String reply) {
        repo.findById(id).ifPresent(f -> { f.setReply(reply); f.setStatus("1"); repo.save(f); });
    }
}
