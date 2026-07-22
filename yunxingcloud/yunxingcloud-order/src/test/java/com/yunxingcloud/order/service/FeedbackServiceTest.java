package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Feedback;
import com.yunxingcloud.order.repository.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock private FeedbackRepository repo;
    @InjectMocks private FeedbackService service;

    @Test
    void shouldSubmitFeedback() {
        Feedback fb = new Feedback(); fb.setUsername("user1"); fb.setContent("建议");
        when(repo.save(any())).thenReturn(fb);

        Feedback result = service.submit(fb);
        assertThat(result).isNotNull();
        verify(repo).save(fb);
    }

    @Test
    void shouldListFeedbackAsAdmin() {
        Feedback f = new Feedback(); f.setId(1L); f.setContent("建议");
        when(repo.findByOrderByCreatedAtDesc()).thenReturn(List.of(f));

        List<Feedback> result = service.list(true, "");
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldListFeedbackAsUser() {
        when(repo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(List.of());

        List<Feedback> result = service.list(false, "user1");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReplyToFeedback() {
        Feedback f = new Feedback(); f.setId(1L); f.setStatus("0");
        when(repo.findById(1L)).thenReturn(Optional.of(f));
        when(repo.save(any())).thenReturn(f);

        service.reply(1L, "已处理");
        assertThat(f.getStatus()).isEqualTo("1");
        assertThat(f.getReply()).isEqualTo("已处理");
        verify(repo).save(f);
    }
}