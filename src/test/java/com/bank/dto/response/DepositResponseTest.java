package com.bank.dto.response;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class DepositResponseTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        DepositResponse resp = new DepositResponse("SUCCESS", new BigDecimal("100.00"));
        assertEquals("SUCCESS", resp.getStatus());
        assertEquals(new BigDecimal("100.00"), resp.getBalance());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        DepositResponse resp = new DepositResponse();
        resp.setStatus("FAILED");
        resp.setBalance(new BigDecimal("50.00"));
        assertEquals("FAILED", resp.getStatus());
        assertEquals(new BigDecimal("50.00"), resp.getBalance());
    }

    @Test
    void testEqualsAndHashCode() {
        DepositResponse resp1 = new DepositResponse("SUCCESS", new BigDecimal("100.00"));
        DepositResponse resp2 = new DepositResponse("SUCCESS", new BigDecimal("100.00"));
        assertEquals(resp1, resp2);
        assertEquals(resp1.hashCode(), resp2.hashCode());
    }

    @Test
    void testToString() {
        DepositResponse resp = new DepositResponse("SUCCESS", new BigDecimal("100.00"));
        String str = resp.toString();
        assertTrue(str.contains("SUCCESS"));
        assertTrue(str.contains("100.00"));
    }
}

