package com.bank.service.impl;
import com.bank.dto.response.DepositResponse;
import com.bank.entity.Customer;
import com.bank.exception.AccountNotFoundException;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deposit_success() {
        Customer customer = new Customer();
        customer.setAccountNumber("123");
        customer.setBalance(BigDecimal.valueOf(100));
        when(customerRepository.findByAccountNumberForUpdate("123")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        DepositResponse response = accountService.deposit("123", BigDecimal.valueOf(50));
        assertEquals("123", customer.getAccountNumber());
        assertEquals(BigDecimal.valueOf(150), customer.getBalance());
        assertNotNull(response);
    }

    @Test
    void deposit_accountNotFound() {
        when(customerRepository.findByAccountNumberForUpdate("999")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.deposit("999", BigDecimal.valueOf(50)));
    }
}

