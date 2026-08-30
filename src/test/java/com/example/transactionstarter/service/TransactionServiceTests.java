package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.exception.DuplicateTransactionException;
import com.example.transactionstarter.model.Currency;
import com.example.transactionstarter.model.TransactionType;
import com.example.transactionstarter.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionServiceTests {

    @Test
    void createTransaction_reportsConflictWhenDatabaseRejectsConcurrentDuplicate() {
        TransactionRepository repository = mock(TransactionRepository.class);
        TransactionService service = new TransactionService(repository);
        CreateTransactionRequest request = validRequest();

        when(repository.existsById(request.getTransactionId())).thenReturn(false);
        when(repository.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThrows(DuplicateTransactionException.class, () -> service.createTransaction(request));
    }

    private CreateTransactionRequest validRequest() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionId("TXN-CONCURRENT");
        request.setCustomerId("CUST-1");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency(Currency.USD);
        request.setType(TransactionType.PAYMENT);
        return request;
    }
}
