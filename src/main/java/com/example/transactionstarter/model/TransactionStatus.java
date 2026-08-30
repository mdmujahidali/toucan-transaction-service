package com.example.transactionstarter.model;

/**
 * Lifecycle states for a transaction.
 *
 * PENDING is the only status a transaction can be created with.
 * COMPLETED, FAILED and CANCELLED are terminal — once reached, no further
 * status change is permitted. See TransactionService for the allowed
 * transition rules and the reasoning behind them.
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}
