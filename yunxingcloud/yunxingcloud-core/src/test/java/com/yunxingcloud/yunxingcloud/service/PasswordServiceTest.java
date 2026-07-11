package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.yunxingcloud.entity.PasswordResetToken;
import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.PasswordResetTokenRepository;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private I18nService i18n;

    @InjectMocks private PasswordService passwordService;

    @Test
    void shouldForgotPasswordReturnSuccess() {
        when(i18n.msg(anyString())).thenAnswer(inv -> inv.getArgument(0));
        User user = new User("testuser", "encoded-password", "test@example.com");
        user.setId(1L);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(emailService.sendPasswordResetEmail(anyString(), anyString())).thenReturn(true);

        Map<String, Object> result = passwordService.forgotPassword("test@example.com");

        assertThat(result).containsEntry("success", true);
        assertThat(result).containsKey("message");
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    void shouldForgotPasswordNotRevealEmail() {
        when(i18n.msg(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Map<String, Object> result = passwordService.forgotPassword("unknown@example.com");

        assertThat(result).containsEntry("success", true);
        assertThat(result).containsKey("message");
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void shouldForgotPasswordBlankEmail() {
        when(i18n.msg(anyString())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = passwordService.forgotPassword("");

        assertThat(result).containsEntry("success", false);
        assertThat(result).containsEntry("message", "password.blank_email");
        verifyNoInteractions(userRepository, tokenRepository, emailService);
    }

    @Test
    void shouldResetPasswordSuccess() {
        when(i18n.msg(anyString())).thenAnswer(inv -> inv.getArgument(0));
        String token = "valid-token-32-chars-long-string";
        Long userId = 1L;
        PasswordResetToken resetToken = new PasswordResetToken(userId, token);
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        User user = new User("testuser", "old-encoded", "test@example.com");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Abcdef12!")).thenReturn("new-encoded-password");

        Map<String, Object> result = passwordService.resetPassword(token, "Abcdef12!");

        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("message", "password.reset_success");
        verify(passwordEncoder).encode("Abcdef12!");
        verify(userRepository).save(user);
        verify(tokenRepository).save(resetToken);
    }

    @Test
    void shouldResetPasswordInvalidToken() {
        when(i18n.msg(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(tokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        Map<String, Object> result = passwordService.resetPassword("bad-token", "somePassword1!");

        assertThat(result).containsEntry("success", false);
        assertThat(result).containsEntry("message", "password.invalid_token");
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void shouldResetPasswordWeakPassword() {
        when(i18n.msg(anyString())).thenAnswer(inv -> inv.getArgument(0));
        String token = "valid-token-for-weak-pw-test";
        PasswordResetToken resetToken = new PasswordResetToken(1L, token);
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        Map<String, Object> result = passwordService.resetPassword(token, "123");

        assertThat(result).containsEntry("success", false);
        assertThat(result).containsEntry("message", "password.weak");
        assertThat(result).containsKey("details");
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void shouldChangePasswordSuccess() {
        when(i18n.msg(anyString())).thenAnswer(inv -> inv.getArgument(0));
        String username = "testuser";
        User user = new User(username, "encoded-old-pw", "test@example.com");
        user.setId(1L);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass1!", "encoded-old-pw")).thenReturn(true);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("encoded-new-pw");

        Map<String, Object> result = passwordService.changePassword(username, "oldPass1!", "NewPass1!");

        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("message", "password.change_success");
        verify(passwordEncoder).encode("NewPass1!");
        verify(userRepository).save(user);
    }

    @Test
    void shouldChangePasswordWrongOldPassword() {
        when(i18n.msg(anyString())).thenAnswer(inv -> inv.getArgument(0));
        String username = "testuser";
        User user = new User(username, "encoded-old-pw", "test@example.com");
        user.setId(1L);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-old", "encoded-old-pw")).thenReturn(false);

        Map<String, Object> result = passwordService.changePassword(username, "wrong-old", "NewPass1!");

        assertThat(result).containsEntry("success", false);
        assertThat(result).containsEntry("message", "password.old_wrong");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldChangePasswordUserNotFound() {
        when(i18n.msg(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Map<String, Object> result = passwordService.changePassword("nonexistent", "oldPass1!", "NewPass1!");

        assertThat(result).containsEntry("success", false);
        assertThat(result).containsEntry("message", "password.not_found");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any());
    }
}
