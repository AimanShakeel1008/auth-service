package com.aiplms.auth.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Publishes OutboxMessage to Kafka topic.
 * Enabled when property outbox.publisher=kafka
 *
 * This implementation is defensive about the Future type returned by KafkaTemplate.send(...)
 * and will attempt to handle ListenableFuture, CompletableFuture or any java.util.concurrent.Future
 * by calling .get() so failures surface to the caller.
 */
@Component
@ConditionalOnProperty(
        name = "outbox.publisher",
        havingValue = "kafka",
        matchIfMissing = false      // <—— ENSURES IT DOES NOT LOAD BY DEFAULT
)
public class KafkaOutboxPublisher implements OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaOutboxPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxConfigProperties props;
    private final ObjectMapper objectMapper;

    public KafkaOutboxPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                OutboxConfigProperties props,
                                ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(OutboxMessage message) throws IOException {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }

        String topic = props.getTopic();
        if (topic == null || topic.isBlank()) {
            throw new IllegalStateException("outbox.kafka.topic must be configured");
        }

        String key = message.getAggregateId() != null
                ? message.getAggregateId().toString()
                : message.getId() != null ? message.getId().toString() : UUID.randomUUID().toString();

        // Always serialize payload to JSON string using ObjectMapper.
        final String payloadStr;
        try {
            payloadStr = objectMapper.writeValueAsString(message.getPayload());
        } catch (Exception e) {
            log.error("Failed to serialize outbox payload for message id={}", message.getId(), e);
            throw new IOException("Failed to serialize outbox payload", e);
        }

        log.debug("Publishing outbox message id={} key={} topic={}", message.getId(), key, topic);

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, payloadStr);

        Object sendFutureObj = kafkaTemplate.send(record);

        // Support different future types by treating the returned object as a Future and calling get()
        if (!(sendFutureObj instanceof Future<?>)) {
            // In practice KafkaTemplate.send(...) returns ListenableFuture or CompletableFuture which both are Future
            log.error("KafkaTemplate.send(...) returned unexpected type: {}", sendFutureObj.getClass().getName());
            throw new IOException("KafkaTemplate.send returned unsupported future type: " + sendFutureObj.getClass().getName());
        }

        @SuppressWarnings("unchecked")
        Future<SendResult<String, String>> future = (Future<SendResult<String, String>>) sendFutureObj;

        try {
            SendResult<String, String> sendResult = future.get(); // blocks until send completes (success or failure)

            if (sendResult != null && sendResult.getRecordMetadata() != null) {
                log.info("Published outbox message id={} to topic={} partition={} offset={}",
                        message.getId(),
                        topic,
                        sendResult.getRecordMetadata().partition(),
                        sendResult.getRecordMetadata().offset());
            } else {
                log.info("Published outbox message id={} to topic={} - sendResult metadata not available",
                        message.getId(), topic);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while publishing outbox message id={}", message.getId(), e);
            throw new IOException("Interrupted while publishing outbox message", e);
        } catch (ExecutionException e) {
            // Unwrap cause for clearer error surfaced to caller/outbox relay
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("Failed to publish outbox message id={} to topic={} - cause={}", message.getId(), topic, cause.getMessage(), cause);
            throw new IOException("Failed to publish outbox message to Kafka", cause);
        }
    }
}
