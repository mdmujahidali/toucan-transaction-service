package com.example.transactionstarter.dto;

import com.example.transactionstarter.entity.Transaction;
import com.example.transactionstarter.model.Currency;
import com.example.transactionstarter.model.TransactionStatus;
import com.example.transactionstarter.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * What we return to callers. Kept separate from the entity so the API
 * shape doesn't silently change if the entity's persistence details
 * change later.
 */
public class TransactionResponse {

    private final String transactionId;
    private final String customerId;
    private final BigDecimal amount;
    private final Currency currency;
    private final TransactionType type;
    private final TransactionStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public TransactionResponse(Transaction transaction) {
        this.transactionId = transaction.getTransactionId();
        this.customerId = transaction.getCustomerId();
        this.amount = transaction.getAmount();
        this.currency = transaction.getCurrency();
        this.type = transaction.getType();
        this.status = transaction.getStatus();
        this.createdAt = transaction.getCreatedAt();
        this.updatedAt = transaction.getUpdatedAt();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
