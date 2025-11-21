package com.aiplms.auth.repository;

import com.aiplms.auth.entity.EmailVerificationToken;
import com.aiplms.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
    Optional<EmailVerificationToken> findTopByUserOrderByCreatedAtDesc(User user);
    void deleteByUser(User user);
}
