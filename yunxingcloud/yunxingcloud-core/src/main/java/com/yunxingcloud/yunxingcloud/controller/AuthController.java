package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.yunxingcloud.config.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
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

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request,
                                                      HttpServletRequest httpRequest) {
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

        try {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), password);
            Authentication auth = authenticationManager.authenticate(token);

            String accessToken = jwtTokenService.createAccessToken(auth.getName());
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
        private String username;
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
