package com.aiplms.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens", indexes = {
        @Index(columnList = "token_hash"),
        @Index(columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken {

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    /**
     * SHA256 hash of the token (do not store plain token)
     */
    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    /**
     * actual token expiry moment (UTC)
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * whether token was already used (single-use)
     */
    @Column(name = "used", nullable = false)
    private boolean used = false;

    /**
     * user for whom this token was created
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
