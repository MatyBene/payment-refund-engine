package com.matiasbenedetti.refund.exception;

import java.util.UUID;

public class RefundNotFoundException extends RuntimeException {

    private final UUID refundId;

    public RefundNotFoundException(UUID refundId) {
        super("Refund request not found: " + refundId);
        this.refundId = refundId;
    }

    public UUID getRefundId() {
        return refundId;
    }
}
