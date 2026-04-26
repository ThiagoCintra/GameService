package com.gameservice.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "processed_event", indexes = {@Index(name = "idx_event_id", columnList = "eventId")})
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    private String customerId;

    private Instant processedAt;

    public ProcessedEvent() {}

    public ProcessedEvent(String eventId, String customerId, Instant processedAt) {
        this.eventId = eventId;
        this.customerId = customerId;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
