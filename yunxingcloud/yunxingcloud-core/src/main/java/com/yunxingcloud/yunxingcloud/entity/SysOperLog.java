package com.yunxingcloud.yunxingcloud.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_oper_log")
public class SysOperLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String title;

    @Column(length = 20)
    private String businessType;

    @Column(length = 200)
    private String method;

    @Column(length = 20)
    private String operatorType;

    @Column(length = 50)
    private String operName;

    @Column(length = 50)
    private String operIp;

    @Column(length = 255)
    private String operUrl;

    @Column(length = 2000)
    private String operParam;

    @Column(length = 2000)
    private String jsonResult;

    private Integer status;

    @Column(length = 2000)
    private String errorMsg;

    private Long costTime;

    @Column(updatable = false)
    private LocalDateTime operTime;

    public SysOperLog() {}

    @PrePersist protected void onCreate() { if (operTime == null) operTime = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getOperatorType() { return operatorType; }
    public void setOperatorType(String operatorType) { this.operatorType = operatorType; }
    public String getOperName() { return operName; }
    public void setOperName(String operName) { this.operName = operName; }
    public String getOperIp() { return operIp; }
    public void setOperIp(String operIp) { this.operIp = operIp; }
    public String getOperUrl() { return operUrl; }
    public void setOperUrl(String operUrl) { this.operUrl = operUrl; }
    public String getOperParam() { return operParam; }
    public void setOperParam(String operParam) { this.operParam = operParam; }
    public String getJsonResult() { return jsonResult; }
    public void setJsonResult(String jsonResult) { this.jsonResult = jsonResult; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public Long getCostTime() { return costTime; }
    public void setCostTime(Long costTime) { this.costTime = costTime; }
    public LocalDateTime getOperTime() { return operTime; }
}
