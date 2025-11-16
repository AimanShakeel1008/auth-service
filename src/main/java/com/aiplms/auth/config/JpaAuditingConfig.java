package com.aiplms.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Instant;
import java.util.Optional;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

    /**
     * Provides the current timestamp used by @CreatedDate and @LastModifiedDate.
     * Returns Instant in UTC to keep timestamps consistent across environments.
     */
    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(Instant.now());
        // If you want ZonedDateTime instead, use:
        // return () -> Optional.of(ZonedDateTime.now(ZoneOffset.UTC));
    }
}


