package com.bank.controller;
import com.bank.dto.request.*;
import com.bank.dto.response.*;
import com.bank.service.AccountService;
import com.bank.util.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import org.springframework.context.annotation.Import;

@WebMvcTest(
    value = AccountController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.bank.audit.AuditLoggingAspect.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.bank.security.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.bank.config.SwaggerConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.bank.config.TwilioConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.bank.config.RedisConfig.class)
    },
    excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
)
@Import(com.bank.exception.GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {
    @SuppressWarnings("unused")
    @Autowired
    private MockMvc mockMvc;
    @SuppressWarnings("unused")
    @MockBean
    private AccountService accountService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deposit_success() throws Exception {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("12345");
        req.setAmount(BigDecimal.valueOf(1000));
        when(accountService.deposit("12345", BigDecimal.valueOf(1000)))
            .thenReturn(new DepositResponse("Deposit successful", BigDecimal.valueOf(5000)));
        mockMvc.perform(post("/api/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Deposit successful"))
                .andExpect(jsonPath("$.balance").value(5000));
    }

    /**
     * Test deposit with negative amount (validation error).
     */
    @Test
    void deposit_negativeAmount_returnsBadRequest() throws Exception {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("12345");
        req.setAmount(BigDecimal.valueOf(-100));
        mockMvc.perform(post("/api/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").value("Amount should be in Positive"));
    }

    @Test
    void withdraw_success() throws Exception {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountNumber("12345");
        req.setAmount(BigDecimal.valueOf(500));
        when(accountService.withdraw("12345", BigDecimal.valueOf(500)))
            .thenReturn(new WithdrawResponse("Withdrawal successful", BigDecimal.valueOf(4500)));
        mockMvc.perform(post("/api/accounts/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Withdrawal successful"))
                .andExpect(jsonPath("$.balance").value(4500));
    }

    /**
     * Test withdraw with insufficient balance (service throws exception).
     */
    @Test
    void withdraw_insufficientBalance_returnsBadRequest() throws Exception {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountNumber("12345");
        req.setAmount(BigDecimal.valueOf(10000));
        when(accountService.withdraw("12345", BigDecimal.valueOf(10000)))
            .thenThrow(new InsufficientBalanceException("Insufficient balance"));
        mockMvc.perform(post("/api/accounts/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    @Test
    void transfer_success() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("12345");
        req.setToAccount("67890");
        req.setAmount(BigDecimal.valueOf(500));
        when(accountService.transfer("12345", "67890", BigDecimal.valueOf(500)))
            .thenReturn(new TransferResponse("Transfer successful"));
        mockMvc.perform(post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Transfer successful"));
    }

    /**
     * Test transfer with same from and to account (validation error).
     */
    @Test
    void transfer_sameAccount_returnsBadRequest() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("12345");
        req.setToAccount("12345");
        req.setAmount(BigDecimal.valueOf(100));
        when(accountService.transfer("12345", "12345", BigDecimal.valueOf(100)))
            .thenThrow(new IllegalArgumentException("From and To account cannot be the same"));
        mockMvc.perform(post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("From and To account cannot be the same"));
    }

    @Test
    void balance_success() throws Exception {
        when(accountService.balance("12345"))
            .thenReturn(new BalanceResponse("12345", BigDecimal.valueOf(5000)));
        mockMvc.perform(get("/api/accounts/balance/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.balance").value(5000));
    }

    /**
     * Test balance for non-existent account (service throws exception).
     */
    @Test
    void balance_nonExistentAccount_returnsNotFound() throws Exception {
        when(accountService.balance("99999"))
            .thenThrow(new AccountNotFoundException("Account not found"));
        mockMvc.perform(get("/api/accounts/balance/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void transactions_success() throws Exception {
        TransactionResponse t = new TransactionResponse(1L, "12345", null, BigDecimal.TEN, BigDecimal.TEN, java.time.Instant.now(), null, "Deposit");
        when(accountService.transactions("12345")).thenReturn(List.of(t));
        mockMvc.perform(get("/api/accounts/transactions/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("12345"))
                .andExpect(jsonPath("$[0].statement").value("Deposit"));
    }

    /**
     * Test transactions for non-existent account (service throws exception).
     */
    @Test
    void transactions_nonExistentAccount_returnsNotFound() throws Exception {
        when(accountService.transactions("99999"))
            .thenThrow(new AccountNotFoundException("Account not found"));
        mockMvc.perform(get("/api/accounts/transactions/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void transactionsHistoryByFilter_transactionId() throws Exception {
        TransactionResponse t = new TransactionResponse(1L, "12345", null, BigDecimal.TEN, BigDecimal.TEN, java.time.Instant.now(), null, "Deposit");
        when(accountService.searchTransactions(eq(1L), isNull(), isNull(), isNull(), anyInt(), anyInt())).thenReturn(List.of(t));
        mockMvc.perform(get("/api/accounts/transactionsHistory/transactionId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value(1))
                .andExpect(jsonPath("$[0].statement").value("Deposit"));
    }

    @Test
    void transactionsHistoryByFilter_accountNumber() throws Exception {
        TransactionResponse t = new TransactionResponse(2L, "456", null, BigDecimal.ONE, BigDecimal.ONE, java.time.Instant.now(), null, "Withdraw");
        when(accountService.searchTransactions(isNull(), eq("456"), isNull(), isNull(), anyInt(), anyInt())).thenReturn(List.of(t));
        mockMvc.perform(get("/api/accounts/transactionsHistory/accountNumber=456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("456"))
                .andExpect(jsonPath("$[0].statement").value("Withdraw"));
    }

    @Test
    void transactionsHistoryByFilter_transactionType() throws Exception {
        TransactionResponse t = new TransactionResponse(3L, "789", null, BigDecimal.TEN, BigDecimal.TEN, java.time.Instant.now(), null, "Deposit");
        when(accountService.searchTransactions(isNull(), isNull(), isNull(), eq("DEPOSIT"), anyInt(), anyInt())).thenReturn(List.of(t));
        mockMvc.perform(get("/api/accounts/transactionsHistory/transactionType=DEPOSIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statement").value("Deposit"));
    }

    @Test
    void transactionsHistoryByFilter_invalidFilter() throws Exception {
        mockMvc.perform(get("/api/accounts/transactionsHistory/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("[]")); // Response is an empty array
    }

    // Additional endpoint tests will be added here for full coverage

    @Test
    void deposit_nullAccountNumber_returnsBadRequest() throws Exception {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber(null);
        req.setAmount(BigDecimal.valueOf(100));
        mockMvc.perform(post("/api/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deposit_nullAmount_returnsBadRequest() throws Exception {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("12345");
        req.setAmount(null);
        mockMvc.perform(post("/api/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deposit_malformedJson_returnsBadRequest() throws Exception {
        String malformedJson = "{\"accountNumber\":12345,\"amount\":\"notANumber\"}";
        mockMvc.perform(post("/api/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_nullAccountNumber_returnsBadRequest() throws Exception {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountNumber(null);
        req.setAmount(BigDecimal.valueOf(100));
        mockMvc.perform(post("/api/accounts/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_nullAmount_returnsBadRequest() throws Exception {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountNumber("12345");
        req.setAmount(null);
        mockMvc.perform(post("/api/accounts/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_nullFromAccount_returnsBadRequest() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromAccount(null);
        req.setToAccount("67890");
        req.setAmount(BigDecimal.valueOf(100));
        mockMvc.perform(post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_nullToAccount_returnsBadRequest() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("12345");
        req.setToAccount(null);
        req.setAmount(BigDecimal.valueOf(100));
        mockMvc.perform(post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_nullAmount_returnsBadRequest() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("12345");
        req.setToAccount("67890");
        req.setAmount(null);
        mockMvc.perform(post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_insufficientBalance_returnsBadRequest() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("12345");
        req.setToAccount("67890");
        req.setAmount(BigDecimal.valueOf(10000));
        when(accountService.transfer("12345", "67890", BigDecimal.valueOf(10000)))
                .thenThrow(new InsufficientBalanceException("Insufficient balance"));
        mockMvc.perform(post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    @Test
    void transfer_nonExistentAccount_returnsNotFound() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromAccount("99999");
        req.setToAccount("67890");
        req.setAmount(BigDecimal.valueOf(100));
        when(accountService.transfer("99999", "67890", BigDecimal.valueOf(100)))
                .thenThrow(new AccountNotFoundException("Account not found"));
        mockMvc.perform(post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void transactionsHistoryByFilter_transactionId_invalidNumber_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/accounts/transactionsHistory/transactionId=notANumber"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("[]"));
    }

    @Test
    void transactionsHistoryByFilter_validFilter_noResults_returnsOkEmptyList() throws Exception {
        when(accountService.searchTransactions(eq(1L), isNull(), isNull(), isNull(), anyInt(), anyInt())).thenReturn(List.of());
        mockMvc.perform(get("/api/accounts/transactionsHistory/transactionId=1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void deposit_genericException_returnsInternalServerError() throws Exception {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("12345");
        req.setAmount(BigDecimal.valueOf(100));
        when(accountService.deposit(anyString(), any())).thenThrow(new RuntimeException("Unexpected error"));
        mockMvc.perform(post("/api/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    @Test
    void withdraw_zeroAmount_returnsBadRequest() throws Exception {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountNumber("12345");
        req.setAmount(BigDecimal.ZERO);
        mockMvc.perform(post("/api/accounts/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deposit_veryLargeAmount_success() throws Exception {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("12345");
        req.setAmount(new BigDecimal("1000000000000"));
        when(accountService.deposit("12345", new BigDecimal("1000000000000")))
                .thenReturn(new DepositResponse("Deposit successful", new BigDecimal("1000000005000")));
        mockMvc.perform(post("/api/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Deposit successful"))
                .andExpect(jsonPath("$.balance").value(1000000005000L));
    }

    @Test
    void transactionsHistoryByFilter_emptyKeyAndValue_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/accounts/transactionsHistory/="))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("[]"));
    }

    @Test
    void transactionsHistoryByFilter_accountNumberEmptyValue_returnsOkEmptyList() throws Exception {
        when(accountService.searchTransactions(isNull(), eq(""), isNull(), isNull(), anyInt(), anyInt())).thenReturn(List.of());
        mockMvc.perform(get("/api/accounts/transactionsHistory/accountNumber="))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void transactionsHistoryByFilter_emptyKey_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/accounts/transactionsHistory/=somevalue"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("[]"));
    }
}
