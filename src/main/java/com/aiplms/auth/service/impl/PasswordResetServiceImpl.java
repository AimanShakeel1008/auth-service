package com.aiplms.auth.service.impl;

import com.aiplms.auth.entity.PasswordResetToken;
import com.aiplms.auth.entity.User;
import com.aiplms.auth.exception.Exceptions;
import com.aiplms.auth.repository.PasswordResetTokenRepository;
import com.aiplms.auth.repository.UserRepository;
import com.aiplms.auth.service.EmailService;
import com.aiplms.auth.service.PasswordResetService;
import com.aiplms.auth.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    // Default TTL for reset tokens: 1 hour
    private static final Duration DEFAULT_TOKEN_TTL = Duration.ofHours(1);

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url:http://localhost:8081}")
    private String appBaseUrl;

    // Make TTL configurable later via properties if required.
    private final Duration tokenTtl = DEFAULT_TOKEN_TTL;

    @Override
    @Transactional
    public String createAndSendToken(String email) {
        if (email == null || email.isBlank()) {
            throw Exceptions.badRequest("Email required");
        }

        Optional<User> uOpt = userRepository.findByEmail(email);
        if (uOpt.isEmpty()) {
            // Do not reveal whether email exists — still return as if done.
            log.info("Password reset requested for non-existent email: {}", email);
            return null;
        }

        User user = uOpt.get();

        // Remove previous tokens (optional, similar to email verification implementation)
        tokenRepository.deleteByUser(user);

        // Generate secure URL-safe token (like email verification implementation)
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);

        // Hash the token for storage
        String tokenHash = TokenUtil.sha256Hex(token);
        Instant expiresAt = Instant.now().plus(tokenTtl);

        PasswordResetToken record = PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .user(user)
                .used(false)
                .build();

        tokenRepository.save(record);

        // Build reset link (pointing to app frontend reset page)
        String link = String.format("%s/password-reset?token=%s", appBaseUrl, token);

        String subject = "Password Reset Request";
        String body = String.format("Hi %s,\n\nWe received a request to reset your password. " +
                        "You can reset your password using the link below (valid for %d minutes):\n\n%s\n\n" +
                        "If you did not request this, ignore this message.\n",
                user.getUsername(), tokenTtl.toMinutes(), link);

        // send email (MailHog in dev)
        emailService.send(user.getEmail(), subject, body);

        // return plain token for tests (production: don't log)
        return token;
    }

    @Override
    @Transactional
    public boolean confirmReset(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw Exceptions.badRequest("Token required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw Exceptions.badRequest("New password required");
        }

        String tokenHash = TokenUtil.sha256Hex(token);
        var o = tokenRepository.findByTokenHash(tokenHash);
        if (o.isEmpty()) {
            return false;
        }

        PasswordResetToken rec = o.get();

        if (rec.isUsed()) return false;
        if (rec.getExpiresAt().isBefore(Instant.now())) return false;

        // Update user's password
        User user = rec.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // mark token used
        rec.setUsed(true);
        tokenRepository.save(rec);

        return true;
    }
}
