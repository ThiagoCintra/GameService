package com.gameservice.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "benefit_redemption")
public class BenefitRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;

    private String benefitName;

    private Instant redeemedAt;

    public BenefitRedemption() {}

    public BenefitRedemption(String customerId, String benefitName, Instant redeemedAt) {
        this.customerId = customerId;
        this.benefitName = benefitName;
        this.redeemedAt = redeemedAt;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getBenefitName() {
        return benefitName;
    }

    public Instant getRedeemedAt() {
        return redeemedAt;
    }
}
