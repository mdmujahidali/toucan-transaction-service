package com.example.transactionstarter;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.model.Currency;
import com.example.transactionstarter.model.TransactionStatus;
import com.example.transactionstarter.model.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateTransactionRequest validRequest(String transactionId, String customerId) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionId(transactionId);
        request.setCustomerId(customerId);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency(Currency.USD);
        request.setType(TransactionType.PAYMENT);
        return request;
    }

    // 1. A transaction created successfully
    @Test
    void createTransaction_succeeds_withValidPayload() throws Exception {
        CreateTransactionRequest request = validRequest("TXN-1001", "CUST-1");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN-1001"))
                .andExpect(jsonPath("$.customerId").value("CUST-1"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // 2. A transaction rejected because it fails validation
    @Test
    void createTransaction_rejected_whenAmountIsNegative() throws Exception {
        CreateTransactionRequest request = validRequest("TXN-1002", "CUST-1");
        request.setAmount(new BigDecimal("-50.00"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // 3. A duplicate Transaction ID rejected
    @Test
    void createTransaction_rejected_whenTransactionIdAlreadyExists() throws Exception {
        CreateTransactionRequest request = validRequest("TXN-1003", "CUST-1");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // second attempt with the same transactionId
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // 4. A request for a transaction that does not exist
    @Test
    void getTransaction_returnsNotFound_whenTransactionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", "DOES-NOT-EXIST"))
                .andExpect(status().isNotFound());
    }

    // Additional test: status update happy path
    @Test
    void updateStatus_succeeds_forValidTransition() throws Exception {
        CreateTransactionRequest createRequest = validRequest("TXN-1005", "CUST-2");
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        UpdateStatusRequest statusRequest = new UpdateStatusRequest();
        statusRequest.setStatus(TransactionStatus.COMPLETED);

        mockMvc.perform(patch("/api/transactions/{id}/status", "TXN-1005")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    // Additional test: status update rejected once a transaction is terminal
    @Test
    void updateStatus_rejected_whenTransactionAlreadyTerminal() throws Exception {
        CreateTransactionRequest createRequest = validRequest("TXN-1006", "CUST-2");
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        UpdateStatusRequest toCompleted = new UpdateStatusRequest();
        toCompleted.setStatus(TransactionStatus.COMPLETED);
        mockMvc.perform(patch("/api/transactions/{id}/status", "TXN-1006")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toCompleted)))
                .andExpect(status().isOk());

        // COMPLETED is terminal -> this must be rejected
        UpdateStatusRequest toPending = new UpdateStatusRequest();
        toPending.setStatus(TransactionStatus.PENDING);
        mockMvc.perform(patch("/api/transactions/{id}/status", "TXN-1006")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toPending)))
                .andExpect(status().isConflict());
    }

    // Additional test: get all transactions for a customer
    @Test
    void getCustomerTransactions_returnsOnlyThatCustomersTransactions() throws Exception {
        CreateTransactionRequest txn1 = validRequest("TXN-2001", "CUST-3");
        CreateTransactionRequest txn2 = validRequest("TXN-2002", "CUST-3");
        CreateTransactionRequest otherCustomer = validRequest("TXN-2003", "CUST-4");

        for (CreateTransactionRequest r : List.of(txn1, txn2, otherCustomer)) {
            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(r)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/customers/{customerId}/transactions", "CUST-3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
