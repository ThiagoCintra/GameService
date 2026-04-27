package com.gameservice.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "mission_completion")
public class MissionCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;

    private Long missionId;

    private Instant completedAt;

    public MissionCompletion() {}

    public MissionCompletion(String customerId, Long missionId, Instant completedAt) {
        this.customerId = customerId;
        this.missionId = missionId;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Long getMissionId() {
        return missionId;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
