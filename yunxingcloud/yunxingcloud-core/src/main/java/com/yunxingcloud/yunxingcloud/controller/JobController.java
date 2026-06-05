package com.yunxingcloud.yunxingcloud.controller;
import org.springframework.security.access.prepost.PreAuthorize;

import com.yunxingcloud.common.annotation.Log;
import com.yunxingcloud.common.enums.BusinessType;
import com.yunxingcloud.yunxingcloud.entity.SysJob;
import com.yunxingcloud.yunxingcloud.repository.SysJobRepository;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job")
public class JobController {

    private final SysJobRepository jobRepository;

    @Autowired(required = false)
    private Scheduler scheduler;

    public JobController(SysJobRepository jobRepository) { this.jobRepository = jobRepository; }

    @GetMapping
    public ResponseEntity<List<SysJob>> list() { return ResponseEntity.ok(jobRepository.findAll()); }

    @PreAuthorize("hasAuthority('job:write')")
    @Log(title = "定时任务", businessType = BusinessType.INSERT)
    @PostMapping
    public ResponseEntity<SysJob> create(@RequestBody SysJob job) throws SchedulerException {
        SysJob saved = jobRepository.save(job);
        if (scheduler != null && "0".equals(job.getStatus())) {
            JobDetail jobDetail = JobBuilder.newJob(SimpleJob.class)
                    .withIdentity(job.getJobName(), job.getJobGroup()).build();
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getJobName(), job.getJobGroup())
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        }
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAuthority('job:write')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<SysJob> update(@PathVariable Long id, @RequestBody SysJob body) throws SchedulerException {
        return jobRepository.findById(id).map(j -> {
            j.setJobName(body.getJobName()); j.setJobGroup(body.getJobGroup());
            j.setInvokeTarget(body.getInvokeTarget()); j.setCronExpression(body.getCronExpression());
            j.setMisfirePolicy(body.getMisfirePolicy()); j.setConcurrent(body.getConcurrent());
            j.setStatus(body.getStatus()); j.setRemark(body.getRemark());
            j.setUpdatedAt(java.time.LocalDateTime.now());
            return ResponseEntity.ok(jobRepository.save(j));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('job:write')")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) throws SchedulerException {
        jobRepository.findById(id).ifPresent(j -> {
            try {
                if (scheduler != null) scheduler.deleteJob(new JobKey(j.getJobName(), j.getJobGroup()));
            } catch (Exception ignored) {}
        });
        jobRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PreAuthorize("hasAuthority('job:write')")
    @PostMapping("/{id}/run")
    public ResponseEntity<Map<String, Object>> run(@PathVariable Long id) throws SchedulerException {
        jobRepository.findById(id).ifPresent(j -> {
            if (scheduler != null) {
                try { scheduler.triggerJob(new JobKey(j.getJobName(), j.getJobGroup())); } catch (Exception ignored) {}
            }
        });
        return ResponseEntity.ok(Map.of("success", true, "message", "执行成功"));
    }

    public static class SimpleJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Job executed: " + context.getJobDetail().getKey());
        }
    }
}
