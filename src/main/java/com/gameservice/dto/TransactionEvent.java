package com.gameservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEvent(
        String eventId,
        String customerId,
        String type,
        BigDecimal amount,
        Instant timestamp
) {
}
