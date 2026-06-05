package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.config.JwtTokenService;
import com.yunxingcloud.yunxingcloud.event.AuditEvent;
import org.springframework.context.ApplicationEventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.yunxingcloud.yunxingcloud.config.RateLimitService;
import com.yunxingcloud.yunxingcloud.config.TokenBlacklist;
import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证管理", description = "登录、登出、Token 刷新、用户信息")
@RestController
@RequestMapping("/api")
public class AuthController {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final TokenBlacklist tokenBlacklist;
    private final RateLimitService rateLimitService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenService jwtTokenService,
                          TokenBlacklist tokenBlacklist,
                          RateLimitService rateLimitService,
                          UserRepository userRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.tokenBlacklist = tokenBlacklist;
        this.rateLimitService = rateLimitService;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Operation(summary = "用户登录", description = "使用用户名密码登录，返回 JWT accessToken 和 refreshToken")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                                      HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        if (!rateLimitService.isAllowed(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "success", false, "message", "请求过于频繁，请稍后再试"
            ));
        }

        if (request.getCode() != null && !request.getCode().isEmpty()) {
            String sessionCaptcha = CaptchaController.getCaptcha(httpRequest.getSession());
            if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(request.getCode())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false, "message", "验证码错误"
                ));
            }
        }

        String password = request.getPassword();
        try {
            password = CaptchaController.decryptRSA(password);
        } catch (Exception ignored) {}

        // check account lockout
        var userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isPresent() && userOpt.get().isLocked()) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of(
                    "success", false, "message", "账号已被锁定，请30分钟后重试"
            ));
        }

        try {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), password);
            Authentication auth = authenticationManager.authenticate(token);

            // reset failed attempts on success
            userOpt.ifPresent(u -> { u.onLoginSuccess(); userRepository.save(u); });

            String accessToken = jwtTokenService.createAccessToken(auth.getName());
            eventPublisher.publishEvent(new AuditEvent("LOGIN_SUCCESS", auth.getName(), ip));
            String refreshToken = jwtTokenService.createRefreshToken(auth.getName());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", auth.getName(),
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "tokenType", "Bearer",
                    "expiresIn", JwtTokenService.ACCESS_EXPIRATION
            ));
        } catch (BadCredentialsException e) {
            userOpt.ifPresent(u -> { u.onLoginFailed(); userRepository.save(u); });
            eventPublisher.publishEvent(new AuditEvent("LOGIN_FAILED", request.getUsername(), ip));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false, "message", "用户名或密码错误"
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false, "message", "认证失败，请重试"
            ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || !jwtTokenService.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false, "message", "无效的刷新令牌"
            ));
        }
        String username = jwtTokenService.getUsernameFromToken(refreshToken);
        String newAccessToken = jwtTokenService.createAccessToken(username);
        String newRefreshToken = jwtTokenService.createRefreshToken(username);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken,
                "tokenType", "Bearer",
                "expiresIn", JwtTokenService.ACCESS_EXPIRATION
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            tokenBlacklist.add(bearer.substring(7));
            String username = jwtTokenService.getUsernameFromToken(bearer.substring(7));
            eventPublisher.publishEvent(new AuditEvent("LOGOUT", username, request.getRemoteAddr()));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "已登出"));
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || ANONYMOUS_USER.equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of(
                "username", auth.getName(),
                "authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).toList()
        ));
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 50, message = "用户名长度2-50位")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 4, max = 100, message = "密码长度4-100位")
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
