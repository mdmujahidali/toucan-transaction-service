package com.example.transactionstarter.dto;

import com.example.transactionstarter.model.Currency;
import com.example.transactionstarter.model.TransactionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Payload for creating a new transaction.
 *
 * Field-level rules enforced here (via bean validation):
 * - transactionId, customerId: required, non-blank.
 * - amount: required, greater than zero, capped at 1,000,000
 *   (ASSUMPTION: default maximum in the absence of an assigned variant),
 *   at most 2 decimal places.
 * - currency, type: required; must additionally be one of the enum's
 *   permitted values (enforced automatically by Jackson deserialization —
 *   an unknown value is rejected before it even reaches the service).
 *
 * Status is deliberately not part of this request: every new transaction
 * starts as PENDING. Letting the caller set the initial status would let
 * them create a transaction that is already COMPLETED or FAILED without
 * it ever having been processed, which defeats the point of having a
 * status at all.
 */
public class CreateTransactionRequest {

    @NotBlank(message = "transactionId is required")
    private String transactionId;

    @NotBlank(message = "customerId is required")
    private String customerId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    @DecimalMax(value = "1000000.00", message = "amount must not exceed 1,000,000")
    @Digits(integer = 10, fraction = 2, message = "amount may have at most 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "currency is required")
    private Currency currency;

    @NotNull(message = "type is required")
    private TransactionType type;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}
