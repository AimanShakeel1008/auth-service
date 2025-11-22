package com.aiplms.auth.service.impl;

import com.aiplms.auth.outbox.OutboxMessage;
import com.aiplms.auth.outbox.OutboxRepository;
import com.aiplms.auth.service.OutboxService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void saveEvent(String aggregateType, UUID aggregateId, String type, String payloadJson) {
        JsonNode payloadNode;
        try {
            payloadNode = objectMapper.readTree(payloadJson);
        } catch (Exception ex) {
            // fallback: wrap raw string
            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("raw", payloadJson);
            payloadNode = wrapper;
        }

        OutboxMessage msg = OutboxMessage.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .type(type)
                .payload(payloadNode)
                .occurredAt(OffsetDateTime.now())
                .processed(false)
                .retryCount(0)
                .build();
        outboxRepository.save(msg);
    }
}
