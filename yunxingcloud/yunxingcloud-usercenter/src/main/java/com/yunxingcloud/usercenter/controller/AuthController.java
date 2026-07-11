package com.yunxingcloud.usercenter.controller;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.usercenter.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@Tag(name = "用户认证", description = "用户登录认证")
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final I18nService i18n;

    public AuthController(AuthService authService, I18nService i18n) {
        this.authService = authService;
        this.i18n = i18n;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request,
                                                      HttpServletRequest httpRequest,
                                                      HttpServletResponse httpResponse) {
        try {
            return ResponseEntity.ok(authService.login(request.username(), request.password(),
                    httpRequest, httpResponse));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false, "message", i18n.msg("auth.bad_credentials")
            ));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false, "message", e.getMessage()
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false, "message", i18n.msg("auth.login_failed")
            ));
        }
    }

    record LoginRequest(String username, String password) {}
}
