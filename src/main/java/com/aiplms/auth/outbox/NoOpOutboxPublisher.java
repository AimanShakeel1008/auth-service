package com.aiplms.auth.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * No-op publisher used during development / when broker integration is not yet available.
 * It logs the message and treats it as published successfully.
 */
@Component
public class NoOpOutboxPublisher implements OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpOutboxPublisher.class);

    @Override
    public void publish(OutboxMessage message) throws IOException {
        // In dev/test we just log. When implementing actual broker integration, replace or extend this.
        log.info("NoOpOutboxPublisher - pretend to publish outbox id={} type={} aggregateType={}",
                message.getId(), message.getType(), message.getAggregateType());
        // simulate success (no exception) so relay marks message processed
    }
}
