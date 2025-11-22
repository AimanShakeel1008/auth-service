package com.aiplms.auth.service;

import java.util.UUID;

public interface OutboxService {
    void saveEvent(String aggregateType, UUID aggregateId, String type, String payload);
}
