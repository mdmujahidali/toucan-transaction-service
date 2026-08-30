package com.example.transactionstarter.exception;

import com.example.transactionstarter.model.TransactionStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(TransactionStatus from, TransactionStatus to) {
        super("Cannot change status from " + from + " to " + to);
    }
}
