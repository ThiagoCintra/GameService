package com.gameservice.service;

import com.gameservice.infrastructure.persistence.LevelRuleRepository;

public class LevelService {

    private final com.gameservice.infrastructure.persistence.LevelRuleRepository levelRuleRepository;

    public LevelService(com.gameservice.infrastructure.persistence.LevelRuleRepository levelRuleRepository) {
        this.levelRuleRepository = levelRuleRepository;
    }

    public int calculateLevel(Long totalPoints) {
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
            if (totalPoints < 100) return 1;
            if (totalPoints < 500) return 2;
            if (totalPoints < 1000) return 3;
            if (totalPoints < 2000) return 4;
            return 5;
        }
    }
}
