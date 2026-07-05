package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @org.springframework.transaction.annotation.Transactional
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
}
