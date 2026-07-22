package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.Notification;
import com.yunxingcloud.order.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @Mock private NotificationRepository repo;
    @InjectMocks private NotificationService service;

    @BeforeEach
    void setUp() {
        Notification n = new Notification();
        n.setId(1L);
        n.setUsername("user1");
        n.setTitle("Test");
        n.setIsRead(false);
        when(repo.findByUsernameOrUsernameOrderByCreatedAtDesc("user1", "ALL"))
            .thenReturn(List.of(n));
        when(repo.countByUsernameAndIsReadFalse("user1")).thenReturn(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(n));
    }

    @Test
    void shouldGetNotifications() {
        List<Notification> result = service.get("user1");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test");
    }

    @Test
    void shouldGetUnreadCount() {
        long count = service.unreadCount("user1");
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void shouldMarkAsRead() {
        service.markRead(1L);
        verify(repo).save(argThat(n -> n.getIsRead()));
    }

    @Test
    void shouldMarkAllRead() {
        service.markAllRead("user1");
        verify(repo).saveAll(any());
    }

    @Test
    void shouldSendNotification() {
        Notification n = new Notification();
        n.setTitle("New");
        when(repo.save(any())).thenReturn(n);

        Notification result = service.send(n);
        assertThat(result.getTitle()).isEqualTo("New");
        verify(repo).save(n);
    }
}