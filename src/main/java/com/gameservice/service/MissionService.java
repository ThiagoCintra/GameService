package com.gameservice.service;

import com.gameservice.domain.Mission;
import com.gameservice.domain.MissionCompletion;
import com.gameservice.dto.TransactionEvent;
import com.gameservice.infrastructure.persistence.MissionCompletionRepository;
import com.gameservice.infrastructure.persistence.MissionRepository;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;

public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionCompletionRepository missionCompletionRepository;

    public MissionService(MissionRepository missionRepository, MissionCompletionRepository missionCompletionRepository) {
        this.missionRepository = missionRepository;
        this.missionCompletionRepository = missionCompletionRepository;
    }

    public long evaluateAndApply(TransactionEvent event, com.gameservice.domain.CustomerProgress progress) {
        List<Mission> missions = missionRepository.findAllByActiveTrue();
        long awarded = 0L;
        for (Mission m : missions) {
            if (eligibleForMission(event.amount(), m)) {
                boolean alreadyCompleted = missionCompletionRepository.existsByCustomerIdAndMissionId(event.customerId(), m.getId());
                if (alreadyCompleted) continue;
                long points = m.getPoints() != null ? m.getPoints() : 0L;
                progress.setTotalPoints(progress.getTotalPoints() + points);
                missionCompletionRepository.save(new MissionCompletion(event.customerId(), m.getId(), Instant.now()));
                awarded += points;
            }
        }
        return awarded;
    }

    private boolean eligibleForMission(BigDecimal amount, Mission mission) {
        if (amount == null) return false;
        BigDecimal min = mission.getMinValue() != null ? mission.getMinValue() : BigDecimal.ZERO;
        BigDecimal max = mission.getMaxValue() != null ? mission.getMaxValue() : null;
        boolean ge = amount.compareTo(min) >= 0;
        boolean le = max == null || amount.compareTo(max) <= 0;
        return ge && le;
    }
}
