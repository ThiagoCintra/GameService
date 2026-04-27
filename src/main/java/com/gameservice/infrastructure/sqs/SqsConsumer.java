package com.gameservice.infrastructure.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameservice.dto.TransactionEvent;
import com.gameservice.infrastructure.persistence.mongo.GameEventDocument;
import com.gameservice.infrastructure.persistence.mongo.GameEventRepository;
import com.gameservice.service.GamificationService;
import com.gameservice.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
@ConditionalOnProperty(prefix = "aws.sqs", name = "queue-url")
public class SqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsConsumer.class);

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor;
    private final GameEventRepository gameEventRepository;
    private final GamificationService gamificationService;
    private final int maxMessages;
    private final int waitTimeSeconds;
    private final String dlqUrl;
    private final int maxReceiveCount;
    private final long pollIntervalMs;

    public SqsConsumer(SqsClient sqsClient,
                       @Value("${aws.sqs.queue-url}") String queueUrl,
                       ObjectMapper objectMapper,
                       ExecutorService virtualThreadExecutor,
                       GameEventRepository gameEventRepository,
                       GamificationService gamificationService,
                       @Value("${app.sqs.max-messages:10}") int maxMessages,
                       @Value("${app.sqs.wait-time-seconds:20}") int waitTimeSeconds,
                       @Value("${aws.sqs.dlq-url:}") String dlqUrl,
                       @Value("${app.sqs.max-receive-count:5}") int maxReceiveCount,
                       @Value("${app.sqs.poll-interval-ms:500}") long pollIntervalMs) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.objectMapper = objectMapper;
        this.executor = virtualThreadExecutor;
        this.gameEventRepository = gameEventRepository;
        this.gamificationService = gamificationService;
        this.maxMessages = maxMessages;
        this.waitTimeSeconds = waitTimeSeconds;
        this.dlqUrl = dlqUrl;
        this.maxReceiveCount = maxReceiveCount;
        this.pollIntervalMs = pollIntervalMs;
    }

    public void startPolling() {
        log.info("Starting SQS polling on {}", queueUrl);
        long backoff = 1000L;
        while (true) {
            try {
                ReceiveMessageRequest req = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(maxMessages)
                        .waitTimeSeconds(waitTimeSeconds)
                        .attributeNames(software.amazon.awssdk.services.sqs.model.QueueAttributeName.ALL)
                        .build();

                List<Message> messages = sqsClient.receiveMessage(req).messages();
                if (messages.isEmpty()) {
                    try { Thread.sleep(pollIntervalMs); } catch (InterruptedException ignored) {}
                    continue;
                }

                for (Message m : messages) {
                    executor.submit(() -> processMessage(m));
                }
                backoff = 1000L; // reset backoff after successful receive
            } catch (Exception e) {
                log.error("Error while polling SQS", e);
                try { Thread.sleep(Math.min(backoff, 30000L)); } catch (InterruptedException ignored) {}
                backoff = Math.min(backoff * 2, 30000L);
            }
        }
    }

    private void processMessage(Message m) {
        String body = m.body();
        try {
            TransactionEvent event = objectMapper.readValue(body, TransactionEvent.class);

            // idempotency: check mongo if event exists
            if (gameEventRepository.findById(event.eventId()).isPresent()) {
                log.info("Event {} already processed, deleting message", event.eventId());
                deleteMessage(m);
                return;
            }

            GameEventDocument doc = new GameEventDocument(event.eventId(), event.customerId(), event.type(), event.amount(), event.timestamp(), Instant.now());
            gameEventRepository.save(doc);

            gamificationService.processEvent(event);

            deleteMessage(m);
            log.info("Processed and deleted message {}", event.eventId());
        } catch (ProcessingException pe) {
            log.warn("Processing failed for message {}: {}", m.messageId(), pe.getMessage());
            handleFailedMessage(m);
        } catch (Exception e) {
            log.error("Unexpected error processing message {}", m.messageId(), e);
            handleFailedMessage(m);
        }
    }

    private void handleFailedMessage(Message m) {
        try {
            String receiveCountStr = m.attributes().getOrDefault("ApproximateReceiveCount", "1");
            int receiveCount = Integer.parseInt(receiveCountStr);

            if (receiveCount > maxReceiveCount && dlqUrl != null && !dlqUrl.isBlank()) {
                log.info("Message {} exceeded maxReceiveCount ({}). Publishing to DLQ {}", m.messageId(), receiveCount, dlqUrl);
                SendMessageRequest send = SendMessageRequest.builder()
                        .queueUrl(dlqUrl)
                        .messageBody(m.body())
                        .build();
                sqsClient.sendMessage(send);
                deleteMessage(m);
                return;
            }

            // increase visibility timeout to avoid immediate reprocessing (exponential backoff)
            int visibility = Math.min(60 * receiveCount, 12 * 60 * 60); // seconds
            ChangeMessageVisibilityRequest change = ChangeMessageVisibilityRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(m.receiptHandle())
                    .visibilityTimeout(visibility)
                    .build();
            sqsClient.changeMessageVisibility(change);
            log.info("Changed visibility timeout for message {} to {}s", m.messageId(), visibility);
        } catch (Exception e) {
            log.error("Failed handling failed message {}", m.messageId(), e);
        }
    }

    private void deleteMessage(Message m) {
        try {
            DeleteMessageRequest del = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(m.receiptHandle())
                    .build();
            sqsClient.deleteMessage(del);
        } catch (Exception e) {
            log.error("Failed to delete message {}", m.messageId(), e);
        }
    }
}
