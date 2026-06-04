package com.yunxingcloud.usercenter.controller;

import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegisterController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public RegisterController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request,
                                                         HttpServletRequest httpRequest,
                                                         HttpServletResponse httpResponse) {
        if (request.username() == null || request.username().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户名不能为空"));
        }
        if (request.password() == null || request.password().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "密码至少6位"));
        }

        try {
            User user = userService.register(request.username(), request.password(), request.email());

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(request.username(), request.password());
            Authentication auth = authenticationManager.authenticate(token);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", user.getUsername(),
                    "message", "注册成功"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    record RegisterRequest(String username, String password, String email) {}
}
