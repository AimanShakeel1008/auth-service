package com.aiplms.auth.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for outbox -> kafka integration.
 */
@Component
@ConfigurationProperties(prefix = "outbox.kafka")
public class OutboxConfigProperties {

    /**
     * Kafka topic to publish outbox events to.
     */
    private String topic = "auth.events";

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
