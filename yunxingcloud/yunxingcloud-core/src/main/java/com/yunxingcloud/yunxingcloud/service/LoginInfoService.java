package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.SysLoginInfo;
import com.yunxingcloud.yunxingcloud.repository.SysLoginInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class LoginInfoService {

    private final SysLoginInfoRepository loginInfoRepository;
    private final JdbcTemplate jdbc;
    private final I18nService i18n;

    public LoginInfoService(SysLoginInfoRepository loginInfoRepository, JdbcTemplate jdbc, I18nService i18n) {
        this.loginInfoRepository = loginInfoRepository;
        this.jdbc = jdbc;
        this.i18n = i18n;
    }

    public Map<String, Object> getStats() {
        long todaySuccess = 0, todayFail = 0, total = loginInfoRepository.count();
        try {
            String today = java.time.LocalDate.now().toString();
            todaySuccess = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM sys_logininfor WHERE status='0' AND DATE(login_time)=?", Long.class, today);
            todayFail = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM sys_logininfor WHERE status='1' AND DATE(login_time)=?", Long.class, today);
        } catch (Exception ignored) {}
        return Map.of("total", total, "todaySuccess", todaySuccess, "todayFail", todayFail);
    }

    public Map<String, Object> list(String userName, String status, int page, int pageSize) {
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "loginTime"));
        Page<SysLoginInfo> result;
        if (userName != null && !userName.isEmpty() && status != null && !status.isEmpty()) {
            result = loginInfoRepository.findByUserNameContainingAndStatus(userName, status, pageable);
        } else if (userName != null && !userName.isEmpty()) {
            result = loginInfoRepository.findByUserNameContaining(userName, pageable);
        } else if (status != null && !status.isEmpty()) {
            result = loginInfoRepository.findByStatus(status, pageable);
        } else {
            result = loginInfoRepository.findAll(pageable);
        }
        return Map.of(
                "items", result.getContent(),
                "total", result.getTotalElements(),
                "page", page,
                "pageSize", pageSize
        );
    }

    public Optional<SysLoginInfo> findById(Long id) {
        return loginInfoRepository.findById(id);
    }

    @Transactional
    public void delete(SysLoginInfo info) {
        loginInfoRepository.delete(info);
    }

    @Transactional
    public Map<String, Object> clean() {
        loginInfoRepository.deleteAll();
        return Map.of("success", true, "message", i18n.msg("loginlog.cleaned"));
    }
}
