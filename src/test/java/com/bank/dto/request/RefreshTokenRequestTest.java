package com.bank.dto.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenRequestTest {
    @Test
    void testLombokMethods() {
        RefreshTokenRequest req1 = new RefreshTokenRequest();
        req1.setRefreshToken("token");
        RefreshTokenRequest req2 = new RefreshTokenRequest();
        req2.setRefreshToken("token");
        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        assertTrue(req1.toString().contains("token"));
    }
}

