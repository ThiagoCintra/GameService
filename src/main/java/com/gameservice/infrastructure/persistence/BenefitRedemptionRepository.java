package com.gameservice.infrastructure.persistence;

import com.gameservice.domain.BenefitRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BenefitRedemptionRepository extends JpaRepository<BenefitRedemption, Long> {
    Optional<BenefitRedemption> findTopByCustomerIdOrderByRedeemedAtDesc(String customerId);
}
