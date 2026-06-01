package com.matiasbenedetti.refund.dto;

import com.matiasbenedetti.refund.model.RefundStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class RefundResponseDTO {

    private UUID refundId;
    private UUID transactionId;
    private RefundStatus status;
    private String reason;
    private UUID idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RefundResponseDTO() {
    }

    public RefundResponseDTO(
            UUID refundId,
            UUID transactionId,
            RefundStatus status,
            String reason,
            UUID idempotencyKey,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.refundId = refundId;
        this.transactionId = transactionId;
        this.status = status;
        this.reason = reason;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getRefundId() {
        return refundId;
    }

    public void setRefundId(UUID refundId) {
        this.refundId = refundId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(UUID idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
