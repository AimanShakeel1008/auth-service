package com.aiplms.auth.repository;

import com.aiplms.auth.entity.PasswordResetToken;
import com.aiplms.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    Optional<PasswordResetToken> findTopByUserOrderByCreatedAtDesc(User user);
    void deleteByUser(User user);
}
