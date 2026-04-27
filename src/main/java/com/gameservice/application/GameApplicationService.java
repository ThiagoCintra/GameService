package com.gameservice.application;

import com.gameservice.dto.TransactionEvent;
import com.gameservice.infrastructure.persistence.*;
import com.gameservice.infrastructure.persistence.mongo.GameEventRepository;
import com.gameservice.service.LevelService;
import com.gameservice.service.MissionService;
import com.gameservice.service.RedemptionService;

import java.time.Instant;

public class GameApplicationService {

    private final CustomerProgressRepository customerProgressRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final MissionService missionService;
    private final LevelService levelService;
    private final RedemptionService redemptionService;
    private final GameEventRepository gameEventRepository;

    public GameApplicationService(CustomerProgressRepository customerProgressRepository,
                                  MissionRepository missionRepository,
                                  MissionCompletionRepository missionCompletionRepository,
                                  BenefitRedemptionRepository benefitRedemptionRepository,
                                  com.gameservice.infrastructure.persistence.LevelRuleRepository levelRuleRepository,
                                  ProcessedEventRepository processedEventRepository,
                                  GameEventRepository gameEventRepository) {
        this.customerProgressRepository = customerProgressRepository;
        this.processedEventRepository = processedEventRepository;
        this.missionService = new MissionService(missionRepository, missionCompletionRepository);
        this.levelService = new LevelService(levelRuleRepository);
        this.redemptionService = new RedemptionService(benefitRedemptionRepository);
        this.gameEventRepository = gameEventRepository;
    }

    public void processTransactionEvent(TransactionEvent event) {
        try {
            // idempotency
            if (processedEventRepository != null && processedEventRepository.existsByEventId(event.eventId())) {
                return;
            }

            if (!"PIX".equalsIgnoreCase(event.type())) return;

            Instant now = Instant.now();

            var cpOpt = customerProgressRepository.findByCustomerIdForUpdate(event.customerId());
            com.gameservice.domain.CustomerProgress progress = cpOpt.orElseGet(() -> new com.gameservice.domain.CustomerProgress(event.customerId()));

            if (progress.getLastReset() == null || isDifferentMonth(progress.getLastReset(), now)) {
                progress.setTotalPoints(0L);
                progress.setLevel(1);
                progress.setLastReset(now);
            }

            // check redemption
            if (redemptionService.hasRedeemedThisMonth(event.customerId(), now)) {
                customerProgressRepository.save(progress);
                return;
            }

            // store event in mongo
            if (gameEventRepository != null && gameEventRepository.findById(event.eventId()).isEmpty()) {
                gameEventRepository.save(new com.gameservice.infrastructure.persistence.mongo.GameEventDocument(event.eventId(), event.customerId(), event.type(), event.amount(), event.timestamp(), now));
            }

            // missions
            missionService.evaluateAndApply(event, progress);

            // level update
            progress.setLevel(levelService.calculateLevel(progress.getTotalPoints()));
            customerProgressRepository.save(progress);

            // mark processed
            try {
                if (processedEventRepository != null)
                    processedEventRepository.save(new com.gameservice.domain.ProcessedEvent(event.eventId(), event.customerId(), now));
            } catch (Exception e) {
                // best-effort
            }
        } catch (Exception e) {
            throw new com.gameservice.exception.ProcessingException("Failed processing event " + event.eventId(), e);
        }
    }

    private boolean isDifferentMonth(Instant a, Instant b) {
        java.time.LocalDate da = java.time.LocalDate.ofInstant(a, java.time.ZoneId.systemDefault());
        java.time.LocalDate db = java.time.LocalDate.ofInstant(b, java.time.ZoneId.systemDefault());
        return da.getYear() != db.getYear() || da.getMonthValue() != db.getMonthValue();
    }
}
