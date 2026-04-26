//package com.gameservice;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.DirtiesContext;
//
//import com.gameservice.domain.*;
//import com.gameservice.dto.*;
//import com.gameservice.infrastructure.*;
//import com.gameservice.service.*;
//
//@SpringBootTest(classes = com.gameservice.GameServiceApplication.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//public class GamificationConcurrencyTest {
//
//    @Autowired
//    private GamificationService gamificationService;
//
//    @Autowired
//    private MissionRepository missionRepository;
//
//    @Autowired
//    private CustomerProgressRepository customerProgressRepository;
//
//    @BeforeEach
//    void setup() {
//        missionRepository.deleteAll();
//        var m = new Mission();
//        m.setName("PIX Small");
//        m.setMinValue(new BigDecimal("0.01"));
//        m.setMaxValue(new BigDecimal("1000"));
//        m.setPoints(5L);
//        missionRepository.save(m);
//    }
//
//    @Test
//    void concurrentProcessingShouldResultInSingleMissionCompletion() throws InterruptedException {
//        int threads = 50;
//        ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor();
//        List<Runnable> tasks = new ArrayList<>();
//        for (int i = 0; i < threads; i++) {
//            int idx = i;
//            tasks.add(() -> {
//                var ev = new TransactionEvent("evt-conc","cust-conc","PIX", new BigDecimal("10.0"), Instant.now());
//                gamificationService.processEvent(ev);
//            });
//        }
//
//        tasks.forEach(ex::submit);
//        ex.shutdown();
//        ex.awaitTermination(30, TimeUnit.SECONDS);
//
//        var cpOpt = customerProgressRepository.findByCustomerId("cust-conc");
//        assertThat(cpOpt).isPresent();
//        assertThat(cpOpt.get().getTotalPoints()).isEqualTo(5L);
//    }
//}
