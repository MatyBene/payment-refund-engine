package com.matiasbenedetti.refund.repository;

import com.matiasbenedetti.refund.model.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, UUID> {

    Optional<RefundRequest> findByIdempotencyKey(UUID idempotencyKey);

    Optional<RefundRequest> findByOriginalTransactionId(UUID transactionId);

    boolean existsByIdempotencyKey(UUID idempotencyKey);

    boolean existsByOriginalTransactionId(UUID transactionId);

}
