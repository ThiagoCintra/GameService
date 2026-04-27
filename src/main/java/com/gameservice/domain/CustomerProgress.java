package com.gameservice.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "customer_progress", indexes = {@Index(name = "idx_customer_id", columnList = "customerId")})
public class CustomerProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String customerId;

    private Long totalPoints = 0L;

    private Integer level = 1;

    private Instant lastReset;

    @Version
    private Long version;

    public CustomerProgress() {
    }

    public CustomerProgress(String customerId) {
        this.customerId = customerId;
        this.lastReset = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Instant getLastReset() {
        return lastReset;
    }

    public void setLastReset(Instant lastReset) {
        this.lastReset = lastReset;
    }
}
