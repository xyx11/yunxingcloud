package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.yunxingcloud.common.core.CsvUtils;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;
    private final I18nService i18n;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JdbcTemplate jdbc, I18nService i18n) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbc = jdbc;
        this.i18n = i18n;
    }

    @Transactional
    public User register(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User(username, passwordEncoder.encode(password), email);
        user.setNickname(username);
        user.setRegisterSource("local");
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean isAdmin(String username) {
        try {
            Long c = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM user_roles ur JOIN role r ON r.id=ur.role_id JOIN users u ON u.id=ur.user_id WHERE u.username=? AND r.code='admin'",
                    Long.class, username);
            return c != null && c > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getCurrentUserDeptId(String username) {
        try {
            return jdbc.queryForObject("SELECT department_id FROM users WHERE username=?", Long.class, username);
        } catch (Exception e) {
            return null;
        }
    }

    public Set<Long> getDeptAndChildren(Long deptId) {
        Set<Long> ids = new LinkedHashSet<>();
        if (deptId == null) return ids;
        ids.add(deptId);
        try {
            List<Long> children = jdbc.queryForList("SELECT id FROM department WHERE parent_id=?", Long.class, deptId);
            for (Long child : children) {
                ids.addAll(getDeptAndChildren(child));
            }
        } catch (Exception ignored) {
        }
        return ids;
    }

    public Map<String, Object> listUsers(int page, int pageSize, String keyword, String currentUser) {
        Map<Long, String> deptNames = new HashMap<>();
        try {
            jdbc.queryForList("SELECT id, name FROM department")
                    .forEach(r -> deptNames.put(((Number) r.get("id")).longValue(), (String) r.get("name")));
        } catch (Exception ignored) {
        }

        Map<Long, List<Map<String, Object>>> userRoles = new HashMap<>();
        try {
            jdbc.queryForList("SELECT ur.user_id, r.id, r.name, r.code FROM user_roles ur JOIN role r ON r.id = ur.role_id")
                    .forEach(r -> {
                        Long uid = ((Number) r.get("user_id")).longValue();
                        userRoles.computeIfAbsent(uid, k -> new ArrayList<>())
                                .add(Map.of("id", ((Number) r.get("id")).longValue(), "name", (String) r.get("name"), "code", (String) r.get("code")));
                    });
        } catch (Exception ignored) {
        }

        var pageable = PageRequest.of(page - 1, pageSize);
        var pageResult = (keyword != null && !keyword.isBlank())
                ? userRepository.findByUsernameContainingOrNicknameContainingOrEmailContaining(keyword, keyword, keyword, pageable)
                : userRepository.findAll(pageable);

        boolean admin = isAdmin(currentUser);
        Set<Long> allowedDeptIds = admin ? null : getDeptAndChildren(getCurrentUserDeptId(currentUser));

        var items = pageResult.stream()
                .filter(u -> admin || u.getUsername().equals(currentUser)
                        || (allowedDeptIds != null && !allowedDeptIds.isEmpty()
                        && u.getDepartmentId() != null && allowedDeptIds.contains(u.getDepartmentId())))
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("username", u.getUsername());
                    m.put("nickname", u.getNickname() != null ? u.getNickname() : "");
                    m.put("email", u.getEmail() != null ? u.getEmail() : "");
                    m.put("departmentId", u.getDepartmentId() != null ? u.getDepartmentId() : 0);
                    m.put("postId", u.getPostId() != null ? u.getPostId() : 0);
                    m.put("departmentName", deptNames.getOrDefault(u.getDepartmentId(), "-"));
                    m.put("registerSource", u.getRegisterSource());
                    m.put("enabled", u.isEnabled());
                    m.put("approved", u.isApproved());
                    m.put("lastLoginTime", u.getLastLoginTime() != null ? u.getLastLoginTime().toString() : "");
                    m.put("roles", userRoles.getOrDefault(u.getId(), List.of()));
                    return m;
                }).toList();

        return Map.of("items", items, "total", pageResult.getTotalElements());
    }

    @Transactional
    public void setUserDepartment(Long id, Long deptId) {
        if (deptId == null) return;
        userRepository.findById(id).ifPresent(u -> {
            u.setDepartmentId(deptId);
            userRepository.save(u);
        });
    }

    @Transactional
    public void setUserPost(Long id, Long postId) {
        if (postId == null) return;
        userRepository.findById(id).ifPresent(u -> {
            u.setPostId(postId);
            userRepository.save(u);
        });
    }

    @Transactional
    public Map<String, Object> setUserRoles(Long id, List<Long> roleIds) {
        if (roleIds == null) {
            return Map.of("success", false, "message", i18n.msg("user.role_ids_empty"));
        }
        jdbc.update("DELETE FROM user_roles WHERE user_id = ?", id);
        for (Long roleId : roleIds) {
            jdbc.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", id, roleId);
        }
        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Object> toggleUserStatus(Long id) {
        return userRepository.findById(id).map(u -> {
            u.setEnabled(!u.isEnabled());
            userRepository.save(u);
            return Map.<String, Object>of("success", true, "enabled", u.isEnabled());
        }).orElse(null);
    }

    @Transactional
    public Map<String, Object> createUser(Map<String, String> body) {
        String username = body.get("username"), password = body.get("password");
        if (username == null || username.isBlank()) {
            return Map.of("success", false, "message", i18n.msg("register.username_blank"));
        }
        if (password == null || password.length() < 6) {
            return Map.of("success", false, "message", i18n.msg("validate.password_min"));
        }
        if (userRepository.existsByUsername(username)) {
            return Map.of("success", false, "message", i18n.msg("register.duplicate_username"));
        }
        User u = new User(username, passwordEncoder.encode(password), body.get("email"));
        if (body.containsKey("nickname")) {
            u.setNickname(body.get("nickname"));
        }
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
            return Map.<String, Object>of("success", true);
        }).orElse(null);
    }

    @Transactional
    public Map<String, Object> batchDeleteUsers(List<Long> ids) {
        for (Long id : ids) {
            jdbc.update("DELETE FROM user_roles WHERE user_id = ?", id);
            userRepository.deleteById(id);
        }
        return Map.of("success", true, "message", i18n.msg("user.delete_batch", ids.size()));
    }

    @Transactional
    public Map<String, Object> deleteUser(Long id) {
        if (!userRepository.existsById(id)) return null;
        jdbc.update("DELETE FROM user_roles WHERE user_id = ?", id);
        userRepository.deleteById(id);
        return Map.of("success", true, "message", i18n.msg("user.delete_success"));
    }

    @Transactional
    public Map<String, Object> importUsers(MultipartFile file) {
        int created = 0, skipped = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",");
                if (cols.length < 3) {
                    skipped++;
                    continue;
                }
                String username = cols[0].trim(), password = cols[1].trim(), email = cols.length > 2 ? cols[2].trim() : "";
                if (username.isEmpty() || password.isEmpty() || userRepository.existsByUsername(username)) {
                    skipped++;
                    continue;
                }
                User u = new User(username, passwordEncoder.encode(password), email);
                if (cols.length > 3 && !cols[3].trim().isEmpty()) {
                    u.setNickname(cols[3].trim());
                }
                u.setRegisterSource("import");
                userRepository.save(u);
                created++;
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", i18n.msg("user.import_failed", e.getMessage()));
        }
        return Map.of("success", true, "created", created, "skipped", skipped, "message", i18n.msg("user.import_complete", created, skipped));
    }

    public byte[] exportUsers() {
        List<String[]> rows = userRepository.findAll().stream()
                .map(u -> new String[]{u.getUsername(), u.getNickname() != null ? u.getNickname() : "",
                        u.getEmail() != null ? u.getEmail() : "", u.getRegisterSource(),
                        u.isEnabled() ? "正常" : "停用"})
                .toList();
        return CsvUtils.toCsv(new String[]{"用户名", "昵称", "邮箱", "来源", "状态"}, rows).getBytes(StandardCharsets.UTF_8);
    }

    public List<Map<String, Object>> pendingApprovals() {
        return userRepository.findByApproved(false).stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "email", u.getEmail() != null ? u.getEmail() : "",
                        "registerSource", u.getRegisterSource()
                ))
                .toList();
    }

    @Transactional
    public Map<String, Object> approveUser(Long id) {
        return userRepository.findById(id).map(u -> {
            u.setApproved(true);
            userRepository.save(u);
            return Map.<String, Object>of("success", true, "message", i18n.msg("user.approve_success"));
        }).orElse(null);
    }

    @Transactional
    public Map<String, Object> rejectUser(Long id) {
        return userRepository.findById(id).map(u -> {
            jdbc.update("DELETE FROM user_roles WHERE user_id = ?", id);
            userRepository.delete(u);
            return Map.<String, Object>of("success", true, "message", i18n.msg("user.reject_success"));
        }).orElse(null);
    }

    @Transactional
    public Map<String, Object> resetUserPassword(Long id) {
        return userRepository.findById(id).map(u -> {
            u.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(u);
            return Map.<String, Object>of("success", true, "message", i18n.msg("user.pwd_reset"));
        }).orElse(null);
    }
}
