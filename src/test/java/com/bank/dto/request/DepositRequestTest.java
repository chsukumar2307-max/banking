package com.bank.dto.request;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class DepositRequestTest {
    @Test
    void testSettersAndGetters() {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("ACC123");
        req.setAmount(new BigDecimal("100.00"));
        assertEquals("ACC123", req.getAccountNumber());
        assertEquals(new BigDecimal("100.00"), req.getAmount());
    }

    @Test
    void testToStringAndEquals() {
        DepositRequest req1 = new DepositRequest();
        req1.setAccountNumber("ACC123");
        req1.setAmount(new BigDecimal("100.00"));
        DepositRequest req2 = new DepositRequest();
        req2.setAccountNumber("ACC123");
        req2.setAmount(new BigDecimal("100.00"));
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        assertTrue(req1.toString().contains("ACC123"));
    }
}

