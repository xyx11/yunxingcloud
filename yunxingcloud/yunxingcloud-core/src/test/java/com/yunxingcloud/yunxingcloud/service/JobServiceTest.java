package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysJob;
import com.yunxingcloud.yunxingcloud.entity.SysJobLog;
import com.yunxingcloud.yunxingcloud.repository.SysJobLogRepository;
import com.yunxingcloud.yunxingcloud.repository.SysJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock private SysJobRepository jobRepository;
    @Mock private SysJobLogRepository jobLogRepository;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepository, jobLogRepository, null);
    }

    @Test void shouldFindAllJobs() {
        when(jobRepository.findAll()).thenReturn(List.of(new SysJob()));
        assertEquals(1, jobService.findAll().size());
    }

    @Test void shouldFindJobLogs() {
        SysJob job = new SysJob();
        job.setId(1L);
        job.setJobName("test");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobLogRepository.findByJobNameOrderByCreatedAtDesc("test")).thenReturn(List.of());
        assertNotNull(jobService.getLogs(1L));
    }

    @Test void shouldThrowWhenJobNotFound() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> jobService.getLogs(99L));
    }
}
