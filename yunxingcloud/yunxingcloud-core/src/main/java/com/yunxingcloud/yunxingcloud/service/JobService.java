package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.SysJob;
import com.yunxingcloud.yunxingcloud.entity.SysJobLog;
import com.yunxingcloud.yunxingcloud.repository.SysJobLogRepository;
import com.yunxingcloud.yunxingcloud.repository.SysJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    private final SysJobRepository jobRepository;
    private final SysJobLogRepository jobLogRepository;

    public JobService(SysJobRepository jobRepository,
                      SysJobLogRepository jobLogRepository) {
        this.jobRepository = jobRepository;
        this.jobLogRepository = jobLogRepository;
    }

    public List<SysJob> findAll() {
        return jobRepository.findAll();
    }

    @Transactional
    public SysJob create(SysJob job) {
        return jobRepository.save(job);
    }

    @Transactional
    public SysJob update(Long id, SysJob body) {
        return jobRepository.findById(id).map(existing -> {
            existing.setJobName(body.getJobName());
            existing.setJobGroup(body.getJobGroup());
            existing.setInvokeTarget(body.getInvokeTarget());
            existing.setCronExpression(body.getCronExpression());
            existing.setMisfirePolicy(body.getMisfirePolicy());
            existing.setConcurrent(body.getConcurrent());
            existing.setStatus(body.getStatus());
            existing.setRemark(body.getRemark());
            return jobRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Job not found"));
    }

    @Transactional
    public void delete(Long id) {
        jobRepository.deleteById(id);
    }

    @Transactional
    public void run(Long id) {
        SysJob job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        SysJobLog log = new SysJobLog();
        log.setJobName(job.getJobName());
        log.setJobGroup(job.getJobGroup());
        log.setInvokeTarget(job.getInvokeTarget());
        log.setResult("手动执行");
        log.setStatus("0");
        log.setStartTime(LocalDateTime.now());
        log.setEndTime(LocalDateTime.now());
        jobLogRepository.save(log);
    }

    @Transactional
    public void pause(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus("1");
            jobRepository.save(job);
        });
    }

    @Transactional
    public void resume(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus("0");
            jobRepository.save(job);
        });
    }

    public List<SysJobLog> getLogs(Long jobId) {
        SysJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        return jobLogRepository.findByJobNameOrderByCreatedAtDesc(job.getJobName());
    }
}
