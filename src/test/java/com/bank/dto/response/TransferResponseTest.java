package com.bank.dto.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TransferResponseTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        TransferResponse resp = new TransferResponse("SUCCESS");
        assertEquals("SUCCESS", resp.getStatus());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        TransferResponse resp = new TransferResponse();
        resp.setStatus("FAILED");
        assertEquals("FAILED", resp.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        TransferResponse resp1 = new TransferResponse("SUCCESS");
        TransferResponse resp2 = new TransferResponse("SUCCESS");
        assertEquals(resp1, resp2);
        assertEquals(resp1.hashCode(), resp2.hashCode());
    }

    @Test
    void testToString() {
        TransferResponse resp = new TransferResponse("SUCCESS");
        String str = resp.toString();
        assertTrue(str.contains("SUCCESS"));
    }
}

