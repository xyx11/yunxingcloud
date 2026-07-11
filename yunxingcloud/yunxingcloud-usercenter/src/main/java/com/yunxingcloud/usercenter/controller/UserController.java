package com.yunxingcloud.usercenter.controller;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.usercenter.repository.SocialAccountRepository;
import com.yunxingcloud.usercenter.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Tag(name = "用户信息", description = "当前用户信息查询")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final SocialAccountRepository socialAccountRepository;
    private final I18nService i18n;

    public UserController(UserService userService, SocialAccountRepository socialAccountRepository, I18nService i18n) {
        this.userService = userService;
        this.socialAccountRepository = socialAccountRepository;
        this.i18n = i18n;
    }

    @Operation(summary = "查询当前用户")
    @GetMapping
    public ResponseEntity<Map<String, Object>> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        return userService.findByUsername(username)
                .map(user -> ResponseEntity.ok(Map.<String, Object>of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail() != null ? user.getEmail() : "",
                        "nickname", user.getNickname() != null ? user.getNickname() : "",
                        "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                        "registerSource", user.getRegisterSource()
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/social")
    public ResponseEntity<List<Map<String, Object>>> socialAccounts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userService.findByUsername(auth.getName())
                .map(user -> socialAccountRepository.findByUserId(user.getId())
                        .stream()
                        .<Map<String, Object>>map(sa -> Map.of(
                                "id", sa.getId(),
                                "provider", sa.getProvider(),
                                "nickname", sa.getNickname() != null ? sa.getNickname() : "",
                                "avatarUrl", sa.getAvatarUrl() != null ? sa.getAvatarUrl() : ""
                        ))
                        .toList())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @DeleteMapping("/social/{id}")
    public ResponseEntity<Map<String, Object>> unbindSocial(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userService.findByUsername(auth.getName())
                .flatMap(user -> socialAccountRepository.findById(id)
                        .filter(sa -> sa.getUserId().equals(user.getId()))
                        .map(sa -> {
                            socialAccountRepository.delete(sa);
                            return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", i18n.msg("user.unbind_success")));
                        }))
                .orElse(ResponseEntity.notFound().build());
    }
}
