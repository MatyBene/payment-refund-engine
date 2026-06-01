package com.matiasbenedetti.refund.controller;

import com.matiasbenedetti.refund.dto.RefundRequestDTO;
import com.matiasbenedetti.refund.dto.RefundResponseDTO;
import com.matiasbenedetti.refund.service.RefundService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/refunds", produces = MediaType.APPLICATION_JSON_VALUE)
public class RefundController {

    private static final Logger log = LoggerFactory.getLogger(RefundController.class);

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    /**
     * Processes a refund request.
     * <p>
     * The client MUST provide an {@code Idempotency-Key} header (a UUID) so that
     * retrying the same request does not create duplicate refunds.
     *
     * @param request        the refund details (transaction ID and reason)
     * @param idempotencyKey unique idempotency key from the request header
     * @return 200 OK with refund details, or 409 Conflict if already refunded
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RefundResponseDTO> createRefund(@Valid @RequestBody RefundRequestDTO request, @RequestHeader("Idempotency-key") UUID idempotencyKey) {
        log.info("POST /api/v1/refunds - transaction: {}, idempotencyKey: {}", request.getTransactionId(), idempotencyKey);

        RefundResponseDTO response = refundService.processRefund(request, idempotencyKey);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the details of a previously processed refund.
     *
     * @param refundId the refund request UUID
     * @return 200 OK with refund details
     */
    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponseDTO> getRefund(@PathVariable UUID refundId) {
        log.info("GET /api/v1/refunds/{}", refundId);

        RefundResponseDTO response = refundService.getRefund(refundId);

        return ResponseEntity.ok(response);
    }
}
