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
            httpResp.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self'");
            httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            httpResp.setHeader("X-Permitted-Cross-Domain-Policies", "none");
            httpResp.setHeader("Cross-Origin-Resource-Policy", "cross-origin");
        }
        chain.doFilter(request, response);
    }
}
