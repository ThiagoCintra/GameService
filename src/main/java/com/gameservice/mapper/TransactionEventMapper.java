package com.gameservice.mapper;

import com.gameservice.dto.TransactionEvent;
import com.gameservice.dto.TransactionEventDTO;

public final class TransactionEventMapper {

    private TransactionEventMapper() {}

    public static TransactionEventDTO toDTO(TransactionEvent e) {
        if (e == null) return null;
        return new TransactionEventDTO(e.eventId(), e.customerId(), e.type(), e.amount(), e.timestamp());
    }
}
