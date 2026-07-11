package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.SysJob;
import com.yunxingcloud.yunxingcloud.entity.SysJobLog;
import com.yunxingcloud.yunxingcloud.entity.SysOperLog;
import com.yunxingcloud.yunxingcloud.repository.SysJobLogRepository;
import com.yunxingcloud.yunxingcloud.repository.SysJobRepository;
import com.yunxingcloud.yunxingcloud.repository.SysOperLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Tag(name = "定时任务管理", description = "定时任务CRUD与执行控制")
@RestController
@RequestMapping("/api/job")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);
    private final SysJobRepository jobRepository;
    private final SysOperLogRepository operLogRepository;
    private final SysJobLogRepository jobLogRepository;
    private final Scheduler scheduler;
    private final I18nService i18n;

    public JobController(SysJobRepository jobRepository, SysOperLogRepository operLogRepository,
                         SysJobLogRepository jobLogRepository, Scheduler scheduler,
                         I18nService i18n) {
        this.jobRepository = jobRepository;
        this.operLogRepository = operLogRepository;
        this.jobLogRepository = jobLogRepository;
        this.scheduler = scheduler;
        this.i18n = i18n;
    }

    @GetMapping
    public ResponseEntity<List<SysJob>> list() {
        return ResponseEntity.ok(jobRepository.findAll());
    }

    @PreAuthorize("hasAuthority('job:write')")
    @Log(title = "定时任务", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<SysJob> create(@RequestBody SysJob job) {
        SysJob saved = jobRepository.save(job);
        if (scheduler != null && "0".equals(job.getStatus())) {
            scheduleJob(saved);
        }
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAuthority('job:write')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<SysJob> update(@PathVariable Long id, @RequestBody SysJob body) {
        return jobRepository.findById(id).map(j -> {
            j.setJobName(body.getJobName());
            j.setJobGroup(body.getJobGroup());
            j.setInvokeTarget(body.getInvokeTarget());
            j.setCronExpression(body.getCronExpression());
            j.setMisfirePolicy(body.getMisfirePolicy());
            j.setConcurrent(body.getConcurrent());
            j.setStatus(body.getStatus());
            j.setRemark(body.getRemark());
            j.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(jobRepository.save(j));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('job:write')")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        jobRepository.findById(id).ifPresent(j -> {
            try {
                if (scheduler != null) scheduler.deleteJob(new JobKey(j.getJobName(), j.getJobGroup()));
            } catch (Exception e) { log.warn("删除Job调度失败: {}", e.getMessage()); }
        });
        jobRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", (Object) true));
    }

    @PreAuthorize("hasAuthority('job:write')")
    @PostMapping("/{id}/run")
    public ResponseEntity<Map<String, Object>> run(@PathVariable Long id) {
        jobRepository.findById(id).ifPresent(j -> {
            LocalDateTime start = LocalDateTime.now();
            SysJobLog jobLog = new SysJobLog();
            jobLog.setJobName(j.getJobName());
            jobLog.setJobGroup(j.getJobGroup());
            jobLog.setInvokeTarget(j.getInvokeTarget());
            jobLog.setStartTime(start);
            try {
                if (scheduler != null) {
                    scheduler.triggerJob(new JobKey(j.getJobName(), j.getJobGroup()));
                }
                jobLog.setStatus("0");
                jobLog.setResult("执行成功");
                jobLog.setEndTime(LocalDateTime.now());
            } catch (Exception e) {
                jobLog.setStatus("1");
                jobLog.setResult("执行失败: " + e.getMessage());
                jobLog.setEndTime(LocalDateTime.now());
            }
            jobLogRepository.save(jobLog);
        });
        return ResponseEntity.ok(Map.of("success", (Object) true, "message", i18n.msg("job.run_success")));
    }

    @PreAuthorize("hasAuthority('job:write')")
    @PostMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pause(@PathVariable Long id) {
        return jobRepository.findById(id).map(j -> {
            j.setStatus("1");
            jobRepository.save(j);
            try {
                if (scheduler != null) scheduler.pauseJob(new JobKey(j.getJobName(), j.getJobGroup()));
            } catch (Exception e) { log.warn("暂停失败: {}", e.getMessage()); }
            return ResponseEntity.ok(Map.of("success", (Object) true, "message", i18n.msg("job.pause_success")));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('job:write')")
    @PostMapping("/{id}/resume")
    public ResponseEntity<Map<String, Object>> resume(@PathVariable Long id) {
        return jobRepository.findById(id).map(j -> {
            j.setStatus("0");
            jobRepository.save(j);
            try {
                if (scheduler != null) scheduler.resumeJob(new JobKey(j.getJobName(), j.getJobGroup()));
            } catch (Exception e) { log.warn("恢复失败: {}", e.getMessage()); }
            return ResponseEntity.ok(Map.of("success", (Object) true, "message", i18n.msg("job.resume_success")));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<SysJobLog>> logs(@PathVariable Long id) {
        return jobRepository.findById(id)
                .map(j -> ResponseEntity.ok(jobLogRepository.findByJobNameOrderByCreatedAtDesc(j.getJobName())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/logs")
    public ResponseEntity<List<SysJobLog>> allLogs() {
        return ResponseEntity.ok(jobLogRepository.findTop50ByOrderByCreatedAtDesc());
    }

    private void scheduleJob(SysJob job) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(SimpleJob.class)
                    .withIdentity(job.getJobName(), job.getJobGroup()).build();
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getJobName(), job.getJobGroup())
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.warn("创建Job调度失败: {}", e.getMessage());
        }
    }

    public static class SimpleJob implements Job {
        private static final Logger jobLog = LoggerFactory.getLogger(SimpleJob.class);
        @Override
        public void execute(JobExecutionContext context) {
            JobKey key = context.getJobDetail().getKey();
            jobLog.info("定时任务触发: {}.{}, 时间: {}, 上次触发: {}",
                key.getGroup(), key.getName(),
                context.getFireTime(), context.getPreviousFireTime());
        }
    }
}
