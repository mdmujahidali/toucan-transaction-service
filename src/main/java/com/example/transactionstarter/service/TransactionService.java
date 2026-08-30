package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.TransactionResponse;
import com.example.transactionstarter.entity.Transaction;
import com.example.transactionstarter.exception.DuplicateTransactionException;
import com.example.transactionstarter.exception.InvalidStatusTransitionException;
import com.example.transactionstarter.exception.TransactionNotFoundException;
import com.example.transactionstarter.model.TransactionStatus;
import com.example.transactionstarter.repository.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TransactionService {

    /**
     * Allowed status transitions.
     *
     * Reasoning:
     * - PENDING is the only starting state, and can move to any of the
     *   three terminal states — that's the whole point of a "pending"
     *   status: something hasn't been decided yet.
     * - COMPLETED, FAILED and CANCELLED are terminal. Once a transaction
     *   has actually completed or failed, changing its status after the
     *   fact would let you rewrite financial history — e.g. flipping a
     *   FAILED payment to COMPLETED without it ever being reprocessed.
     *   If a transaction genuinely needs to be reversed, that should be
     *   a new transaction (e.g. a refund), not a mutation of the old
     *   one's status.
     */
    private static final Map<TransactionStatus, Set<TransactionStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(TransactionStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(TransactionStatus.PENDING,
                EnumSet.of(TransactionStatus.COMPLETED, TransactionStatus.FAILED, TransactionStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TransactionStatus.COMPLETED, EnumSet.noneOf(TransactionStatus.class));
        ALLOWED_TRANSITIONS.put(TransactionStatus.FAILED, EnumSet.noneOf(TransactionStatus.class));
        ALLOWED_TRANSITIONS.put(TransactionStatus.CANCELLED, EnumSet.noneOf(TransactionStatus.class));
    }

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        if (repository.existsById(request.getTransactionId())) {
            throw new DuplicateTransactionException(request.getTransactionId());
        }

        Transaction transaction = new Transaction(
                request.getTransactionId(),
                request.getCustomerId(),
                request.getAmount(),
                request.getCurrency(),
                request.getType(),
                TransactionStatus.PENDING
        );

        try {
            Transaction saved = repository.save(transaction);
            return new TransactionResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            /*
             * The existence check gives the usual duplicate request a useful
             * response. The database constraint remains the authority when
             * two requests with the same ID pass that check concurrently.
             */
            throw new DuplicateTransactionException(request.getTransactionId());
        }
    }

    public TransactionResponse getTransaction(String transactionId) {
        Transaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        return new TransactionResponse(transaction);
    }

    public TransactionResponse updateStatus(String transactionId, TransactionStatus newStatus) {
        Transaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        TransactionStatus currentStatus = transaction.getStatus();
        Set<TransactionStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        if (!allowed.contains(newStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, newStatus);
        }

        transaction.setStatus(newStatus);
        Transaction saved = repository.save(transaction);
        return new TransactionResponse(saved);
    }

    public List<TransactionResponse> getCustomerTransactions(String customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(TransactionResponse::new)
                .toList();
    }
}
