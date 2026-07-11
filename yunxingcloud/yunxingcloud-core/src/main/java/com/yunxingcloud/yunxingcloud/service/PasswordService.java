package com.yunxingcloud.yunxingcloud.service;

import com.yunxingcloud.common.core.I18nService;
import com.yunxingcloud.common.core.PasswordValidator;
import com.yunxingcloud.yunxingcloud.entity.PasswordResetToken;
import com.yunxingcloud.yunxingcloud.entity.User;
import com.yunxingcloud.yunxingcloud.repository.PasswordResetTokenRepository;
import com.yunxingcloud.yunxingcloud.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PasswordService {

    private static final Logger log = LoggerFactory.getLogger(PasswordService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final I18nService i18n;

    public PasswordService(UserRepository userRepository,
                           PasswordResetTokenRepository tokenRepository,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService,
                           I18nService i18n) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.i18n = i18n;
    }

    @Transactional
    public Map<String, Object> forgotPassword(String email) {
        if (email == null || email.isBlank()) {
            return Map.of("success", false, "message", i18n.msg("password.blank_email"));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return Map.of("success", true, "message", i18n.msg("password.email_sent"));
        }

        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        PasswordResetToken resetToken = new PasswordResetToken(user.getId(), token);
        tokenRepository.save(resetToken);

        boolean sent = emailService.sendPasswordResetEmail(email, token);
        if (!sent) {
            log.info("[DEV] 密码重置令牌: token={} (邮件未发送)", token);
            return Map.of(
                    "success", true,
                    "message", i18n.msg("password.email_sent"),
                    "token", token
            );
        }
        return Map.of("success", true, "message", i18n.msg("password.email_sent"));
    }

    @Transactional
    public Map<String, Object> resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            return Map.of("success", false, "message", i18n.msg("password.invalid_token"));
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);
        if (resetToken == null || !resetToken.isValid()) {
            return Map.of("success", false, "message", i18n.msg("password.invalid_token"));
        }

        List<String> pwErrors = PasswordValidator.validate(newPassword);
        if (!pwErrors.isEmpty()) {
            return Map.of(
                "success", false, "message", i18n.msg("password.weak"),
                "details", String.join("; ", pwErrors));
        }

        User user = userRepository.findById(resetToken.getUserId()).orElse(null);
        if (user == null) {
            return Map.of("success", false, "message", i18n.msg("password.not_found"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return Map.of("success", true, "message", i18n.msg("password.reset_success"));
    }

    @Transactional
    public Map<String, Object> changePassword(String username, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isBlank()) {
            return Map.of("success", false, "message", i18n.msg("common.bad_request"));
        }
        if (newPassword == null || newPassword.isBlank()) {
            return Map.of("success", false, "message", i18n.msg("common.bad_request"));
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return Map.of("success", false, "message", i18n.msg("password.not_found"));
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Map.of("success", false, "message", i18n.msg("password.old_wrong"));
        }

        List<String> pwErrors = PasswordValidator.validate(newPassword);
        if (!pwErrors.isEmpty()) {
            return Map.of(
                "success", false, "message", i18n.msg("password.weak"),
                "details", String.join("; ", pwErrors));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return Map.of("success", true, "message", i18n.msg("password.change_success"));
    }
}
