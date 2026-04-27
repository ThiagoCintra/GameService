package com.gameservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEventDTO(
        String eventId,
        String customerId,
        String type,
        BigDecimal amount,
        Instant timestamp
) {
}
