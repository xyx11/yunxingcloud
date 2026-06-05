package com.yunxingcloud.yunxingcloud.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpResp) {
            httpResp.setHeader("X-Content-Type-Options", "nosniff");
            httpResp.setHeader("X-Frame-Options", "SAMEORIGIN");
            httpResp.setHeader("X-XSS-Protection", "1; mode=block");
            httpResp.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            httpResp.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        }
        chain.doFilter(request, response);
    }
}
