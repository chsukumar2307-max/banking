package com.bank.dto.response;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class WithdrawResponseTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        WithdrawResponse resp = new WithdrawResponse("SUCCESS", new BigDecimal("200.00"));
        assertEquals("SUCCESS", resp.getStatus());
        assertEquals(new BigDecimal("200.00"), resp.getBalance());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        WithdrawResponse resp = new WithdrawResponse();
        resp.setStatus("FAILED");
        resp.setBalance(new BigDecimal("75.00"));
        assertEquals("FAILED", resp.getStatus());
        assertEquals(new BigDecimal("75.00"), resp.getBalance());
    }

    @Test
    void testEqualsAndHashCode() {
        WithdrawResponse resp1 = new WithdrawResponse("SUCCESS", new BigDecimal("200.00"));
        WithdrawResponse resp2 = new WithdrawResponse("SUCCESS", new BigDecimal("200.00"));
        assertEquals(resp1, resp2);
        assertEquals(resp1.hashCode(), resp2.hashCode());
    }

    @Test
    void testToString() {
        WithdrawResponse resp = new WithdrawResponse("SUCCESS", new BigDecimal("200.00"));
        String str = resp.toString();
        assertTrue(str.contains("SUCCESS"));
        assertTrue(str.contains("200.00"));
    }
}

