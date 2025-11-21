package com.aiplms.auth.service.impl;

import com.aiplms.auth.config.AuthProperties;
import com.aiplms.auth.entity.EmailVerificationToken;
import com.aiplms.auth.entity.User;
import com.aiplms.auth.exception.Exceptions;
import com.aiplms.auth.repository.EmailVerificationTokenRepository;
import com.aiplms.auth.repository.UserRepository;
import com.aiplms.auth.service.EmailService;
import com.aiplms.auth.service.EmailVerificationService;
import com.aiplms.auth.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    // Duration for verification tokens (configurable — 24 hours)
    private final Duration tokenTtl = Duration.ofHours(24);

    // application base URL for link construction (fallback to env variable)
    @Value("${app.base-url:http://localhost:8081}")
    private String appBaseUrl;

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String createAndSendToken(User user) {
        // remove previous tokens for the user (optional)
        tokenRepository.deleteByUser(user);

        // generate random token (URL-safe)
        byte[] random = new byte[32];
        secureRandom.nextBytes(random);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);

        String tokenHash = TokenUtil.sha256Hex(token);
        Instant expiresAt = Instant.now().plus(tokenTtl);

        EmailVerificationToken record = EmailVerificationToken.builder()
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .user(user)
                .used(false)
                .build();

        tokenRepository.save(record);

        // Construct verification link
        String link = String.format("%s/api/v1/auth/verify-email?token=%s", appBaseUrl, token);

        // Build simple email body (could be an HTML template later)
        String subject = "Verify your email";
        String body = String.format("Hi %s,\n\nPlease verify your email by clicking the link below:\n\n%s\n\nThis link expires in %d hours.\n\nIf you did not sign up, ignore this message.",
                user.getUsername(), link, tokenTtl.toHours());

        // send email (MailHog in dev)
        emailService.send(user.getEmail(), subject, body);

        return token;
    }

    @Override
    @Transactional
    public boolean verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String hash = TokenUtil.sha256Hex(token);
        Optional<EmailVerificationToken> opt = tokenRepository.findByTokenHash(hash);
        if (opt.isEmpty()) {
            return false;
        }

        EmailVerificationToken rec = opt.get();

        if (rec.isUsed()) return false;
        if (rec.getExpiresAt().isBefore(Instant.now())) return false;

        // activate user (set enabled true)
        User user = rec.getUser();
        user.setEnabled(true);
        user.setEmailVerified(true);
        userRepository.save(user);

        rec.setUsed(true);
        tokenRepository.save(rec);

        return true;
    }
}
