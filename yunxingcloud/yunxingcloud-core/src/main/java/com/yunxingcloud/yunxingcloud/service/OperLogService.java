package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.SysOperLog;
import com.yunxingcloud.yunxingcloud.repository.SysOperLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.yunxingcloud.common.core.CsvUtils;

@Service
public class OperLogService {

    private final SysOperLogRepository logRepository;
    private final I18nService i18n;

    public OperLogService(SysOperLogRepository logRepository, I18nService i18n) {
        this.logRepository = logRepository;
        this.i18n = i18n;
    }

    public Map<String, Object> list(String type, String user, String keyword, String method,
                                     String startTime, String endTime, int page, int pageSize) {
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "operTime"));
        Page<SysOperLog> result;
        if (keyword != null && !keyword.isEmpty()) {
            result = logRepository.findByTitleContaining(keyword, pageable);
        } else if ((type != null && !type.isEmpty()) && (user != null && !user.isEmpty())) {
            result = logRepository.findByBusinessTypeAndOperNameContaining(type, user, pageable);
        } else if (type != null && !type.isEmpty()) {
            result = logRepository.findByBusinessType(type, pageable);
        } else if (user != null && !user.isEmpty()) {
            result = logRepository.findByOperNameContaining(user, pageable);
        } else {
            result = logRepository.findAll(pageable);
        }

        var items = result.getContent().stream().filter(log -> {
            if (method != null && !method.isEmpty() && !method.equals(log.getMethod())) return false;
            if (startTime != null && log.getOperTime() != null && log.getOperTime().isBefore(LocalDateTime.parse(startTime))) return false;
            if (endTime != null && log.getOperTime() != null && log.getOperTime().isAfter(LocalDateTime.parse(endTime))) return false;
            return true;
        }).toList();

        return Map.of(
            "items", items,
            "total", result.getTotalElements(),
            "page", page,
            "pageSize", pageSize
        );
    }

    public byte[] exportCsv(String type, String user, String method, String startTime, String endTime) {
        PageRequest limit = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "operTime"));
        List<SysOperLog> logs;
        if (type != null && !type.isEmpty() && user != null && !user.isEmpty()) {
            logs = logRepository.findByBusinessTypeAndOperNameContaining(type, user, limit).getContent();
        } else if (type != null && !type.isEmpty()) {
            logs = logRepository.findByBusinessType(type, limit).getContent();
        } else if (user != null && !user.isEmpty()) {
            logs = logRepository.findByOperNameContaining(user, limit).getContent();
        } else {
            logs = logRepository.findAll(limit).getContent();
        }
        if (method != null && !method.isEmpty()) {
            logs = logs.stream().filter(l -> method.equals(l.getMethod())).collect(Collectors.toList());
        }
        if (startTime != null && !startTime.isEmpty()) {
            LocalDateTime s = LocalDateTime.parse(startTime);
            LocalDateTime e = endTime != null && !endTime.isEmpty() ? LocalDateTime.parse(endTime) : null;
            logs = logs.stream().filter(l -> l.getOperTime() != null && !l.getOperTime().isBefore(s) && (e == null || !l.getOperTime().isAfter(e))).collect(Collectors.toList());
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<String[]> rows = logs.stream()
                .map(log -> new String[]{
                        String.valueOf(log.getId()),
                        log.getTitle(),
                        log.getBusinessType(),
                        log.getOperName(),
                        log.getOperIp(),
                        log.getOperUrl(),
                        log.getStatus() != null && log.getStatus() == 0 ? "Success" : "Failure",
                        String.valueOf(log.getCostTime() != null ? log.getCostTime() : 0),
                        log.getOperTime() != null ? log.getOperTime().format(fmt) : ""
                })
                .toList();
        return CsvUtils.toCsv(new String[]{"ID", "Title", "BizType", "Operator", "IP", "URL", "Status", "Cost(ms)", "OperTime"}, rows).getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public void delete(Long id) {
        logRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> clean() {
        logRepository.deleteAll();
        return Map.of("success", true, "message", i18n.msg("operlog.cleaned"));
    }

    @Transactional
    public Map<String, Object> batchDelete(List<Long> ids) {
        logRepository.deleteAllById(ids);
        return Map.of("success", true, "message", i18n.msg("operlog.batch_delete", ids.size()));
    }
}
