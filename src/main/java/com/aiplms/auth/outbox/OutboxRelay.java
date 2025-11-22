package com.aiplms.auth.outbox;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Polls the outbox table and attempts to publish events.
 *
 * This is intentionally simple — it delegates publishing to an OutboxPublisher,
 * and updates the outbox row state accordingly.
 */
@Service
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxRepository repository;
    private final OutboxPublisher publisher;
    private final OutboxProperties properties;

    public OutboxRelay(OutboxRepository repository,
                       OutboxPublisher publisher,
                       OutboxProperties properties) {
        this.repository = repository;
        this.publisher = publisher;
        this.properties = properties;
    }

    /**
     * Scheduled invocation. We use fixedDelayString configured via properties.
     * The actual scheduling configuration will be bound by a property 'outbox.poll-interval'.
     *
     * For now this method is simple and processes one batch at a time.
     */
    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:5000}")
    public void pollAndRelay() {
        try {
            processBatch();
        } catch (Exception ex) {
            log.error("Unexpected error in OutboxRelay.pollAndRelay", ex);
        }
    }

    @Transactional
    protected void processBatch() {
        List<OutboxMessage> pending = repository.findTop100ByProcessedFalseOrderByOccurredAtAsc();
        if (pending.isEmpty()) {
            if (log.isTraceEnabled()) {
                log.trace("OutboxRelay: no pending messages");
            }
            return;
        }

        log.info("OutboxRelay: processing {} outbox messages (batchSize={})", pending.size(), properties.getBatchSize());
        for (OutboxMessage msg : pending) {
            if (msg.getRetryCount() >= properties.getMaxRetries()) {
                log.warn("OutboxRelay: message id={} reached max retries ({}). Skipping for now.",
                        msg.getId(), msg.getRetryCount());
                continue; // later we may move to DLQ/poison queue
            }

            try {
                log.debug("OutboxRelay: publishing id={} type={}", msg.getId(), msg.getType());
                publisher.publish(msg);

                msg.setProcessed(true);
                msg.setProcessedAt(OffsetDateTime.now());
                repository.save(msg);
                log.info("OutboxRelay: published and marked processed id={}", msg.getId());
            } catch (IOException ex) {
                // treat as transient error: increment retry count and persist
                msg.setRetryCount(msg.getRetryCount() + 1);
                repository.save(msg);
                log.warn("OutboxRelay: transient error publishing id={}, retryCount={}, error={}",
                        msg.getId(), msg.getRetryCount(), ex.getMessage());
            } catch (Exception ex) {
                // unexpected/unrecoverable — increment retry and log; keep unprocessed for future attempts
                msg.setRetryCount(msg.getRetryCount() + 1);
                repository.save(msg);
                log.error("OutboxRelay: error publishing id={}, retryCount={}, error={}", msg.getId(), msg.getRetryCount(), ex.getMessage(), ex);
            }
        }
    }
}
