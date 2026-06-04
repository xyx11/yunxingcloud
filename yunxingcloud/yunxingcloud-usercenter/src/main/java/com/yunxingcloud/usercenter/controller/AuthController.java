package com.yunxingcloud.usercenter.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final String SAVED_REQUEST_ATTR = "SPRING_SECURITY_SAVED_REQUEST";

    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request,
                                                      HttpServletRequest httpRequest,
                                                      HttpServletResponse httpResponse) {
        try {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(request.username(), request.password());
            Authentication auth = authenticationManager.authenticate(token);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            String redirectUrl = getSavedRequestUrl(httpRequest);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", auth.getName(),
                    "redirectUrl", redirectUrl != null ? redirectUrl : "/"
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

    private String getSavedRequestUrl(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            SavedRequest savedRequest = (SavedRequest) session.getAttribute(SAVED_REQUEST_ATTR);
            if (savedRequest != null) {
                return savedRequest.getRedirectUrl();
            }
        }
        return null;
    }

    record LoginRequest(String username, String password) {}
}
