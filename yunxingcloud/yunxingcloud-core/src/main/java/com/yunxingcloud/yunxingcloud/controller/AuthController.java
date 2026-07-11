package com.yunxingcloud.yunxingcloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证管理", description = "登录、登出、Token 刷新、用户信息")
@RestController
@RequestMapping("/api")
public class AuthController {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private final AuthService authService;
    private final I18nService i18n;

    public AuthController(AuthService authService, I18nService i18n) {
        this.authService = authService;
        this.i18n = i18n;
    }

    @Operation(summary = "用户登录", description = "使用用户名密码登录，返回 JWT accessToken 和 refreshToken")
    @PostMapping("/login")
    @SentinelResource(value = "loginFlow", blockHandler = "loginBlockHandler")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                                      HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        if (authService.isRateLimited(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "success", false, "message", i18n.msg("ratelimit.too_many_requests")
            ));
        }

        String captchaError = authService.validateCaptcha(request.getCode(), httpRequest.getSession());
        if (captchaError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false, "message", captchaError
            ));
        }

        String password = authService.decryptPassword(request.getPassword());

        String lockError = authService.checkAccountLocked(request.getUsername());
        if (lockError != null) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of(
                    "success", false, "message", lockError
            ));
        }

        try {
            return ResponseEntity.ok(authService.login(request.getUsername(), password, ip));
        } catch (BadCredentialsException e) {
            authService.handleLoginFailed(request.getUsername(), ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false, "message", i18n.msg("auth.bad_credentials")
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false, "message", i18n.msg("auth.login_failed")
            ));
        }
    }

    public ResponseEntity<Map<String, Object>> loginBlockHandler(LoginRequest request,
                                                                  HttpServletRequest httpRequest,
                                                                  BlockException ex) {
        if (ex instanceof DegradeException) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false, "message", i18n.msg("circuit_breaker.unavailable")
            ));
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "success", false, "message", i18n.msg("ratelimit.too_many_requests")
        ));
    }

    @PostMapping("/refresh")
    @SentinelResource(value = "refreshFlow", blockHandler = "refreshBlockHandler")
    public ResponseEntity<Map<String, Object>> refresh(@RequestBody Map<String, String> body) {
        Map<String, Object> result = authService.refreshToken(body.get("refreshToken"));
        if (Boolean.FALSE.equals(result.get("success"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<Map<String, Object>> refreshBlockHandler(Map<String, String> body,
                                                                    BlockException ex) {
        if (ex instanceof DegradeException) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false, "message", i18n.msg("circuit_breaker.unavailable")
            ));
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "success", false, "message", i18n.msg("ratelimit.too_many_requests")
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        authService.logout(request.getHeader("Authorization"), request.getRemoteAddr());
        return ResponseEntity.ok(Map.of("success", true, "message", i18n.msg("auth.logout_success")));
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || ANONYMOUS_USER.equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.getCurrentUser(auth));
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    static class LoginRequest {
        @NotBlank(message = "{validate.not_blank}")
        @Size(min = 2, max = 50, message = "{validate.username_size}")
        private String username;

        @NotBlank(message = "{validate.not_blank}")
        private String password;

        private String code;
        private String uuid;
        private Boolean rememberMe = false;

        public LoginRequest() {}
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public Boolean getRememberMe() { return rememberMe; }
        public void setRememberMe(Boolean rememberMe) { this.rememberMe = rememberMe; }
    }
}
