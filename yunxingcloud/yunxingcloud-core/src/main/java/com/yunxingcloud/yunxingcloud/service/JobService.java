package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysJob;
import com.yunxingcloud.yunxingcloud.entity.SysJobLog;
import com.yunxingcloud.yunxingcloud.repository.SysJobLogRepository;
import com.yunxingcloud.yunxingcloud.repository.SysJobRepository;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);
    private final SysJobRepository jobRepository;
    private final SysJobLogRepository jobLogRepository;
    private final Scheduler scheduler;

    @Autowired(required = false)
    public JobService(SysJobRepository jobRepository,
                      SysJobLogRepository jobLogRepository,
                      Scheduler scheduler) {
        this.jobRepository = jobRepository;
        this.jobLogRepository = jobLogRepository;
        this.scheduler = scheduler;
    }

    public List<SysJob> findAll() {
        return jobRepository.findAll();
    }

    public Optional<SysJob> findById(Long id) {
        return jobRepository.findById(id);
    }

    @Transactional
    public SysJob create(SysJob job) {
        SysJob saved = jobRepository.save(job);
        if (scheduler != null && "0".equals(job.getStatus())) {
            scheduleJob(saved);
        }
        return saved;
    }

    @Transactional
    public SysJob update(Long id, SysJob body) {
        return jobRepository.findById(id).map(existing -> {
            String oldName = existing.getJobName();
            String oldGroup = existing.getJobGroup();
            existing.setJobName(body.getJobName());
            existing.setJobGroup(body.getJobGroup());
            existing.setInvokeTarget(body.getInvokeTarget());
            existing.setCronExpression(body.getCronExpression());
            existing.setMisfirePolicy(body.getMisfirePolicy());
            existing.setConcurrent(body.getConcurrent());
            existing.setStatus(body.getStatus());
            existing.setRemark(body.getRemark());
            SysJob saved = jobRepository.save(existing);
            if (scheduler != null) {
                try {
                    scheduler.deleteJob(new JobKey(oldName, oldGroup));
                } catch (Exception e) {
                    log.warn("删除旧Job调度失败: {}", e.getMessage());
                }
                if ("0".equals(saved.getStatus())) {
                    scheduleJob(saved);
                }
            }
            return saved;
        }).orElseThrow(() -> new IllegalArgumentException("Job not found"));
    }

    @Transactional
    public void delete(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            if (scheduler != null) {
                try {
                    scheduler.deleteJob(new JobKey(job.getJobName(), job.getJobGroup()));
                } catch (Exception e) {
                    log.warn("删除Job调度失败: {}", e.getMessage());
                }
            }
        });
        jobRepository.deleteById(id);
    }

    @Transactional
    public void run(Long id) {
        SysJob job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobName(job.getJobName());
        jobLog.setJobGroup(job.getJobGroup());
        jobLog.setInvokeTarget(job.getInvokeTarget());
        jobLog.setStartTime(LocalDateTime.now());
        try {
            if (scheduler != null) {
                scheduler.triggerJob(new JobKey(job.getJobName(), job.getJobGroup()));
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
    }

    @Transactional
    public void pause(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus("1");
            jobRepository.save(job);
            if (scheduler != null) {
                try {
                    scheduler.pauseJob(new JobKey(job.getJobName(), job.getJobGroup()));
                } catch (Exception e) {
                    log.warn("暂停Job调度失败: {}", e.getMessage());
                }
            }
        });
    }

    @Transactional
    public void resume(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus("0");
            jobRepository.save(job);
            if (scheduler != null) {
                try {
                    scheduler.resumeJob(new JobKey(job.getJobName(), job.getJobGroup()));
                } catch (Exception e) {
                    log.warn("恢复Job调度失败: {}", e.getMessage());
                }
            }
        });
    }

    public List<SysJobLog> getLogs(Long jobId) {
        SysJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        return jobLogRepository.findByJobNameOrderByCreatedAtDesc(job.getJobName());
    }

    public List<SysJobLog> allLogs() {
        return jobLogRepository.findTop50ByOrderByCreatedAtDesc();
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
