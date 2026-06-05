package com.yunxingcloud.yunxingcloud.event;

import java.time.LocalDateTime;

public class AuditEvent {

    private final String type;       // LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT
    private final String username;
    private final String ip;
    private final LocalDateTime time;

    public AuditEvent(String type, String username, String ip) {
        this.type = type;
        this.username = username;
        this.ip = ip;
        this.time = LocalDateTime.now();
    }

    public String getType() { return type; }
    public String getUsername() { return username; }
    public String getIp() { return ip; }
    public LocalDateTime getTime() { return time; }
}
