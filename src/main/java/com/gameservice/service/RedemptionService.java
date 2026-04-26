package com.gameservice.service;

import com.gameservice.infrastructure.persistence.BenefitRedemptionRepository;

import java.time.Instant;

public class RedemptionService {

    private final BenefitRedemptionRepository benefitRedemptionRepository;

    public RedemptionService(BenefitRedemptionRepository benefitRedemptionRepository) {
        this.benefitRedemptionRepository = benefitRedemptionRepository;
    }

    public boolean hasRedeemedThisMonth(String customerId, Instant now) {
        var redeemedOpt = benefitRedemptionRepository.findTopByCustomerIdOrderByRedeemedAtDesc(customerId);
        if (redeemedOpt.isPresent()) {
            Instant redeemedAt = redeemedOpt.get().getRedeemedAt();
            return !isDifferentMonth(redeemedAt, now);
        }
        return false;
    }

    private boolean isDifferentMonth(Instant a, Instant b) {
        java.time.LocalDate da = java.time.LocalDate.ofInstant(a, java.time.ZoneId.systemDefault());
        java.time.LocalDate db = java.time.LocalDate.ofInstant(b, java.time.ZoneId.systemDefault());
        return da.getYear() != db.getYear() || da.getMonthValue() != db.getMonthValue();
    }
}
