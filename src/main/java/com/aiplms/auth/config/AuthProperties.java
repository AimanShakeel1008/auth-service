package com.aiplms.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for authentication.
 *
 * Bound from properties prefixed with 'auth'.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /**
     * TTL for refresh tokens. Supports Spring Duration formats like "30d", "48h", "PT720H".
     * Default: 30 days.
     */
    private Duration refreshTokenTtl = Duration.ofDays(30);

    /**
     * Maximum allowed consecutive failed login attempts before account gets locked.
     * Default: 5.
     */
    private int maxFailedAttempts = 5;

    /**
     * Duration for which the account will remain locked once threshold is reached.
     * Default: 15 minutes.
     */
    private Duration lockoutDuration = Duration.ofMinutes(15);

}
