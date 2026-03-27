package com.bank.security;
import com.bank.entity.Customer;
import com.bank.enums.CustomerStatus;
import com.bank.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerUserDetailsServiceTest {
    @Mock
    private CustomerRepository customerRepository;
    @InjectMocks
    private CustomerUserDetailsService service;

    @Test
    @SuppressWarnings("unused")
    void loadUserByUsername_userFound_active() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            Customer customer = new Customer();
            customer.setUsername("user");
            customer.setPassword("pass");
            customer.setStatus(CustomerStatus.ACTIVE);
            when(customerRepository.findByUsername("user")).thenReturn(Optional.of(customer));
            UserDetails details = service.loadUserByUsername("user");
            assertEquals("user", details.getUsername());
            assertEquals("pass", details.getPassword());
            assertTrue(details.isEnabled());
            assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
        }
    }

    @Test
    @SuppressWarnings("unused")
    void loadUserByUsername_userFound_inactive() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            Customer customer = new Customer();
            customer.setUsername("user");
            customer.setPassword("pass");
            customer.setStatus(null); // No INACTIVE, so use null to simulate not ACTIVE
            when(customerRepository.findByUsername("user")).thenReturn(Optional.of(customer));
            UserDetails details = service.loadUserByUsername("user");
            assertFalse(details.isEnabled());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void loadUserByUsername_userNotFound() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            when(customerRepository.findByUsername("missing")).thenReturn(Optional.empty());
            assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing"));
        }
    }
}
