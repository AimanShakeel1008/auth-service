package com.aiplms.auth.outbox;

import java.io.IOException;

/**
 * Abstraction for publishing outbox messages to a broker.
 *
 * Implementations should be idempotent or handle duplicates as needed
 * at the broker/consumer side.
 */
public interface OutboxPublisher {

    /**
     * Publish the given outbox message.
     *
     * @param message message to publish
     * @throws IOException on transient publish errors
     * @throws RuntimeException on unrecoverable errors
     */
    void publish(OutboxMessage message) throws IOException;
}
