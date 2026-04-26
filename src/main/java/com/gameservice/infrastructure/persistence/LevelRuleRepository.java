package com.gameservice.infrastructure.persistence;

import com.gameservice.domain.LevelRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelRuleRepository extends JpaRepository<LevelRule, Long> {
    List<LevelRule> findAllByOrderByMinPointsAsc();
}
