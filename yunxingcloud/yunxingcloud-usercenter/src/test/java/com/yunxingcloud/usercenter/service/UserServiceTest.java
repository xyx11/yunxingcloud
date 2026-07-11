package com.yunxingcloud.usercenter.service;

import com.yunxingcloud.usercenter.entity.User;
import com.yunxingcloud.usercenter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private com.yunxingcloud.common.core.I18nService i18n;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, i18n);
    }

    @Test void shouldFindByUsername() {
        User user = new User();
        user.setUsername("test");
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        var result = userService.findByUsername("test");
        assertTrue(result.isPresent());
        assertEquals("test", result.get().getUsername());
    }

    @Test void shouldRegisterUser() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("Test1234!")).thenReturn("encoded");
        User saved = new User();
        saved.setUsername("newuser");
        saved.setEmail("test@test.com");
        when(userRepository.save(any())).thenReturn(saved);

        User result = userService.register("newuser", "Test1234!", "test@test.com");
        assertEquals("newuser", result.getUsername());
        verify(passwordEncoder).encode("Test1234!");
    }

    @Test void shouldThrowOnDuplicateUsername() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () ->
                userService.register("existing", "Test1234!", "test@test.com"));
    }

    @Test void shouldFindById() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertTrue(userService.findById(1L).isPresent());
    }
}
