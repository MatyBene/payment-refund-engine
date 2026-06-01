package com.matiasbenedetti.refund.service;

import com.matiasbenedetti.refund.dto.RefundRequestDTO;
import com.matiasbenedetti.refund.dto.RefundResponseDTO;
import com.matiasbenedetti.refund.exception.InvalidRefundException;
import com.matiasbenedetti.refund.exception.RefundAlreadyProcessedException;
import com.matiasbenedetti.refund.exception.TransactionNotFoundException;
import com.matiasbenedetti.refund.model.*;
import com.matiasbenedetti.refund.repository.AccountRepository;
import com.matiasbenedetti.refund.repository.RefundRequestRepository;
import com.matiasbenedetti.refund.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.Logger;

/**
 * Core service for processing payment refunds.
 * <p>
 * This service guarantees:
 * <ul>
 *   <li><b>ACID compliance</b> — refund creation and balance update happen
 *       in the same database transaction via {@link Transactional}.</li>
 *   <li><b>Idempotency</b> — if the caller provides the same
 *       {@code Idempotency-Key} twice, the second request returns the stored
 *       result without mutating any state.</li>
 *   <li><b>Double-refund prevention</b> — each original transaction can be
 *       refunded at most once.</li>
 * </ul>
 */
@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final RefundRequestRepository refundRequestRepository;

    public RefundService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            RefundRequestRepository refundRequestRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.refundRequestRepository = refundRequestRepository;
    }

    /**
     * Processes a refund request with full transactional guarantees.
     *
     * @param request        the refund details (transaction ID and reason)
     * @param idempotencyKey unique key for idempotent processing
     * @return response DTO with refund details
     * @throws TransactionNotFoundException    if the original transaction does not exist
     * @throws RefundAlreadyProcessedException if the transaction was already refunded
     * @throws InvalidRefundException          if the transaction is not eligible for refund
     */
    @Transactional
    public RefundResponseDTO processRefund(RefundRequestDTO request, UUID idempotencyKey) {
        log.info("Processing refund for transaction {} with idempotency key {}",
                request.getTransactionId(), idempotencyKey);

        // 1. Idempotency check — if this key was already processed, return cached result
        var existingRefund = refundRequestRepository.findByIdempotencyKey(idempotencyKey);
        if (existingRefund.isPresent()) {
            log.info("Idempotency key {} already processed, returning cached result", idempotencyKey);
            return toResponse(existingRefund.get());
        }

        // 2. Validate original transaction
        Transaction originalTransaction = transactionRepository
                .findById(request.getTransactionId())
                .orElseThrow(() -> new TransactionNotFoundException(request.getTransactionId()));

        validateTransaction(originalTransaction);

        // 3. Check for double refund
        if (refundRequestRepository.existsByOriginalTransactionId(originalTransaction.getId())) {
            throw new RefundAlreadyProcessedException(originalTransaction.getId());
        }

        // 4. Create refund request
        RefundRequest refundRequest = new RefundRequest(
                originalTransaction, request.getReason(), idempotencyKey);
        refundRequest.setStatus(RefundStatus.COMPLETED);
        refundRequest = refundRequestRepository.save(refundRequest);

        // 5. Update account balance (same transaction, thanks to @Transactional)
        Account account = originalTransaction.getAccount();
        BigDecimal newBalance = account.getBalance().add(originalTransaction.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        log.info("Refund {} completed. Account {} new balance: {}",
                refundRequest.getId(), account.getId(), newBalance);

        return toResponse(refundRequest);
    }

    /**
     * Retrieves a refund request by its ID.
     *
     * @param refundId the refund request UUID
     * @return response DTO with refund details
     * @throws com.matiasbenedetti.refund.exception.RefundNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public RefundResponseDTO getRefund(UUID refundId) {
        RefundRequest refundRequest = refundRequestRepository
                .findById(refundId)
                .orElseThrow(() -> new com.matiasbenedetti.refund.exception.RefundNotFoundException(refundId));
        return toResponse(refundRequest);
    }

    /**
     * Validates that a transaction can be refunded.
     */
    private void validateTransaction(Transaction transaction) {
        if (transaction.getType() != TransactionType.PURCHASE) {
            throw new InvalidRefundException(
                    "Only purchase transactions can be refunded. Transaction type: "
                            + transaction.getType());
        }
        if (transaction.getStatus() != TransactionStatus.APPROVED) {
            throw new InvalidRefundException(
                    "Cannot refund a transaction with status: " + transaction.getStatus());
        }
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRefundException(
                    "Cannot refund a non-positive amount: " + transaction.getAmount());
        }
    }

    private RefundResponseDTO toResponse(RefundRequest refundRequest) {
        return new RefundResponseDTO(
                refundRequest.getId(),
                refundRequest.getOriginalTransaction().getId(),
                refundRequest.getStatus(),
                refundRequest.getReason(),
                refundRequest.getIdempotencyKey(),
                refundRequest.getCreatedAt(),
                refundRequest.getUpdatedAt()
        );
    }
}
