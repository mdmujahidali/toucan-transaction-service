package com.example.transactionstarter.dto;

import java.time.Instant;
import java.util.List;

/**
 * Uniform error body returned for every failure case, so clients can
 * parse errors the same way regardless of which endpoint failed.
 */
public class ErrorResponse {

    private final Instant timestamp = Instant.now();
    private final int status;
    private final String error;
    private final List<String> messages;

    public ErrorResponse(int status, String error, List<String> messages) {
        this.status = status;
        this.error = error;
        this.messages = messages;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public List<String> getMessages() {
        return messages;
    }
}
