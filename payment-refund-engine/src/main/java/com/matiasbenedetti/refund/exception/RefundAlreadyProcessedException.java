package com.matiasbenedetti.refund.exception;

import java.util.UUID;

public class RefundAlreadyProcessedException extends RuntimeException {
    private final UUID transactionId;

    public RefundAlreadyProcessedException(UUID transactionId) {
        super("Transaction has already been refunded: " + transactionId);
        this.transactionId = transactionId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }
}
