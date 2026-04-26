package com.gameservice.infrastructure.persistence;

import com.gameservice.domain.MissionCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionCompletionRepository extends JpaRepository<MissionCompletion, Long> {
    boolean existsByCustomerIdAndMissionId(String customerId, Long missionId);
}
