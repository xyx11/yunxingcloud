package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.config.JwtTokenService;
import com.yunxingcloud.yunxingcloud.config.RateLimitService;
import com.yunxingcloud.yunxingcloud.config.TokenBlacklist;
import com.yunxingcloud.yunxingcloud.config.TokenStore;
import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.event.AuditEvent;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final TokenBlacklist tokenBlacklist;
    private final RateLimitService rateLimitService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TokenStore tokenStore;
    private final I18nService i18n;
    private final CaptchaService captchaService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenService jwtTokenService,
                       TokenBlacklist tokenBlacklist,
                       RateLimitService rateLimitService,
                       UserRepository userRepository,
                       ApplicationEventPublisher eventPublisher,
                       TokenStore tokenStore,
                       I18nService i18n,
                       CaptchaService captchaService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.tokenBlacklist = tokenBlacklist;
        this.rateLimitService = rateLimitService;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.tokenStore = tokenStore;
        this.i18n = i18n;
        this.captchaService = captchaService;
    }

    public boolean isRateLimited(String ip) {
        return !rateLimitService.isAllowed(ip);
    }

    public String validateCaptcha(String code, HttpSession session) {
        if (code == null || code.isEmpty()) return null;
        String sessionCaptcha = captchaService.getCaptcha(session);
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(code)) {
            return i18n.msg("auth.captcha_error");
        }
        return null;
    }

    public String decryptPassword(String encrypted) {
        try {
            return captchaService.decryptRSA(encrypted);
        } catch (Exception e) {
            return encrypted;
        }
    }

    public String checkAccountLocked(String username) {
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().isLocked()) {
            return i18n.msg("auth.account_locked");
        }
        return null;
    }

    public Map<String, Object> login(String username, String password, String ip) {
        var userOpt = userRepository.findByUsername(username);

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authenticationManager.authenticate(token);

        userOpt.ifPresent(u -> {
            u.onLoginSuccess();
            u.setLastLoginTime(java.time.LocalDateTime.now());
            userRepository.save(u);
        });

        String accessToken = jwtTokenService.createAccessToken(auth.getName(),
                auth.getAuthorities().stream().map(Object::toString).toList());
        tokenStore.add(accessToken, auth.getName(), System.currentTimeMillis() + JwtTokenService.ACCESS_EXPIRATION * 1000);
        eventPublisher.publishEvent(new AuditEvent("LOGIN_SUCCESS", auth.getName(), ip));
        String refreshToken = jwtTokenService.createRefreshToken(auth.getName());

        return Map.of(
                "success", true,
                "username", auth.getName(),
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "tokenType", "Bearer",
                "expiresIn", JwtTokenService.ACCESS_EXPIRATION
        );
    }

    public void handleLoginFailed(String username, String ip) {
        userRepository.findByUsername(username).ifPresent(u -> {
            u.onLoginFailed();
            userRepository.save(u);
        });
        eventPublisher.publishEvent(new AuditEvent("LOGIN_FAILED", username, ip));
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        if (refreshToken == null || !jwtTokenService.validateToken(refreshToken)) {
            return Map.of("success", false, "message", i18n.msg("auth.token_invalid"));
        }
        String username = jwtTokenService.getUsernameFromToken(refreshToken);
        String newAccessToken = jwtTokenService.createAccessToken(username);
        String newRefreshToken = jwtTokenService.createRefreshToken(username);

        return Map.of(
                "success", true,
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken,
                "tokenType", "Bearer",
                "expiresIn", JwtTokenService.ACCESS_EXPIRATION
        );
    }

    public void logout(String bearerToken, String ip) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            tokenBlacklist.add(token);
            tokenStore.remove(token);
            String username = jwtTokenService.getUsernameFromToken(token);
            eventPublisher.publishEvent(new AuditEvent("LOGOUT", username, ip));
        }
    }

    public Map<String, Object> getCurrentUser(Authentication auth) {
        return Map.of(
                "username", auth.getName(),
                "authorities", auth.getAuthorities().stream()
                        .map(org.springframework.security.core.GrantedAuthority::getAuthority).toList()
        );
    }
}
