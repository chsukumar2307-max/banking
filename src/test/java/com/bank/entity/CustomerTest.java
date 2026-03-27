package com.bank.entity;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        Instant now = Instant.now();
        Customer c = new Customer(1L, "user", "1234567890", "pass", "ACC1",
                new BigDecimal("100.00"), CustomerStatus.ACTIVE, now);
        assertEquals(1L, c.getId());
        assertEquals("user", c.getUsername());
        assertEquals("1234567890", c.getMobileNumber());
        assertEquals("pass", c.getPassword());
        assertEquals("ACC1", c.getAccountNumber());
        assertEquals(new BigDecimal("100.00"), c.getBalance());
        assertEquals(CustomerStatus.ACTIVE, c.getStatus());
        assertEquals(now, c.getCreatedAt());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        Customer c = new Customer();
        c.setId(2L);
        c.setUsername("user2");
        c.setMobileNumber("0987654321");
        c.setPassword("pass2");
        c.setAccountNumber("ACC2");
        c.setBalance(new BigDecimal("200.00"));
        c.setStatus(CustomerStatus.ACTIVE);
        Instant now = Instant.now();
        c.setCreatedAt(now);
        assertEquals(2L, c.getId());
        assertEquals("user2", c.getUsername());
        assertEquals("0987654321", c.getMobileNumber());
        assertEquals("pass2", c.getPassword());
        assertEquals("ACC2", c.getAccountNumber());
        assertEquals(new BigDecimal("200.00"), c.getBalance());
        assertEquals(CustomerStatus.ACTIVE, c.getStatus());
        assertEquals(now, c.getCreatedAt());
    }

    @Test
    void testBuilder() {
        Instant now = Instant.now();
        Customer c = Customer.builder()
                .id(3L)
                .username("builder")
                .mobileNumber("1112223333")
                .password("builderpass")
                .accountNumber("ACC3")
                .balance(new BigDecimal("300.00"))
                .status(CustomerStatus.ACTIVE)
                .createdAt(now)
                .build();
        assertEquals(3L, c.getId());
        assertEquals("builder", c.getUsername());
        assertEquals("1112223333", c.getMobileNumber());
        assertEquals("builderpass", c.getPassword());
        assertEquals("ACC3", c.getAccountNumber());
        assertEquals(new BigDecimal("300.00"), c.getBalance());
        assertEquals(CustomerStatus.ACTIVE, c.getStatus());
        assertEquals(now, c.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Instant now = Instant.now();
        Customer c1 = new Customer(1L, "user", "1234567890", "pass", "ACC1",
                new BigDecimal("100.00"), CustomerStatus.ACTIVE, now);
        Customer c2 = new Customer(1L, "user", "1234567890", "pass", "ACC1",
                new BigDecimal("100.00"), CustomerStatus.ACTIVE, now);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testToString() {
        Customer c = new Customer();
        c.setId(4L);
        c.setUsername("toString");
        c.setAccountNumber("ACC4");
        String str = c.toString();
        assertTrue(str.contains("toString"));
        assertTrue(str.contains("ACC4"));
    }

    @Test
    void testPrePersistSetsDefaults() {
        Customer c = new Customer();
        c.onCreate();
        assertNotNull(c.getCreatedAt());
        assertNotNull(c.getBalance());
        assertEquals(BigDecimal.ZERO, c.getBalance());
        assertEquals(CustomerStatus.ACTIVE, c.getStatus());
    }
}
