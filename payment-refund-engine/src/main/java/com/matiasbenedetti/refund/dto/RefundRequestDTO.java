package com.matiasbenedetti.refund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class RefundRequestDTO {

    @NotNull(message = "Transaction ID is required")
    private UUID transactionId;

    @NotBlank(message = "Reason is required")
    private String reason;

    public RefundRequestDTO() {
    }

    public RefundRequestDTO(UUID transactionId, String reason) {
        this.transactionId = transactionId;
        this.reason = reason;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
