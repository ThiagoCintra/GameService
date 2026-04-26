package com.gameservice.infrastructure;

import com.gameservice.domain.Mission;
import com.gameservice.infrastructure.persistence.MissionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BootstrapDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(BootstrapDataInitializer.class);

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private MissionRepository missionRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.gameservice.infrastructure.persistence.LevelRuleRepository levelRuleRepository;

    // default constructor for frameworks
    public BootstrapDataInitializer() {}

    @PostConstruct
    public void init() {
        if (missionRepository == null) return; // running in contexts without JPA
        List<Mission> missions = missionRepository.findAll();
        if (!missions.isEmpty()) return;

        Mission m1 = new Mission();
        m1.setName("PIX Small");
        m1.setDescription("PIX 0.01–1000 -> 5 points");
        m1.setProduct("PIX");
        m1.setMinValue(new BigDecimal("0.01"));
        m1.setMaxValue(new BigDecimal("1000"));
        m1.setPoints(5L);

        Mission m2 = new Mission();
        m2.setName("PIX Medium");
        m2.setDescription("PIX 1000–9999 -> 10 points");
        m2.setProduct("PIX");
        m2.setMinValue(new BigDecimal("1000"));
        m2.setMaxValue(new BigDecimal("9999"));
        m2.setPoints(10L);

        Mission m3 = new Mission();
        m3.setName("PIX Large");
        m3.setDescription("PIX 10000+ -> 20 points");
        m3.setProduct("PIX");
        m3.setMinValue(new BigDecimal("10000"));
        m3.setMaxValue(null);
        m3.setPoints(20L);

        missionRepository.saveAll(List.of(m1, m2, m3));
        log.info("Initialized default missions");
        if (levelRuleRepository != null) {
            var rules = levelRuleRepository.findAllByOrderByMinPointsAsc();
            if (rules.isEmpty()) {
                levelRuleRepository.saveAll(List.of(
                        new com.gameservice.domain.LevelRule(1, 0L),
                        new com.gameservice.domain.LevelRule(2, 100L),
                        new com.gameservice.domain.LevelRule(3, 500L),
                        new com.gameservice.domain.LevelRule(4, 1000L),
                        new com.gameservice.domain.LevelRule(5, 2000L)
                ));
                log.info("Initialized default level rules");
            }
        }
        // level rules
        try {
            var existing = missionRepository.count(); // noop to avoid errors
        } catch (Exception ignored) {}
    }
}
