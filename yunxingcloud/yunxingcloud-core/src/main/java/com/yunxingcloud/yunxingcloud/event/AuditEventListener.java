package com.yunxingcloud.yunxingcloud.event;

import com.yunxingcloud.yunxingcloud.entity.SysLoginInfo;
import com.yunxingcloud.yunxingcloud.repository.SysLoginInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);
    private final SysLoginInfoRepository loginInfoRepository;

    public AuditEventListener(SysLoginInfoRepository loginInfoRepository) {
        this.loginInfoRepository = loginInfoRepository;
    }

    @Async
    @EventListener
    public void onAuditEvent(AuditEvent event) {
        log.info("[AUDIT] {} | user={} | ip={} | time={}",
                event.getType(), event.getUsername(), event.getIp(), event.getTime());

        try {
            SysLoginInfo info = new SysLoginInfo();
            info.setUserName(event.getUsername());
            info.setIpaddr(event.getIp());

            if ("LOGIN_SUCCESS".equals(event.getType())) {
                info.setStatus("0");
                info.setMsg("登录成功");
            } else if ("LOGIN_FAILED".equals(event.getType())) {
                info.setStatus("1");
                info.setMsg("登录失败");
            } else if ("LOGOUT".equals(event.getType())) {
                info.setStatus("0");
                info.setMsg("退出登录");
            }

            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    String ua = attrs.getRequest().getHeader("User-Agent");
                    if (ua != null) {
                        info.setBrowser(parseBrowser(ua));
                        info.setOs(parseOs(ua));
                    }
                }
            } catch (Exception ignored) { log.warn("操作异常: {}", ignored.getMessage()); }

            loginInfoRepository.save(info);
        } catch (Exception e) {
            log.warn("Failed to persist login info: {}", e.getMessage());
        }
    }

    private String parseBrowser(String ua) {
        if (ua.contains("Edg")) return "Edge";
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari")) return "Safari";
        return "Unknown";
    }

    private String parseOs(String ua) {
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac")) return "MacOS";
        if (ua.contains("Linux")) return "Linux";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        return "Unknown";
    }
}
