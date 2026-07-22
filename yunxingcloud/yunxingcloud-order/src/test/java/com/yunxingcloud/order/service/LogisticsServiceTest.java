package com.yunxingcloud.order.service;

import com.yunxingcloud.order.entity.LogisticsTrace;
import com.yunxingcloud.order.repository.LogisticsTraceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogisticsServiceTest {

    @Mock private LogisticsTraceRepository repo;
    @InjectMocks private LogisticsService service;

    @Test
    void shouldTraceByOrder() {
        LogisticsTrace t = new LogisticsTrace();
        t.setId(1L); t.setOrderId(100L); t.setStatus("已揽件");
        when(repo.findByOrderIdOrderByTraceTimeDesc(100L)).thenReturn(List.of(t));

        List<LogisticsTrace> result = service.tracesByOrder(100L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("已揽件");
    }

    @Test
    void shouldTrackByNo() {
        when(repo.findByTrackingNoOrderByTraceTimeDesc("SF123")).thenReturn(List.of());

        List<LogisticsTrace> result = service.track("SF123");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldAddTrace() {
        LogisticsTrace t = new LogisticsTrace();
        when(repo.save(any())).thenReturn(t);

        LogisticsTrace result = service.add(t);
        assertThat(result).isNotNull();
        verify(repo).save(t);
    }
}