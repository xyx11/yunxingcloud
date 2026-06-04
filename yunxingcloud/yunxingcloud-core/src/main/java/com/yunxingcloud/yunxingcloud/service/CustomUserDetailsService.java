package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public CustomUserDetailsService(UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        try {
            List<String> perms = jdbcTemplate.queryForList(
                "SELECT DISTINCT r.permissions FROM role r " +
                "INNER JOIN user_roles ur ON r.id = ur.role_id " +
                "INNER JOIN users u ON u.id = ur.user_id " +
                "WHERE u.username = ? AND r.enabled = true", String.class, username);
            for (String permStr : perms) {
                if (permStr != null && !permStr.isBlank()) {
                    for (String p : permStr.split(",")) {
                        authorities.add(new SimpleGrantedAuthority(p.trim()));
                    }
                }
            }
            List<String> roleCodes = jdbcTemplate.queryForList(
                "SELECT r.code FROM role r " +
                "INNER JOIN user_roles ur ON r.id = ur.role_id " +
                "INNER JOIN users u ON u.id = ur.user_id " +
                "WHERE u.username = ? AND r.enabled = true", String.class, username);
            for (String code : roleCodes) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + code));
            }
        } catch (Exception ignored) {
            // role/user_roles tables may not exist (e.g., H2 test)
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                authorities
        );
    }
}
