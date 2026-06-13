package com.yunxingcloud.yunxingcloud.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTracingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestTracingFilter.class);
    private static final String REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        String id = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(REQUEST_ID, id);
        long start = System.currentTimeMillis();
        try {
            if (response instanceof HttpServletResponse httpResp) {
                httpResp.setHeader("X-Request-Id", id);
            }
            chain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            if (request instanceof HttpServletRequest httpReq) {
                String path = httpReq.getRequestURI();
                String query = httpReq.getQueryString();
                if (query != null) path += "?" + query;
                if (elapsed > 1000) {
                    log.warn("[{}] {} {} ({}ms) [slow]", id, httpReq.getMethod(), path, elapsed);
                } else if (log.isDebugEnabled()) {
                    log.debug("[{}] {} {} ({}ms)", id, httpReq.getMethod(), path, elapsed);
                }
            }
            MDC.remove(REQUEST_ID);
        }
    }
}
