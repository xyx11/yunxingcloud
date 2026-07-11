package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysNotice;
import com.yunxingcloud.yunxingcloud.repository.SysNoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private SysNoticeRepository noticeRepository;

    @InjectMocks
    private NoticeService noticeService;

    private SysNotice sampleNotice;

    @BeforeEach
    void setUp() {
        sampleNotice = new SysNotice();
        sampleNotice.setId(1L);
        sampleNotice.setNoticeTitle("测试公告");
        sampleNotice.setNoticeType("1");
        sampleNotice.setNoticeContent("测试内容");
        sampleNotice.setStatus("0");
        sampleNotice.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
    }

    @Test
    void shouldListAllNotices() {
        when(noticeRepository.findAll(any(Sort.class))).thenReturn(List.of(sampleNotice));
        var result = noticeService.list();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNoticeTitle()).isEqualTo("测试公告");
        verify(noticeRepository).findAll(any(Sort.class));
    }

    @Test
    void shouldGetNoticeById() {
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(sampleNotice));
        var result = noticeService.get(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getNoticeTitle()).isEqualTo("测试公告");
    }

    @Test
    void shouldReturnEmptyWhenNoticeNotFound() {
        when(noticeRepository.findById(99L)).thenReturn(Optional.empty());
        var result = noticeService.get(99L);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetLatestNotices() {
        when(noticeRepository.findTop5ByStatusAndNoticeTypeOrderByCreatedAtDesc("0", "2"))
                .thenReturn(List.of(sampleNotice));
        var result = noticeService.latest();
        assertThat(result).hasSize(1);
        verify(noticeRepository).findTop5ByStatusAndNoticeTypeOrderByCreatedAtDesc("0", "2");
    }

    @Test
    void shouldCreateNotice() {
        when(noticeRepository.save(sampleNotice)).thenReturn(sampleNotice);
        var result = noticeService.create(sampleNotice);
        assertThat(result).isNotNull();
        assertThat(result.getNoticeTitle()).isEqualTo("测试公告");
        verify(noticeRepository, times(1)).save(sampleNotice);
    }

    @Test
    void shouldUpdateNotice() {
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(sampleNotice));
        when(noticeRepository.save(any())).thenReturn(sampleNotice);

        var body = new SysNotice();
        body.setNoticeTitle("更新标题");
        body.setNoticeType("2");
        body.setNoticeContent("更新内容");
        body.setStatus("1");
        body.setRemark("更新备注");

        var result = noticeService.update(1L, body);

        assertThat(result.getNoticeTitle()).isEqualTo("更新标题");
        assertThat(result.getNoticeType()).isEqualTo("2");
        assertThat(result.getNoticeContent()).isEqualTo("更新内容");
        assertThat(result.getStatus()).isEqualTo("1");
        assertThat(result.getRemark()).isEqualTo("更新备注");
        verify(noticeRepository).save(sampleNotice);
    }

    @Test
    void shouldThrowWhenUpdateNonExistentNotice() {
        when(noticeRepository.findById(99L)).thenReturn(Optional.empty());
        var body = new SysNotice();
        assertThatThrownBy(() -> noticeService.update(99L, body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notice not found: 99");
    }

    @Test
    void shouldDeleteNotice() {
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(sampleNotice));
        noticeService.delete(1L);
        verify(noticeRepository).delete(sampleNotice);
    }

    @Test
    void shouldExportCsv() {
        sampleNotice.setNoticeTitle("测试公告");
        sampleNotice.setNoticeType("1");
        sampleNotice.setStatus("0");
        sampleNotice.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

        when(noticeRepository.findAll()).thenReturn(List.of(sampleNotice));
        var csv = noticeService.exportCsv();
        assertThat(csv).contains("标题,类型,状态,创建时间");
        assertThat(csv).contains("测试公告");
        assertThat(csv).contains("通知");
        assertThat(csv).contains("正常");
    }
}
