package com.yunxingcloud.usercenter.service;

import com.yunxingcloud.usercenter.entity.Role;
import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
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

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        if (!user.isApproved()) {
            throw new DisabledException("账号待审核，请联系管理员");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("账号已被禁用");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            if (role.isEnabled() && role.getPermissions() != null && !role.getPermissions().isBlank()) {
                for (String perm : role.getPermissions().split(",")) {
                    authorities.add(new SimpleGrantedAuthority(perm.trim()));
                }
            }
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, true, true, true,
                authorities
        );
    }
}
