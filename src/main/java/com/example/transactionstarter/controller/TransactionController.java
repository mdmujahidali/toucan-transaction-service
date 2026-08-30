package com.example.transactionstarter.controller;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.TransactionResponse;
import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // A. Create transaction
    @PostMapping("/api/transactions")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // B. Get transaction
    @GetMapping("/api/transactions/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    // C. Update transaction status
    @PatchMapping("/api/transactions/{transactionId}/status")
    public ResponseEntity<TransactionResponse> updateStatus(
            @PathVariable String transactionId,
            @Valid @RequestBody UpdateStatusRequest request) {
        TransactionResponse response = transactionService.updateStatus(transactionId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    // D. Get customer transactions
    @GetMapping("/api/customers/{customerId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getCustomerTransactions(
            @PathVariable String customerId) {
        return ResponseEntity.ok(transactionService.getCustomerTransactions(customerId));
    }
}
