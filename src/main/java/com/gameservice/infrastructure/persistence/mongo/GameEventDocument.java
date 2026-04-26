package com.gameservice.infrastructure.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "game_events")
public class GameEventDocument {

    @Id
    private String eventId;
    private String customerId;
    private String type;
    private BigDecimal amount;
    private Instant timestamp;
    private Instant processedAt;

    public GameEventDocument() {
    }

    public GameEventDocument(String eventId, String customerId, String type, BigDecimal amount, Instant timestamp, Instant processedAt) {
        this.eventId = eventId;
        this.customerId = customerId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.processedAt = processedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getType() {
        return type;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
