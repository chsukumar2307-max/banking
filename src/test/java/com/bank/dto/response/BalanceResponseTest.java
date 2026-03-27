package com.bank.dto.response;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class BalanceResponseTest {
    @Test
    void allArgsConstructorAndGetters() {
        BalanceResponse resp = new BalanceResponse("acc123", new BigDecimal("100.00"));
        assertEquals("acc123", resp.getAccountNumber());
        assertEquals(new BigDecimal("100.00"), resp.getBalance());
    }

    @Test
    void equalsAndHashCode() {
        BalanceResponse r1 = new BalanceResponse("a", new BigDecimal("1.00"));
        BalanceResponse r2 = new BalanceResponse("a", new BigDecimal("1.00"));
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void toStringShouldContainFields() {
        BalanceResponse resp = new BalanceResponse("acc", new BigDecimal("50.00"));
        String str = resp.toString();
        assertTrue(str.contains("acc"));
        assertTrue(str.contains("50.00"));
    }
}

