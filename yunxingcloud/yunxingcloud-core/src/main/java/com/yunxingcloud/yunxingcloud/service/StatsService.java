package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysNotice;
import com.yunxingcloud.yunxingcloud.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsService.class);

    private final UserRepository userRepository;
    private final SysMenuRepository menuRepository;
    private final SysOperLogRepository operLogRepository;
    private final SysJobRepository jobRepository;
    private final JdbcTemplate jdbcTemplate;
    private final SysConfigRepository configRepository;
    private final SysNoticeRepository noticeRepository;
    private final SysLoginInfoRepository loginInfoRepository;

    public StatsService(UserRepository userRepository,
                        SysMenuRepository menuRepository,
                        SysOperLogRepository operLogRepository,
                        SysJobRepository jobRepository,
                        JdbcTemplate jdbcTemplate,
                        SysConfigRepository configRepository,
                        SysNoticeRepository noticeRepository,
                        SysLoginInfoRepository loginInfoRepository) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.operLogRepository = operLogRepository;
        this.jobRepository = jobRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.configRepository = configRepository;
        this.noticeRepository = noticeRepository;
        this.loginInfoRepository = loginInfoRepository;
    }

    public Map<String, Object> getDashboard() {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("userCount", userRepository.count());
        data.put("menuCount", menuRepository.count());
        data.put("operLogCount", operLogRepository.count());
        data.put("jobCount", jobRepository.count());
        data.put("configCount", configRepository.count());
        data.put("noticeCount", noticeRepository.count());
        data.put("todayLoginCount", getTodayLoginCount("0"));
        data.put("todayLoginFailCount", getTodayLoginCount("1"));

        try { data.put("deptCount", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM department", Long.class)); } catch (Exception e) { data.put("deptCount", 0L); }
        try { data.put("postCount", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_post", Long.class)); } catch (Exception e) { data.put("postCount", 0L); }
        try { data.put("dictCount", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_dict_type", Long.class)); } catch (Exception e) { data.put("dictCount", 0L); }

        data.put("weeklyOps", getWeeklyOps());
        data.put("bizTypeDist", getBizTypeDistribution());
        data.put("loginTrend", getLoginTrend());

        return data;
    }

    public List<SysNotice> getRecentNotices() {
        return noticeRepository.findTop5ByStatusAndNoticeTypeOrderByCreatedAtDesc("0", "2");
    }

    private List<Map<String, Object>> getWeeklyOps() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT DATE(oper_time) as op_date, COUNT(*) as cnt FROM sys_oper_log " +
                "WHERE oper_time >= ? GROUP BY DATE(oper_time) ORDER BY op_date",
                LocalDate.now().minusDays(6).toString());
            Map<String, Long> countMap = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) {
                Object d = row.get("op_date");
                Object c = row.get("cnt");
                countMap.put(d != null ? d.toString() : "", c instanceof Number ? ((Number) c).longValue() : 0L);
            }
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                String date = LocalDate.now().minusDays(i).format(fmt);
                result.add(Map.of("date", date, "count", countMap.getOrDefault(LocalDate.now().minusDays(i).toString(), 0L)));
            }
            return result;
        } catch (Exception e) {
            log.warn("查询近7天操作量失败: {}", e.getMessage());
            return List.of();
        }
    }

    private List<Map<String, Object>> getLoginTrend() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT DATE(login_time) as dt, COUNT(*) as cnt FROM sys_logininfor WHERE login_time >= ? GROUP BY DATE(login_time) ORDER BY dt",
                LocalDate.now().minusDays(6).toString());
            Map<String, Long> map = new LinkedHashMap<>();
            for (var r : rows) { map.put(r.get("dt") != null ? r.get("dt").toString() : "", ((Number) r.get("cnt")).longValue()); }
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                String d = LocalDate.now().minusDays(i).format(fmt);
                result.add(Map.of("date", d, "count", map.getOrDefault(LocalDate.now().minusDays(i).toString(), 0L)));
            }
            return result;
        } catch (Exception e) { return List.of(); }
    }

    private long getTodayLoginCount(String status) {
        try {
            Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_logininfor WHERE status = ? AND DATE(login_time) = ?",
                Long.class, status, LocalDate.now().toString());
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private List<Map<String, Object>> getBizTypeDistribution() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT business_type, COUNT(*) as cnt FROM sys_oper_log GROUP BY business_type");
            Map<String, String> nameMap = Map.of(
                "INSERT", "新增", "UPDATE", "修改", "DELETE", "删除", "OTHER", "其他");
            return rows.stream().map(row -> {
                String type = (String) row.get("business_type");
                return Map.<String, Object>of(
                    "name", nameMap.getOrDefault(type, type),
                    "value", row.get("cnt"));
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("查询操作类型分布失败: {}", e.getMessage());
            return List.of();
        }
    }
}
