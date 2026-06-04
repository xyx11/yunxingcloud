package com.yunxingcloud.yunxingcloud.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_job")
public class SysJob {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String jobName;

    @Column(nullable = false, length = 64)
    private String jobGroup = "DEFAULT";

    @Column(nullable = false, length = 255)
    private String invokeTarget;

    @Column(length = 20)
    private String cronExpression;

    @Column(length = 1)
    private String misfirePolicy = "3";

    @Column(length = 1)
    private String concurrent = "1";

    @Column(length = 1)
    private String status = "0";

    @Column(length = 500)
    private String remark;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public SysJob() {}

    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public String getJobGroup() { return jobGroup; }
    public void setJobGroup(String jobGroup) { this.jobGroup = jobGroup; }
    public String getInvokeTarget() { return invokeTarget; }
    public void setInvokeTarget(String invokeTarget) { this.invokeTarget = invokeTarget; }
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public String getMisfirePolicy() { return misfirePolicy; }
    public void setMisfirePolicy(String misfirePolicy) { this.misfirePolicy = misfirePolicy; }
    public String getConcurrent() { return concurrent; }
    public void setConcurrent(String concurrent) { this.concurrent = concurrent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
