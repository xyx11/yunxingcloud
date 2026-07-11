package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysNotice;
import com.yunxingcloud.yunxingcloud.repository.SysNoticeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.yunxingcloud.common.core.CsvUtils;

@Service
public class NoticeService {

    private final SysNoticeRepository noticeRepository;

    public NoticeService(SysNoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<SysNotice> list() {
        return noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Optional<SysNotice> get(Long id) {
        return noticeRepository.findById(id);
    }

    public List<SysNotice> latest() {
        return noticeRepository.findTop5ByStatusAndNoticeTypeOrderByCreatedAtDesc("0", "2");
    }

    @Transactional
    public SysNotice create(SysNotice notice) {
        return noticeRepository.save(notice);
    }

    @Transactional
    public SysNotice update(Long id, SysNotice body) {
        return noticeRepository.findById(id).map(notice -> {
            notice.setNoticeTitle(body.getNoticeTitle());
            notice.setNoticeType(body.getNoticeType());
            notice.setNoticeContent(body.getNoticeContent());
            notice.setStatus(body.getStatus());
            notice.setRemark(body.getRemark());
            return noticeRepository.save(notice);
        }).orElseThrow(() -> new IllegalArgumentException("Notice not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        SysNotice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found: " + id));
        noticeRepository.delete(notice);
    }

    public String exportCsv() {
        List<String[]> rows = noticeRepository.findAll().stream()
                .map(n -> new String[]{n.getNoticeTitle(),
                        "1".equals(n.getNoticeType()) ? "通知" : "公告",
                        "0".equals(n.getStatus()) ? "正常" : "关闭",
                        n.getCreatedAt() != null ? n.getCreatedAt().toString() : ""})
                .toList();
        return CsvUtils.toCsv(new String[]{"标题", "类型", "状态", "创建时间"}, rows);
    }
}
