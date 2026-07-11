package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.SysJob;
import com.yunxingcloud.yunxingcloud.entity.SysJobLog;
import com.yunxingcloud.yunxingcloud.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@Tag(name = "定时任务管理", description = "定时任务CRUD与执行控制")
@RestController
@RequestMapping("/api/job")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    private final JobService jobService;
    private final I18nService i18n;

    public JobController(JobService jobService, I18nService i18n) {
        this.jobService = jobService;
        this.i18n = i18n;
    }

    @GetMapping
    public ResponseEntity<List<SysJob>> list() {
        return ResponseEntity.ok(jobService.findAll());
    }

    @PreAuthorize("hasAuthority('job:write')")
    @Log(title = "定时任务", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<SysJob> create(@RequestBody SysJob job) {
        log.info("创建定时任务: {}", job.getJobName());
        return ResponseEntity.ok(jobService.create(job));
    }

    @PreAuthorize("hasAuthority('job:write')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<SysJob> update(@PathVariable Long id, @RequestBody SysJob body) {
        return ResponseEntity.ok(jobService.update(id, body));
    }

    @PreAuthorize("hasAuthority('job:write')")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        log.info("删除定时任务: {}", id);
        jobService.delete(id);
        return ResponseEntity.ok(Map.of("success", (Object) true));
    }

    @PreAuthorize("hasAuthority('job:write')")
    @PostMapping("/{id}/run")
    public ResponseEntity<Map<String, Object>> run(@PathVariable Long id) {
        log.info("执行定时任务: {}", id);
        jobService.run(id);
        return ResponseEntity.ok(Map.of("success", (Object) true, "message", i18n.msg("job.run_success")));
    }

    @PreAuthorize("hasAuthority('job:write')")
    @PostMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pause(@PathVariable Long id) {
        return jobService.findById(id).map(j -> {
            log.info("暂停定时任务: {}", id);
            jobService.pause(id);
            return ResponseEntity.ok(Map.of("success", (Object) true, "message", i18n.msg("job.pause_success")));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('job:write')")
    @PostMapping("/{id}/resume")
    public ResponseEntity<Map<String, Object>> resume(@PathVariable Long id) {
        return jobService.findById(id).map(j -> {
            log.info("恢复定时任务: {}", id);
            jobService.resume(id);
            return ResponseEntity.ok(Map.of("success", (Object) true, "message", i18n.msg("job.resume_success")));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<SysJobLog>> logs(@PathVariable Long id) {
        return jobService.findById(id)
                .map(j -> ResponseEntity.ok(jobService.getLogs(id)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/logs")
    public ResponseEntity<List<SysJobLog>> allLogs() {
        return ResponseEntity.ok(jobService.allLogs());
    }
}
