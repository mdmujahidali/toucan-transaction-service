package com.example.transactionstarter.model;

/**
 * Permitted transaction types.
 *
 * ASSUMPTION: No variant was assigned to this candidate (no invitation
 * email specifying a custom set was received), so this is a default set
 * chosen to be representative of a real payments platform. Update this
 * enum if a specific variant is later supplied.
 */
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER,
    PAYMENT
}
