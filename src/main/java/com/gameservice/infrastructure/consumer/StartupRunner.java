package com.gameservice.infrastructure.consumer;

import com.gameservice.infrastructure.sqs.SqsConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final SqsConsumer sqsConsumer;
    private final boolean enabled;

    public StartupRunner(@org.springframework.beans.factory.annotation.Autowired(required = false) SqsConsumer sqsConsumer,
                         @org.springframework.beans.factory.annotation.Value("${app.worker.enabled:true}") boolean enabled) {
        this.sqsConsumer = sqsConsumer;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("SQS worker disabled by configuration (app.worker.enabled=false)");
            return;
        }
        if (sqsConsumer == null) {
            log.info("SqsConsumer bean not available, skipping worker startup");
            return;
        }
        log.info("Starting GameService worker...");
        Thread t = new Thread(() -> sqsConsumer.startPolling(), "sqs-poller-main");
        t.setDaemon(true);
        t.start();
    }
}
