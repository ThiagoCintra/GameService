package com.gameservice.service;

import com.gameservice.domain.*;
import com.gameservice.dto.TransactionEvent;
import com.gameservice.infrastructure.persistence.BenefitRedemptionRepository;
import com.gameservice.infrastructure.persistence.CustomerProgressRepository;
import com.gameservice.infrastructure.persistence.MissionCompletionRepository;
import com.gameservice.infrastructure.persistence.MissionRepository;
import com.gameservice.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class GamificationService {

    private static final Logger log = LoggerFactory.getLogger(GamificationService.class);

    private final CustomerProgressRepository customerProgressRepository;
    private final MissionRepository missionRepository;
    private final MissionCompletionRepository missionCompletionRepository;
    private final BenefitRedemptionRepository benefitRedemptionRepository;
    private final com.gameservice.infrastructure.persistence.LevelRuleRepository levelRuleRepository;
    private final com.gameservice.infrastructure.persistence.ProcessedEventRepository processedEventRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public GamificationService(CustomerProgressRepository customerProgressRepository,
                               MissionRepository missionRepository,
                               MissionCompletionRepository missionCompletionRepository,
                               BenefitRedemptionRepository benefitRedemptionRepository,
                               com.gameservice.infrastructure.persistence.LevelRuleRepository levelRuleRepository,
                               com.gameservice.infrastructure.persistence.ProcessedEventRepository processedEventRepository,
                               com.gameservice.infrastructure.persistence.mongo.GameEventRepository gameEventRepository) {
        this.customerProgressRepository = customerProgressRepository;
        this.missionRepository = missionRepository;
        this.missionCompletionRepository = missionCompletionRepository;
        this.benefitRedemptionRepository = benefitRedemptionRepository;
        this.levelRuleRepository = levelRuleRepository;
        this.processedEventRepository = processedEventRepository;

        // build application orchestrator
        this.applicationService = new com.gameservice.application.GameApplicationService(
                this.customerProgressRepository,
                this.missionRepository,
                this.missionCompletionRepository,
                this.benefitRedemptionRepository,
                this.levelRuleRepository,
                this.processedEventRepository,
                gameEventRepository
        );
    }

    // Backwards-compatible constructor used by unit tests
    public GamificationService(CustomerProgressRepository customerProgressRepository,
                               MissionRepository missionRepository,
                               MissionCompletionRepository missionCompletionRepository,
                               BenefitRedemptionRepository benefitRedemptionRepository,
                               com.gameservice.infrastructure.persistence.LevelRuleRepository levelRuleRepository,
                               com.gameservice.infrastructure.persistence.ProcessedEventRepository processedEventRepository) {
        this(customerProgressRepository, missionRepository, missionCompletionRepository, benefitRedemptionRepository, levelRuleRepository, processedEventRepository, null);
    }

    private final com.gameservice.application.GameApplicationService applicationService;

    @Transactional
    public void processEvent(TransactionEvent event) {
        applicationService.processTransactionEvent(event);
    }

    private boolean isDifferentMonth(Instant a, Instant b) {
        LocalDate da = LocalDate.ofInstant(a, ZoneId.systemDefault());
        LocalDate db = LocalDate.ofInstant(b, ZoneId.systemDefault());
        return da.getYear() != db.getYear() || da.getMonthValue() != db.getMonthValue();
    }

    private boolean eligibleForMission(BigDecimal amount, Mission mission) {
        if (amount == null) return false;
        BigDecimal min = mission.getMinValue() != null ? mission.getMinValue() : BigDecimal.ZERO;
        BigDecimal max = mission.getMaxValue() != null ? mission.getMaxValue() : null;
        boolean ge = amount.compareTo(min) >= 0;
        boolean le = max == null || amount.compareTo(max) <= 0;
        return ge && le;
    }

    private int calculateLevel(Long totalPoints) {
        if (totalPoints == null) return 1;
        try {
            var rules = levelRuleRepository.findAllByOrderByMinPointsAsc();
            int level = 1;
            for (var r : rules) {
                if (totalPoints >= (r.getMinPoints() != null ? r.getMinPoints() : 0L)) {
                    level = r.getLevel();
                }
            }
            return level;
        } catch (Exception e) {
            // fallback to defaults
            if (totalPoints < 100) return 1;
            if (totalPoints < 500) return 2;
            if (totalPoints < 1000) return 3;
            if (totalPoints < 2000) return 4;
            return 5;
        }
    }
}
