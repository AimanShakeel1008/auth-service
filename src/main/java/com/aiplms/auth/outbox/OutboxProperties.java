package com.aiplms.auth.outbox;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "outbox")
public class OutboxProperties {

    /**
     * Poll interval for the relay (default: 5 seconds).
     */
    private Duration pollInterval = Duration.ofSeconds(5);

    /**
     * Maximum number of messages to process per poll (default: 100).
     */
    private int batchSize = 100;

    /**
     * Maximum retry attempts before giving up (or moving to a dead-letter flow).
     * Default 5 — application can extend behavior later.
     */
    private int maxRetries = 5;
}
