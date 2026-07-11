package com.yunxingcloud.usercenter.service;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final I18nService i18n;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, I18nService i18n) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.i18n = i18n;
    }

    public User register(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("register.duplicate_username");
        }
        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("register.duplicate_email");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setNickname(username);
        user.setRegisterSource("local");
        user.setEnabled(true);
        user.setApproved(false);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<Map<String, Object>> listUsers() {
        return userRepository.findAll().stream()
                .<Map<String, Object>>map(u -> {
                    List<Map<String, Object>> roles = u.getRoles().stream()
                            .map(r -> Map.<String, Object>of("id", r.getId(), "name", r.getName(), "code", r.getCode()))
                            .toList();
                    return Map.<String, Object>of(
                            "id", u.getId(),
                            "username", u.getUsername(),
                            "nickname", u.getNickname() != null ? u.getNickname() : "",
                            "email", u.getEmail() != null ? u.getEmail() : "",
                            "departmentId", u.getDepartmentId() != null ? u.getDepartmentId() : 0,
                            "registerSource", u.getRegisterSource(),
                            "enabled", u.isEnabled(),
                            "roles", roles
                    );
                })
                .toList();
    }

    @Transactional
    public Map<String, Object> createUser(Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nickname = body.get("nickname");
        String email = body.get("email");
        if (username == null || username.isBlank()) {
            return Map.of("success", false, "message", i18n.msg("register.username_blank"));
        }
        if (password == null || password.length() < 6) {
            return Map.of("success", false, "message", i18n.msg("validate.password_min"));
        }
        if (userRepository.existsByUsername(username)) {
            return Map.of("success", false, "message", i18n.msg("register.duplicate_username"));
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setNickname(nickname);
        u.setEmail(email);
        u.setRegisterSource("manual");
        userRepository.save(u);
        return Map.<String, Object>of("success", true, "message", i18n.msg("user.create_success"));
    }

    @Transactional
    public Map<String, Object> updateUserProfile(Long id, Map<String, String> body) {
        return userRepository.findById(id).map(u -> {
            if (body.containsKey("nickname")) u.setNickname(body.get("nickname"));
            if (body.containsKey("email")) u.setEmail(body.get("email"));
            userRepository.save(u);
            return Map.<String, Object>of("success", true, "message", i18n.msg("user.update_success"));
        }).orElseThrow(() -> new IllegalArgumentException("user.not_found"));
    }

    @Transactional
    public Map<String, Object> resetUserPassword(Long id) {
        return userRepository.findById(id).map(u -> {
            u.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(u);
            return Map.<String, Object>of("success", true, "message", i18n.msg("user.pwd_reset"));
        }).orElseThrow(() -> new IllegalArgumentException("user.not_found"));
    }

    @Transactional
    public Map<String, Object> toggleUserStatus(Long id) {
        return userRepository.findById(id).map(u -> {
            u.setEnabled(!u.isEnabled());
            userRepository.save(u);
            return Map.<String, Object>of("success", true, "enabled", u.isEnabled());
        }).orElseThrow(() -> new IllegalArgumentException("user.not_found"));
    }

    @Transactional
    public Map<String, Object> importUsers(MultipartFile file) {
        if (file.isEmpty()) {
            return Map.of("success", false, "message", i18n.msg("file.empty"));
        }
        int success = 0, fail = 0;
        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) { fail++; continue; }
                try {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String email = parts.length > 2 ? parts[2].trim() : "";
                    if (userRepository.existsByUsername(username)) { fail++; continue; }
                    User u = new User();
                    u.setUsername(username);
                    u.setPassword(passwordEncoder.encode(password));
                    u.setEmail(email);
                    u.setRegisterSource("import");
                    userRepository.save(u);
                    success++;
                } catch (Exception e) { fail++; }
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", i18n.msg("file.parse_failed") + ": " + e.getMessage());
        }
        return Map.of("success", true, "imported", success, "failed", fail);
    }
}
