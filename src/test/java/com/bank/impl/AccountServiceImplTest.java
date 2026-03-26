package com.bank.impl;
import com.bank.dto.response.*;
import com.bank.entity.Customer;
import com.bank.entity.Transaction;
import com.bank.entity.TransactionStatus;
import com.bank.entity.TransactionType;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter .api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class AccountServiceImplTest {
    @Mock
    CustomerRepository customerRepository;
    @Mock
    TransactionRepository transactionRepository;
    @InjectMocks
    AccountServiceImpl accountServiceImpl;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void deposit_success() {
        Customer customer = Customer.builder().accountNumber("123").balance(BigDecimal.valueOf(100)).build();
        when(customerRepository.findByAccountNumberForUpdate("123")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        DepositResponse response = accountServiceImpl.deposit("123", BigDecimal.valueOf(50));
        assertEquals("Deposit successful", response.getStatus());
        assertEquals(BigDecimal.valueOf(150), response.getBalance());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deposit_invalidAmount() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                accountServiceImpl.deposit("123", BigDecimal.ZERO));
        assertEquals("Deposit amount must be positive and non-null", ex.getMessage());
    }

    @Test
    void deposit_accountNotFound() {
        when(customerRepository.findByAccountNumberForUpdate("123")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, this::invokeDepositAccountNotFound);
    }

    private void invokeDepositAccountNotFound() {
        accountServiceImpl.deposit("123", BigDecimal.valueOf(10));
    }

    @Test
    void withdraw_success() {
        Customer customer = Customer.builder().accountNumber("123").balance(BigDecimal.valueOf(100)).build();
        when(customerRepository.findByAccountNumberForUpdate("123")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        WithdrawResponse response = accountServiceImpl.withdraw("123", BigDecimal.valueOf(50));
        assertEquals("Withdrawal successful", response.getStatus());
        assertEquals(BigDecimal.valueOf(50), response.getBalance());
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
    }

    @Test
    void withdraw_insufficientBalance() {
        Customer customer = Customer.builder().accountNumber("123").balance(BigDecimal.valueOf(10)).build();
        when(customerRepository.findByAccountNumberForUpdate("123")).thenReturn(Optional.of(customer));
        assertThrows(InsufficientBalanceException.class, this::invokeWithdrawInsufficientBalance);
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
    }

    private void invokeWithdrawInsufficientBalance() {
        accountServiceImpl.withdraw("123", BigDecimal.valueOf(50));
    }

    @Test
    void withdraw_accountNotFound() {
        when(customerRepository.findByAccountNumberForUpdate("123")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, this::invokeWithdrawAccountNotFound);
    }

    private void invokeWithdrawAccountNotFound() {
        accountServiceImpl.withdraw("123", BigDecimal.valueOf(10));
    }

    @Test
    void transfer_success() {
        Customer from = Customer.builder().accountNumber("A").balance(BigDecimal.valueOf(100)).build();
        Customer to = Customer.builder().accountNumber("B").balance(BigDecimal.valueOf(50)).build();
        when(customerRepository.findByAccountNumberForUpdate("A")).thenReturn(Optional.of(from));
        when(customerRepository.findByAccountNumberForUpdate("B")).thenReturn(Optional.of(to));
        when(customerRepository.save(any(Customer.class))).thenReturn(from).thenReturn(to);
        TransferResponse response = accountServiceImpl.transfer("A", "B", BigDecimal.valueOf(30));
        assertEquals("Transfer successful", response.getStatus());
        verify(transactionRepository, atLeast(2)).save(any(Transaction.class));
    }

    @Test
    void transfer_insufficientBalance() {
        Customer from = Customer.builder().accountNumber("A").balance(BigDecimal.valueOf(10)).build();
        Customer to = Customer.builder().accountNumber("B").balance(BigDecimal.valueOf(50)).build();
        when(customerRepository.findByAccountNumberForUpdate("A")).thenReturn(Optional.of(from));
        when(customerRepository.findByAccountNumberForUpdate("B")).thenReturn(Optional.of(to));
        assertThrows(InsufficientBalanceException.class, this::invokeTransferInsufficientBalance);
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
    }

    private void invokeTransferInsufficientBalance() {
        accountServiceImpl.transfer("A", "B", BigDecimal.valueOf(30));
    }

    @Test
    void transfer_sameAccount() {
        assertThrows(IllegalArgumentException.class, this::invokeTransferSameAccount);
    }

    private void invokeTransferSameAccount() {
        accountServiceImpl.transfer("A", "A", BigDecimal.valueOf(10));
    }

    @Test
    void transfer_accountNotFound() {
        when(customerRepository.findByAccountNumberForUpdate("A")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, this::invokeTransferAccountNotFound);
    }

    private void invokeTransferAccountNotFound() {
        accountServiceImpl.transfer("A", "B", BigDecimal.valueOf(10));
    }

    @Test
    void balance_success() {
        Customer customer = Customer.builder().accountNumber("123").balance(BigDecimal.valueOf(100)).build();
        when(customerRepository.findByAccountNumber("123")).thenReturn(Optional.of(customer));
        BalanceResponse response = accountServiceImpl.balance("123");
        assertEquals("123", response.getAccountNumber());
        assertEquals(BigDecimal.valueOf(100), response.getBalance());
    }

    @Test
    void balance_accountNotFound() {
        when(customerRepository.findByAccountNumber("123")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountServiceImpl.balance("123"));
    }

    @Test
    void transactions_success() {
        Customer customer = Customer.builder().accountNumber("123").build();
        when(customerRepository.findByAccountNumber("123")).thenReturn(Optional.of(customer));
        Transaction t = Transaction.builder().transactionId(1L).accountNumber("123").transactionType(TransactionType.DEPOSIT)
                .amount(BigDecimal.TEN).balanceAfterTransaction(BigDecimal.TEN).transactionDate(java.time.Instant.now())
                .status(TransactionStatus.SUCCESS).statement("Deposit").build();
        when(transactionRepository.findByAccountNumberOrderByTransactionDateDesc("123")).thenReturn(List.of(t));
        List<TransactionResponse> responses = accountServiceImpl.transactions("123");
        assertEquals(1, responses.size());
        assertEquals("123", responses.get(0).getAccountNumber());
    }

    @Test
    void transactions_accountNotFound() {
        when(customerRepository.findByAccountNumber("123")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountServiceImpl.transactions("123"));
    }

    // Helper to avoid unchecked warnings for Specification
    @SuppressWarnings("unchecked")
    private static Specification<Transaction> anyTransactionSpecification() {
        return (Specification<Transaction>) any(Specification.class);
    }

    @Test
    void searchTransactions_filtersByTransactionId() {
        Transaction t = Transaction.builder().transactionId(1L).accountNumber("123").transactionType(TransactionType.DEPOSIT)
                .amount(BigDecimal.TEN).balanceAfterTransaction(BigDecimal.TEN).transactionDate(java.time.Instant.now())
                .status(TransactionStatus.SUCCESS).statement("Deposit").build();
        when(transactionRepository.findAll(anyTransactionSpecification(), any(Pageable.class))).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(t)));
        List<TransactionResponse> responses = accountServiceImpl.searchTransactions(1L, null, null, null, 0, 10);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getTransactionId());
    }

    @Test
    void searchTransactions_filtersByAccountNumber() {
        Transaction t = Transaction.builder().transactionId(2L).accountNumber("456").transactionType(TransactionType.WITHDRAW)
                .amount(BigDecimal.ONE).balanceAfterTransaction(BigDecimal.ONE).transactionDate(java.time.Instant.now())
                .status(TransactionStatus.SUCCESS).statement("Withdraw").build();
        when(transactionRepository.findAll(anyTransactionSpecification(), any(Pageable.class))).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(t)));
        List<TransactionResponse> responses = accountServiceImpl.searchTransactions(null, "456", null, null, 0, 10);
        assertEquals(1, responses.size());
        assertEquals("456", responses.get(0).getAccountNumber());
    }

    @Test
    void searchTransactions_filtersByTransactionDate_validAndInvalid() {
        java.time.Instant now = java.time.Instant.now();
        Transaction t = Transaction.builder().transactionId(3L).accountNumber("789").transactionType(TransactionType.DEPOSIT)
                .amount(BigDecimal.TEN).balanceAfterTransaction(BigDecimal.TEN).transactionDate(now)
                .status(TransactionStatus.SUCCESS).statement("Deposit").build();
        when(transactionRepository.findAll(anyTransactionSpecification(), any(Pageable.class))).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(t)));
        // Valid date
        List<TransactionResponse> responses = accountServiceImpl.searchTransactions(null, null, now.toString(), null, 0, 10);
        assertEquals(1, responses.size());
        // Invalid date (should not throw, just ignore filter)
        responses = accountServiceImpl.searchTransactions(null, null, "invalid-date", null, 0, 10);
        assertEquals(1, responses.size());
    }

    @Test
    void searchTransactions_filtersByTransactionType_validAndInvalid() {
        Transaction t = Transaction.builder().transactionId(4L).accountNumber("999").transactionType(TransactionType.WITHDRAW)
                .amount(BigDecimal.ONE).balanceAfterTransaction(BigDecimal.ONE).transactionDate(java.time.Instant.now())
                .status(TransactionStatus.SUCCESS).statement("Withdraw").build();
        when(transactionRepository.findAll(anyTransactionSpecification(), any(Pageable.class))).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(t)));
        // Valid type
        List<TransactionResponse> responses = accountServiceImpl.searchTransactions(null, null, null, "WITHDRAW", 0, 10);
        assertEquals(1, responses.size());
        // Invalid type (should not throw, just ignore filter)
        responses = accountServiceImpl.searchTransactions(null, null, null, "INVALID_TYPE", 0, 10);
        assertEquals(1, responses.size());
    }

    @Test
    void searchTransactions_handlesNullsAndPagination() {
        Transaction t = Transaction.builder().transactionId(5L).accountNumber("888").transactionType(TransactionType.DEPOSIT)
                .amount(BigDecimal.TEN).balanceAfterTransaction(BigDecimal.TEN).transactionDate(java.time.Instant.now())
                .status(TransactionStatus.SUCCESS).statement("Deposit").build();
        when(transactionRepository.findAll(anyTransactionSpecification(), any(Pageable.class))).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(t)));
        // All null filters
        List<TransactionResponse> responses = accountServiceImpl.searchTransactions(null, null, null, null, null, null);
        assertEquals(1, responses.size());
        // Custom pagination
        responses = accountServiceImpl.searchTransactions(null, null, null, null, 1, 5);
        assertEquals(1, responses.size());
    }
}
