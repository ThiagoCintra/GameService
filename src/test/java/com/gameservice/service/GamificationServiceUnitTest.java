package com.gameservice.service;

import com.gameservice.domain.Mission;
import com.gameservice.domain.MissionCompletion;
import com.gameservice.dto.TransactionEvent;
import com.gameservice.infrastructure.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GamificationServiceUnitTest {

    private CustomerProgressRepository customerProgressRepository;
    private MissionRepository missionRepository;
    private MissionCompletionRepository missionCompletionRepository;
    private BenefitRedemptionRepository benefitRedemptionRepository;
    private com.gameservice.infrastructure.persistence.LevelRuleRepository levelRuleRepository;
    private com.gameservice.infrastructure.persistence.ProcessedEventRepository processedEventRepository;
    private GamificationService service;

    @BeforeEach
    void setup() {
        customerProgressRepository = mock(CustomerProgressRepository.class);
        missionRepository = mock(MissionRepository.class);
        missionCompletionRepository = mock(MissionCompletionRepository.class);
        benefitRedemptionRepository = mock(BenefitRedemptionRepository.class);
        levelRuleRepository = mock(com.gameservice.infrastructure.persistence.LevelRuleRepository.class);
        processedEventRepository = mock(com.gameservice.infrastructure.persistence.ProcessedEventRepository.class);

        when(processedEventRepository.existsByEventId(anyString())).thenReturn(false);

        service = new GamificationService(customerProgressRepository, missionRepository, missionCompletionRepository, benefitRedemptionRepository, levelRuleRepository, processedEventRepository);
    }

    @Test
    void shouldAwardPointsForEligibleMissionOnce() {
        Mission m = new Mission();
        m.setId(1L);
        m.setMinValue(new BigDecimal("0.01"));
        m.setMaxValue(new BigDecimal("1000"));
        m.setPoints(5L);
        when(missionRepository.findAllByActiveTrue()).thenReturn(List.of(m));

        when(customerProgressRepository.findByCustomerIdForUpdate(anyString())).thenReturn(Optional.empty());
        when(missionCompletionRepository.existsByCustomerIdAndMissionId(anyString(), anyLong())).thenReturn(false);

        TransactionEvent ev = new TransactionEvent("evt-1","cust-1","PIX", new BigDecimal("100.00"), Instant.now());
        service.processEvent(ev);

        ArgumentCaptor<com.gameservice.domain.CustomerProgress> cap = ArgumentCaptor.forClass(com.gameservice.domain.CustomerProgress.class);
        verify(customerProgressRepository, times(1)).save(cap.capture());
        assertEquals(5L, cap.getValue().getTotalPoints());
    }

    @Test
    void shouldNotDuplicateMissionPointsIfAlreadyCompleted() {
        Mission m = new Mission();
        m.setId(1L);
        m.setMinValue(new BigDecimal("0.01"));
        m.setMaxValue(new BigDecimal("1000"));
        m.setPoints(5L);
        when(missionRepository.findAllByActiveTrue()).thenReturn(List.of(m));

        when(customerProgressRepository.findByCustomerIdForUpdate(anyString())).thenReturn(Optional.empty());
        when(missionCompletionRepository.existsByCustomerIdAndMissionId(anyString(), anyLong())).thenReturn(true);

        TransactionEvent ev = new TransactionEvent("evt-2","cust-1","PIX", new BigDecimal("100.00"), Instant.now());
        service.processEvent(ev);

        verify(customerProgressRepository, times(1)).save(any());
        // saved but points should remain 0
        ArgumentCaptor<com.gameservice.domain.CustomerProgress> cap = ArgumentCaptor.forClass(com.gameservice.domain.CustomerProgress.class);
        verify(customerProgressRepository).save(cap.capture());
        assertEquals(0L, cap.getValue().getTotalPoints());
    }
}
